package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberPromotion;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderItem;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderPromotion;
import cn.yizhi.yzt.pipeline.udf.UdfLongToTimestamp;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.types.Row;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author hucheng
 * @date 2020/7/28 22:22
 */
public class IndexStreamJob extends StreamJob {

    @Override
    public void defineJob() throws Exception {
        //注册sql文件
        this.registerSql("fact-index.yml");


        // 注册UDF
        this.registerUdf("udf_to_timestamp", new UdfLongToTimestamp());

        //注册表
        this.createTableFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_PROMOTION, OdsOrderPromotion.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_ITEM, OdsOrderItem.class,true);

        //清洗emall_order,避免其他无关数据的变化导致结果错误(下单和支付)
        tableEnv.createTemporaryView("emall_order_pay", this.sqlQuery("factMemberOrderClean"));

        //活动指标计算
        SingleOutputStreamOperator<FactMemberPromotion> orderPromotionQuery = tableEnv.toRetractStream(this.sqlQuery("orderPromotionQuery"), Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactMemberPromotion>() {
            @Override
            public FactMemberPromotion map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactMemberPromotion(value.f1);
                }
                return null;

            }
        }).filter(new FilterFunction<FactMemberPromotion>() {
            @Override
            public boolean filter(FactMemberPromotion value) throws Exception {
                return value != null;
            }
        });

        //写入数据到kafka
        toKafkaSink(orderPromotionQuery,"fact_member_promotion");
    }


    private static FactMemberPromotion convertToFactMemberPromotion(Row value) {
        FactMemberPromotion factMemberPromotion = new FactMemberPromotion();

        factMemberPromotion.setShopId((Integer) value.getField(0));
        factMemberPromotion.setChannel((String) value.getField(1));
        factMemberPromotion.setMemberId((Integer) value.getField(2));
        factMemberPromotion.setPromotionType(value.getField(3) != null ? (Integer) value.getField(3) : 0);
        factMemberPromotion.setPromotionId(value.getField(4) != null ? (Integer) value.getField(4) : 0);
        factMemberPromotion.setOrderCount((Long) value.getField(5));
        factMemberPromotion.setPromotionCount((Long) value.getField(6));
        factMemberPromotion.setOriginalOrderTime(value.getField(7) != null ? Timestamp.valueOf((LocalDateTime) value.getField(7)) : null);
        factMemberPromotion.setOriginalOrderPromotionTime(value.getField(8) != null ? Timestamp.valueOf((LocalDateTime) value.getField(8)) : null);
        factMemberPromotion.setTotalAmount((BigDecimal) value.getField(9));

        return factMemberPromotion;
    }
}
