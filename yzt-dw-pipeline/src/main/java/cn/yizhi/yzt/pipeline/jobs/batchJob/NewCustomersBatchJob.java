package cn.yizhi.yzt.pipeline.jobs.batchJob;

import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberFirstOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderPromotion;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.Table;
import org.apache.flink.types.Row;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author hucheng
 * 新老客计算，每天凌晨跑历史数据
 * @date 2020/8/25 16:12
 */
public class NewCustomersBatchJob extends StreamJob {
    @Override
    public void defineJob() throws Exception {
        //注册sql文件
        this.registerSql("fact-member.yml");
        //注册订单表
        this.createTableFromJdbc("ods_emall_order", "ods_emall_order", OdsEmallOrder.class);
        this.createTableFromJdbc("ods_order_promotion", "ods_order_promotion", OdsOrderPromotion.class);

        //计算历史数据
        Table factMemberOderHistory = this.sqlQuery("factNewCustomersQuery");

        //活动指标计算
        DataStream<FactMemberFirstOrder> orderPromotionQuery = tableEnv.toRetractStream(factMemberOderHistory, Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactMemberFirstOrder>() {
            @Override
            public FactMemberFirstOrder map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactMemberFirstOrder(value.f1);
                }
                return null;

            }
        }).filter(new FilterFunction<FactMemberFirstOrder>() {
            @Override
            public boolean filter(FactMemberFirstOrder value) throws Exception {
                return value != null;
            }
        });

        //写入数据到mysql
        this.toJdbcUpsertSink(orderPromotionQuery, "fact_member_first_order", FactMemberFirstOrder.class);
    }

    private static FactMemberFirstOrder convertToFactMemberFirstOrder(Row value) {
        FactMemberFirstOrder factMemberFirstOrder = new FactMemberFirstOrder();

        factMemberFirstOrder.setOrderId((Long) value.getField(0));
        factMemberFirstOrder.setMainShopId((Integer) value.getField(1));
        factMemberFirstOrder.setShopId((Integer) value.getField(2));
        factMemberFirstOrder.setUnionNo((String) value.getField(3));
        factMemberFirstOrder.setOrderNo((String) value.getField(4));
        factMemberFirstOrder.setTransactionNo((String) value.getField(5));
        factMemberFirstOrder.setPaymentMethod((String) value.getField(6));
        factMemberFirstOrder.setSource((String) value.getField(7));
        factMemberFirstOrder.setPromotionType(value.getField(8) != null ? (Integer) value.getField(8) : 0);
        factMemberFirstOrder.setPromotionId(value.getField(9) != null ? (Integer) value.getField(9) : 0);
        factMemberFirstOrder.setMemberId((Integer) value.getField(10));
        factMemberFirstOrder.setPaidAt(Timestamp.valueOf((LocalDateTime) value.getField(11)));
        factMemberFirstOrder.setCreatedAt(Timestamp.valueOf((LocalDateTime) value.getField(12)));

        return factMemberFirstOrder;
    }
}
