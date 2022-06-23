package cn.yizhi.yzt.pipeline.jobs.batchJob;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcTableOutputFormat;
import cn.yizhi.yzt.pipeline.model.fact.fullreduce.FactFullReduceAll;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStreamBatch;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderPromotion;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import java.time.LocalDate;

/**
 * @author aorui created on 2020/11/17
 */
public class FactFullReduceBatchJob {

    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {

        DataSet<Row> odsEmallOrderRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                OdsEmallOrder.class,
                "ods_emall_order",
                null)
                .name("ds-ods_emall_order");


        DataSet<Row> odsOrderPromotionRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                OdsOrderPromotion.class,
                "ods_order_promotion",
                null)
                .name("ds-ods_order_promotion");


        DataSet<Row> odsLogStreamRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                OdsLogStreamBatch.class,
                "ods_log_stream",
                null)
                .name("ds-ods_log_stream");


        OutputFormat<FactFullReduceAll> outputFormat =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactFullReduceAll.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_FULL_REDUCE_ALL)
                        .finish();


        DataSet<FactFullReduceUv> fullReduceUvReduceDs = odsLogStreamRow
                .map(new MapFunction<Row, OdsLogStreamBatch>() {
                    @Override
                    public OdsLogStreamBatch map(Row value) throws Exception {
                        return PojoTypes.of(OdsLogStreamBatch.class).fromRow(value);
                    }
                })
                .filter(new FilterFunction<OdsLogStreamBatch>() {
                    @Override
                    public boolean filter(OdsLogStreamBatch value) throws Exception {
                        return value.getShopId() != null && value.getShopId() != 0 && value.getPromotionType() != null
                                && value.getPromotionId() != null && value.getPromotionType() == 2
                                && "ViewActivity".equals(value.getEventName());
                    }
                })
                .distinct(new KeySelector<OdsLogStreamBatch, Tuple2<Integer, String>>() {
                    @Override
                    public Tuple2<Integer, String> getKey(OdsLogStreamBatch value) throws Exception {
                        return new Tuple2<>(value.getPromotionId(), value.getDeviceId());
                    }
                })
                .flatMap(new FlatMapFunction<OdsLogStreamBatch, FactFullReduceUv>() {
                    @Override
                    public void flatMap(OdsLogStreamBatch value, Collector<FactFullReduceUv> out) throws Exception {
                        FactFullReduceUv factFullReduceUv = new FactFullReduceUv();
                        factFullReduceUv.shopId = value.getShopId();
                        factFullReduceUv.promotionId = value.getPromotionId();
                        factFullReduceUv.uv = 1;

                        out.collect(factFullReduceUv);
                    }
                })
                .groupBy(new KeySelector<FactFullReduceUv, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactFullReduceUv value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionId);
                    }
                })
                .reduce(new UvFunction());


        DataSet<OdsOrderPromotion> promotionFilterDs = odsOrderPromotionRow
                .map(new MapFunction<Row, OdsOrderPromotion>() {
                    @Override
                    public OdsOrderPromotion map(Row value) throws Exception {
                        return PojoTypes.of(OdsOrderPromotion.class).fromRow(value);
                    }
                })
                .filter(new FilterFunction<OdsOrderPromotion>() {
                    @Override
                    public boolean filter(OdsOrderPromotion value) throws Exception {
                        return value.getPromotionType() != null && value.getPromotionType() == 2;
                    }
                });


        DataSet<FactFullReduceOrderUser> fullReduceOrderUserDs = odsEmallOrderRow
                .map(new MapFunction<Row, OdsEmallOrder>() {
                    @Override
                    public OdsEmallOrder map(Row value) throws Exception {
                        OdsEmallOrder odsEmallOrder = PojoTypes.of(OdsEmallOrder.class).fromRow(value);
                        if ("wxweb".equals(odsEmallOrder.getSource())) {
                            odsEmallOrder.setSource("web");
                        }
                        return odsEmallOrder;
                    }
                })
                .filter(new FilterFunction<OdsEmallOrder>() {
                    @Override
                    public boolean filter(OdsEmallOrder value) throws Exception {
                        return value.getTransactionNo() != null && !value.getTransactionNo().equals("");
                    }
                })
                .join(promotionFilterDs)
                .where(new KeySelector<OdsEmallOrder, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getId());
                    }
                })
                .equalTo(new KeySelector<OdsOrderPromotion, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> getKey(OdsOrderPromotion value) throws Exception {
                        return new Tuple1<>(value.getOrderId().longValue());
                    }
                })
                .with(new JoinFunction<OdsEmallOrder, OdsOrderPromotion, OrderPromotionJoin>() {
                    @Override
                    public OrderPromotionJoin join(OdsEmallOrder first, OdsOrderPromotion second) throws Exception {
                        OrderPromotionJoin orderPromotion = new OrderPromotionJoin();
                        orderPromotion.odsEmallOrder = first;
                        orderPromotion.orderPromotion = second;

                        return orderPromotion;
                    }
                })
                .distinct(new KeySelector<OrderPromotionJoin, Tuple3<Integer, Integer, Integer>>() {
                    @Override
                    public Tuple3<Integer, Integer, Integer> getKey(OrderPromotionJoin value) throws Exception {
                        return new Tuple3<Integer, Integer, Integer>(value.odsEmallOrder.getShopId(), value.orderPromotion.getPromotionId(), value.odsEmallOrder.getMemberId());
                    }
                })
                .flatMap(new FlatMapFunction<OrderPromotionJoin, FactFullReduceOrderUser>() {
                    @Override
                    public void flatMap(OrderPromotionJoin value, Collector<FactFullReduceOrderUser> out) throws Exception {
                        FactFullReduceOrderUser factFullReduceOrderUser = new FactFullReduceOrderUser();
                        factFullReduceOrderUser.shopId = value.odsEmallOrder.getShopId();
                        factFullReduceOrderUser.promotionId = value.orderPromotion.getPromotionId();
                        factFullReduceOrderUser.payNumber = 1;

                        out.collect(factFullReduceOrderUser);
                    }
                })
                .groupBy(new KeySelector<FactFullReduceOrderUser, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactFullReduceOrderUser value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionId);
                    }
                })
                .reduce(new OrderFunction());


        //连接最后结果
        fullReduceUvReduceDs
                .fullOuterJoin(fullReduceOrderUserDs)
                .where(new KeySelector<FactFullReduceUv, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactFullReduceUv value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionId);
                    }
                })
                .equalTo(new KeySelector<FactFullReduceOrderUser, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactFullReduceOrderUser value) throws Exception {
                        return new Tuple2<>(value.shopId, value.promotionId);
                    }
                })
                .with(new JoinFunction<FactFullReduceUv, FactFullReduceOrderUser, FactFullReduceAll>() {
                    @Override
                    public FactFullReduceAll join(FactFullReduceUv first, FactFullReduceOrderUser second) throws Exception {
                        FactFullReduceAll factFullReduceAll = new FactFullReduceAll();
                        factFullReduceAll.setRowDate(LocalDate.now().toString());
                        if (first != null) {
                            factFullReduceAll.setShopId(first.shopId);
                            factFullReduceAll.setPromotionId(first.promotionId);
                            factFullReduceAll.setUv(first.uv);
                        } else {
                            factFullReduceAll.setUv(0);
                        }

                        if (second != null) {
                            factFullReduceAll.setShopId(second.shopId);
                            factFullReduceAll.setPromotionId(second.promotionId);
                            factFullReduceAll.setPayNumber(second.payNumber);
                        } else {
                            factFullReduceAll.setPayNumber(0);
                        }

                        return factFullReduceAll;
                    }
                })
                .filter(new FilterFunction<FactFullReduceAll>() {
                    @Override
                    public boolean filter(FactFullReduceAll value) throws Exception {
                        return value.getShopId() != null && value.getPromotionId() != null;
                    }
                })
                .output(outputFormat);

    }


    private static class OrderPromotionJoin {
        private OdsEmallOrder odsEmallOrder;
        private OdsOrderPromotion orderPromotion;
    }

    /**
     * 累积付款人数
     */
    public static class FactFullReduceOrderUser {

        public int shopId;

        public int promotionId;

        /**
         * 累计付款人数
         */
        public int payNumber = 0;

    }


    /**
     * 累积uv
     */
    public static class FactFullReduceUv {

        public int shopId;

        public int promotionId;

        /**
         * 累计uv
         */
        public int uv = 0;

    }

    public static class OrderFunction implements ReduceFunction<FactFullReduceOrderUser> {


        @Override
        public FactFullReduceOrderUser reduce(FactFullReduceOrderUser value1, FactFullReduceOrderUser value2) throws Exception {
            FactFullReduceOrderUser factFullReduceOrderUser = new FactFullReduceOrderUser();
            factFullReduceOrderUser.shopId = value1.shopId;
            factFullReduceOrderUser.promotionId = value1.promotionId;
            factFullReduceOrderUser.payNumber = value1.payNumber + value2.payNumber;

            return factFullReduceOrderUser;
        }
    }


    public static class UvFunction implements ReduceFunction<FactFullReduceUv> {


        @Override
        public FactFullReduceUv reduce(FactFullReduceUv value1, FactFullReduceUv value2) throws Exception {
            FactFullReduceUv factFullReduceUv = new FactFullReduceUv();
            factFullReduceUv.shopId = value1.shopId;
            factFullReduceUv.promotionId = value1.promotionId;
            factFullReduceUv.uv = value1.uv + value2.uv;

            return factFullReduceUv;
        }
    }

}
