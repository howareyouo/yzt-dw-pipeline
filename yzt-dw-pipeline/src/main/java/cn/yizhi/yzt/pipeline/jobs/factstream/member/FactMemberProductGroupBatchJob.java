package cn.yizhi.yzt.pipeline.jobs.factstream.member;

import cn.yizhi.yzt.pipeline.common.PackType;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.member.FactOrderItemPack;
import cn.yizhi.yzt.pipeline.model.fact.member.FactPackOdsLog;
import cn.yizhi.yzt.pipeline.model.fact.member.FactProductReGroup;
import cn.yizhi.yzt.pipeline.model.fact.member.OdsProductReGroup;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;

/**
 * @Author: HuCheng
 * @Date: 2021/2/3 16:49
 */
public class FactMemberProductGroupBatchJob extends StreamJob {

    @Override
    public void defineJob() throws Exception {
        //注册sql文件
        this.registerSql("fact-product-re-group.yml");

        //注册表
        this.createTableFromJdbc("ods_product_re_group", "ods_product_re_group", OdsProductReGroup.class);

        //初始化规则数据
        Table factProductReGroupQuery = this.sqlQuery("factProductReGroupQuery");
        DataStream<FactProductReGroup> tableReGroupStream = tableEnv.toAppendStream(factProductReGroupQuery, FactProductReGroup.class);


        //包装初始化规则-logStream
        DataStream<FactPackOdsLog> tableProductRefundDs = tableReGroupStream
                .map(new MapFunction<FactProductReGroup, FactPackOdsLog>() {
                    @Override
                    public FactPackOdsLog map(FactProductReGroup value) throws Exception {
                        FactPackOdsLog f = new FactPackOdsLog();
                        f.setPackType(PackType.PACKING_DATA);
                        f.setGroupId(value.getGroup_id());
                        f.setShopId(value.getShop_id());
                        f.setGoodsId(value.getProduct_id());
                        return f;
                    }
                });

        //包装初始化规则-订单
        DataStream<FactOrderItemPack> tablePackOrderItem = tableReGroupStream
                .map(new MapFunction<FactProductReGroup, FactOrderItemPack>() {
                    @Override
                    public FactOrderItemPack map(FactProductReGroup value) throws Exception {
                        FactOrderItemPack f = new FactOrderItemPack();
                        f.setPackType(PackType.PACKING_DATA);
                        f.setGroupId(value.getGroup_id());
                        f.setShopId(value.getShop_id());
                        f.setProductId(value.getProduct_id());
                        return f;
                    }
                });

        //table数据写入到 聚合topic
        toKafkaSink(tablePackOrderItem, SourceTopics.TOPIC_FACT_PACK_ORDER_ITEM);
        toKafkaSink(tableProductRefundDs, SourceTopics.TOPIC_FACT_PACK_ODS_LOG);
    }
}
