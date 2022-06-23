package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.member.*;
import cn.yizhi.yzt.pipeline.model.ods.*;
import cn.yizhi.yzt.pipeline.model.fact.member.*;
import cn.yizhi.yzt.pipeline.model.ods.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.types.Row;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author hucheng
 * @date 2020/7/1 17:12
 */
public class FactMemberStreamJob extends StreamJob {
    @Override
    public void defineJob() throws Exception {
        //注册sql文件
        this.registerSql("fact-member.yml");
        //注册表
        this.createTableFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, OdsLogStream.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_MEMBER_EXTEND, MemberExtend.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_REFUND, OdsRefund.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_CONSIGNEE, OdsOrderConsignee.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_PROMOTION, OdsOrderPromotion.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_SHOP_MEMBER, ShopMember.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_ITEM, OdsOrderItem.class, true);
        this.createTableFromKafka(SourceTopics.TOPIC_PRODUCT_SKU, ProductSku.class, true);

        //注册订单表
        this.createTableFromJdbc("ods_emall_order", "ods_emall_order", OdsEmallOrder.class);

        //注册自提信息表
        this.createTableFromJdbc("ods_pickup_address", "ods_pickup_address", OdsPickupAddress.class);

        //注册factMember表（只有退费相关字段）
        this.createTableFromJdbc("fact_member", "fact_member", FactMemberRefund.class);

        //已支付订单清洗
        tableEnv.createTemporaryView("emall_order_pay",this.sqlQuery("factMemberOrderClean"));

        //退款
        tableEnv.createTemporaryView("refund", tableEnv.sqlQuery("select shopId,orderId,refundAmount,status,refundedAt,proctime from odsRefund  where `status` = 7 "));


