package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.jobs.dataPrepare.TimeDimension;
import cn.yizhi.yzt.pipeline.model.RawLogStream;
import cn.yizhi.yzt.pipeline.model.dim.*;
import cn.yizhi.yzt.pipeline.model.fact.FactLogStream;
import cn.yizhi.yzt.pipeline.model.fact.FactOrder;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderDetail;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderItem;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderPromotion;
import cn.yizhi.yzt.pipeline.table.JdbcAsyncFunc;
import cn.yizhi.yzt.pipeline.udf.UdfDateToTimestamp;
import cn.yizhi.yzt.pipeline.udf.UdfLongToTimestamp;
import cn.yizhi.yzt.pipeline.model.dim.*;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Tumble;

import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class LogStreamJob extends StreamJob {

    @Override
    public void defineJob() throws Exception {

        // 注册UDF
        this.registerUdf("udf_to_timestamp", new UdfLongToTimestamp());
        this.registerUdf("date_to_ts", new UdfDateToTimestamp());
        // 注册sql
        this.registerSql("application.yml");


        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_PROMOTION, OdsOrderPromotion.class, true);

        this.createTableFromJdbc("dim_member", "dim_member_view", DimMember.class);
        this.createTableFromJdbc("dim_shop", "dim_shop_view", DimShop.class);
        this.createTableFromJdbc("dim_channel", "dim_biz_channel", DimChannel.class);
        this.createTableFromJdbc("dim_payment_channel", "dim_payment_channel", DimPaymentChannel.class);
        this.createTableFromJdbc("dim_product", "dim_product_view", DimProduct.class);
        this.createTableFromJdbc("dim_product_sku", "dim_product_sku_view", DimProductSKu.class);
        this.createTableFromJdbc("dim_promotion", "dim_promotion_view", DimPromotion.class);
        this.createTableFromJdbc("dim_date", "dim_date", TimeDimension.class);


        // logstream ODS表加工：
        this.createTableFromKafka(SourceTopics.TOPIC_SDK_LOG_STREAM, RawLogStream.class, true);
        Table logStreamTable = this.sqlQuery("odsLogStreamQuery");
        this.appendToKafka(logStreamTable, OdsLogStream.class,"ods_log_stream");

        // logstream Fact表加工：
        this.createTableFromKafka("ods_log_stream", OdsLogStream.class, true);
        Table factLogStreamTable = this.sqlQuery("factLogStreamQuery");
        this.appendToKafka(factLogStreamTable, FactLogStream.class, "fact_log_stream");

        // 订单事务事实表加工
        this.createTableFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class, true);
        Table orderTable = this.sqlQuery("orderFactQuery");
        this.appendToJdbc(orderTable, FactOrder.class, "fact_order");

        // 订单商品明细表加工
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_ITEM, OdsOrderItem.class, true);
//        this.createTableFromKafka("fact_order", FactOrder.class, true);
        // Join上述的订单
        this.createViewFromTable("factOrder", orderTable);
        Table orderDetailTable = this.sqlQuery("orderDetailFactQuery");
        this.appendToJdbc(orderDetailTable, FactOrderDetail.class, "fact_order_detail");

    }

    private void processFactLogStream() {
        DataStream<OdsLogStream> ds = this.createStreamFromKafka("ods_log_stream", OdsLogStream.class);

        JdbcAsyncFunc.JdbcAsyncFuncBuilder<OdsLogStream, DimShop, FactLogStream> jdbcAsyncFuncBuilder =
                new JdbcAsyncFunc.JdbcAsyncFuncBuilder();

        jdbcAsyncFuncBuilder.setConfig(this.serverConfig)
                .setJdbcTable("dim_shop")
                .setRightTableClass(DimShop.class)
                .setOutputClass(FactLogStream.class)
                .setWhereClauseBuilder((odsLogStream ->
                     String.format("shop_id=%d and start_date <= current_date() and end_date > current_date()", odsLogStream.getShopId())
                ))
                .setResultBuilder(((odsLogStream, dimShops) -> {
                    FactLogStream out = new FactLogStream();

                    return Collections.singletonList(out);
                }));

        DataStream<FactLogStream> outds = AsyncDataStream.orderedWait(ds,
                jdbcAsyncFuncBuilder.build(),
                3000,
                TimeUnit.MILLISECONDS)
                .returns(FactLogStream.class);


    }

}