        //支付订单信息
        SingleOutputStreamOperator<FactMemberOrderOrderPay> factMemberOrderPay = tableEnv.toRetractStream(this.sqlQuery("factMemberOrderPay"), Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactMemberOrderOrderPay>() {
            @Override
            public FactMemberOrderOrderPay map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactMemberOrder(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactMemberOrderOrderPay>() {
            @Override
            public boolean filter(FactMemberOrderOrderPay value) throws Exception {
                return value != null;
            }
        });

        //上次收货地址
        SingleOutputStreamOperator<FactMemberOrderLastAddr> factMemberOrderLastAddr = tableEnv.toRetractStream(this.sqlQuery("factMemberOrderLastAddr"), Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactMemberOrderLastAddr>() {
            @Override
            public FactMemberOrderLastAddr map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactMemberOrderLastAddr(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactMemberOrderLastAddr>() {
            @Override
            public boolean filter(FactMemberOrderLastAddr value) throws Exception {
                return value != null;
            }
        });

        //上次消费服务
        SingleOutputStreamOperator<FactMemberOrderLastServices> factMemberOrderLastServices = tableEnv.toRetractStream(this.sqlQuery("factMemberOrderLastServices"), Row.class).map(new MapFunction<Tuple2<Boolean, Row>, FactMemberOrderLastServices>() {
            @Override
            public FactMemberOrderLastServices map(Tuple2<Boolean, Row> value) throws Exception {
                if (value.f0) {
                    return convertToFactMemberOrderLastServices(value.f1);
                }
                return null;
            }
        }).filter(new FilterFunction<FactMemberOrderLastServices>() {
            @Override
            public boolean filter(FactMemberOrderLastServices value) throws Exception {
                return value != null;
            }
        });

        //退款消息
        DataStream<FactMemberRefund> factMemberRefundDataStream = tableEnv.toAppendStream(this.sqlQuery("factMemberRefund"), FactMemberRefund.class);

        //

        //更新会员相关信息
        toJdbcUpsertSink(factMemberOrderPay,"fact_member", FactMemberOrderOrderPay.class);
        toJdbcUpsertSink(factMemberOrderLastAddr,"fact_member",FactMemberOrderLastAddr.class);
        toJdbcUpsertSink(factMemberOrderLastServices,"fact_member",FactMemberOrderLastServices.class);
        toJdbcUpsertSink(factMemberRefundDataStream,"fact_member",FactMemberRefund.class);
    }

    /**
     * row 转FactMemberExport
     *
     * @param value
     * @return
     */
    private static FactMemberExport factMemberExport(Row value) {
        FactMemberExport factMemberExport = new FactMemberExport();

        factMemberExport.setShopId((Integer) value.getField(0));
        factMemberExport.setMemberId((Integer) value.getField(1));
        factMemberExport.setBalanceAmount((BigDecimal) value.getField(2));
        factMemberExport.setGiftAmount((BigDecimal) value.getField(3));
        factMemberExport.setPoints((BigDecimal) value.getField(4));
        factMemberExport.setDebtAmount((BigDecimal) value.getField(5));
        factMemberExport.setLastViewShopTime(value.getField(6) != null ? Timestamp.valueOf((LocalDateTime) value.getField(6)) : null);
        factMemberExport.setReferrer((String) value.getField(7));
        factMemberExport.setFollower((String) value.getField(8));
        factMemberExport.setCreatedAt(value.getField(9) != null ? Timestamp.valueOf((LocalDateTime) value.getField(9)) : null);
        factMemberExport.setLastConsumeServices((String) value.getField(10));
        factMemberExport.setOrderCountExport((Integer) value.getField(11));
        factMemberExport.setTotalOrderAmountExport((BigDecimal) value.getField(12));
        factMemberExport.setLastConsigneeAddress((String) value.getField(13));
        factMemberExport.setLastOrderTime(value.getField(14) != null ? Timestamp.valueOf((LocalDateTime) value.getField(14)) : null);

        return factMemberExport;
    }

    /**
     * row 转FactMemberOrderPay
     *
     * @param value
     * @return
     */
    private static FactMemberOrderOrderPay convertToFactMemberOrder(Row value) {
        FactMemberOrderOrderPay factMemberOrderOrderPay = new FactMemberOrderOrderPay();

        factMemberOrderOrderPay.setShopId((Integer) value.getField(0));
        factMemberOrderOrderPay.setMemberId((Integer) value.getField(1));
        factMemberOrderOrderPay.setOrderCount((Long) value.getField(2));
        factMemberOrderOrderPay.setTotalOrderAmount((BigDecimal) value.getField(3));

        return factMemberOrderOrderPay;
    }

    /**
     * row 转FactMemberOrderLastAddr
     *
     * @param value
     * @return
     */
    private static FactMemberOrderLastAddr convertToFactMemberOrderLastAddr(Row value) {
        FactMemberOrderLastAddr factMemberOrderLastAddr = new FactMemberOrderLastAddr();

        factMemberOrderLastAddr.setShopId((Integer) value.getField(0));
        factMemberOrderLastAddr.setMemberId((Integer) value.getField(1));
        factMemberOrderLastAddr.setLastOrderTime(Timestamp.valueOf((LocalDateTime) value.getField(2)));
        factMemberOrderLastAddr.setLastOrderId((Long) value.getField(3));
        factMemberOrderLastAddr.setLastConsigneeAddress((String) value.getField(4));

        return factMemberOrderLastAddr;
    }

    /**
     * row 转FactMemberOrderLastServices
     *
     * @param value
     * @return
     */
    private static FactMemberOrderLastServices convertToFactMemberOrderLastServices(Row value) {
        FactMemberOrderLastServices factMemberOrderLastServices = new FactMemberOrderLastServices();

        factMemberOrderLastServices.setShopId((Integer) value.getField(0));
        factMemberOrderLastServices.setMemberId((Integer) value.getField(1));
        factMemberOrderLastServices.setLastConsumeServices(convertToJson((String) value.getField(2)));

        return factMemberOrderLastServices;
    }

    /**
     * row 转factMemberLastViewShop
     */
    private static FactMemberViewShop convertToFactViewShop(Row value){
        FactMemberViewShop factMemberViewShop = new FactMemberViewShop();

        factMemberViewShop.setShopId((Integer) value.getField(0));
        factMemberViewShop.setMemberId((Integer) value.getField(1));
        factMemberViewShop.setLastViewShopTime(Timestamp.valueOf((LocalDateTime) value.getField(2)));

        return factMemberViewShop;
    }

//    private static FactMemberRefund convertToFactRefund(Row value){
//        FactMemberRefund factMemberRefund = new FactMemberRefund();
//
//        factMemberRefund.setShopId((Integer) value.getField(0));
//
//    }

    /**
     * 把上次消费服务转为json格式
     *
     * @param row
     * @return
     */
    private static String convertToJson(String row) {
        //不是拼接数据直接返回
        logger.info("上次消费的服务: {}", row);
        if (row.contains("==")) {
            JSONArray jsonArray = new JSONArray();
            String[] split = row.split(",");
            for (String s : split) {
                JSONObject jsonObject = new JSONObject();
                String[] lastConsumeService = s.split("==");
                jsonObject.put("count", Integer.valueOf(lastConsumeService[1]));
                jsonObject.put("name", lastConsumeService[0]);
                jsonArray.put(jsonObject);
            }

            return jsonArray.toString();
        }
        return row;
    }
}
