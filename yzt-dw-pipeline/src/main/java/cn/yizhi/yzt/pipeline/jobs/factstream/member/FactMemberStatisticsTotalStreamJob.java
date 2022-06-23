
package cn.yizhi.yzt.pipeline.jobs.factstream.member;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.function.FilterPaidOrderFunction;
import cn.yizhi.yzt.pipeline.function.FilterRefundFunction;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderItem;
import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberLastConsumeService;
import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberTotal;
import cn.yizhi.yzt.pipeline.model.fact.member.MgFactMemberOrderLastAddr;
import cn.yizhi.yzt.pipeline.model.fact.member.order.FactMemberOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderConsignee;
import cn.yizhi.yzt.pipeline.model.ods.OdsPickupAddress;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @Author: HuCheng
 * 会员商品指标-天
 * @Date: 2021/1/5 14:22
 */
public class FactMemberStatisticsTotalStreamJob extends StreamJob {
    //3天清除
    private static final StateTtlConfig ttlConfig = StateTtlConfig
            .newBuilder(org.apache.flink.api.common.time.Time.days(3))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    //400天
    private static final StateTtlConfig groupConfig = StateTtlConfig
            .newBuilder(org.apache.flink.api.common.time.Time.days(400))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();


    @Override
    public void defineJob() throws Exception {
        //用户统计
        memberTotalAndOrder();

    }


    private void memberTotalAndOrder() {
        //注册sql文件
        this.registerSql("fact-member.yml");

        //接收订单信息
        DataStream<OdsEmallOrder> odsEmallOrderDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsEmallOrder>() {

                    @Override
                    public long extractAscendingTimestamp(OdsEmallOrder element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                }).map(new MapFunction<OdsEmallOrder, OdsEmallOrder>() {
                    @Override
                    public OdsEmallOrder map(OdsEmallOrder value) throws Exception {
                        if ("wxweb".equals(value.getSource())) {
                            value.setSource("web");
                        }
                        return value;
                    }
                });

        //接收订单和商品的关联数据
        DataStream<FactOrderItem> factOrderItemDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_ITEM, FactOrderItem.class)
                .filter(new FilterFunction<FactOrderItem>() {
                    @Override
                    public boolean filter(FactOrderItem value) throws Exception {
                        // * 发出的订单状态统一为 0为支付  1已支付  2已退款（暂时无）
                        return value.getStatus() == 1;
                    }
                });

        //支付
        DataStream<OdsEmallOrder> filterEmallOrderPaid = odsEmallOrderDataStream
                .keyBy(new KeySelector<OdsEmallOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new FilterPaidOrderFunction())
                .uid("mg-order-filter-paid")
                .name("mg-order-filter-paid");


        //注册自提信息表
        this.createTableFromJdbc("ods_pickup_address", "ods_pickup_address", OdsPickupAddress.class);
        this.createTableFromKafka(SourceTopics.TOPIC_ORDER_CONSIGNEE, OdsOrderConsignee.class, true);
        streamToTable(FactOrderItem.class, factOrderItemDataStream, true);


        //计算上次消费服务
        DataStream<FactMemberLastConsumeService> factMemberLastConsumeServiceDs = factOrderItemDataStream
                .keyBy(new KeySelector<FactOrderItem, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactOrderItem value) throws Exception {
                        return new Tuple2<>(value.getShopId(), value.getMemberId());
                    }
                })
                .process(new LastConsumeProcessFunction())
                .uid("mg-order-last-consume")
                .name("mg-order-last-consume");


        //上次收货地址
        DataStream<MgFactMemberOrderLastAddr> factMemberOrderLastAddr = tableEnv.toRetractStream(this.sqlQuery("factMemberOrderLastAddrV2"), Row.class)
                .map(new MapFunction<Tuple2<Boolean, Row>, MgFactMemberOrderLastAddr>() {
                    @Override
                    public MgFactMemberOrderLastAddr map(Tuple2<Boolean, Row> value) throws Exception {
                        if (value.f0) {
                            return convertToMgFactMemberOrderLastAddr(value.f1);
                        }
                        return null;
                    }
                }).filter(new FilterFunction<MgFactMemberOrderLastAddr>() {
                    @Override
                    public boolean filter(MgFactMemberOrderLastAddr value) throws Exception {
                        //处理为空值情况。
                        return value != null || StringUtils.isNotBlank(value.getLastConsigneeAddress());
                    }
                });

        //退款
        DataStream<OdsEmallOrder> filterEmallOrderRefund = odsEmallOrderDataStream
                .keyBy(new KeySelector<OdsEmallOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new FilterRefundFunction())
                .uid("mg-order-filter-refund")
                .name("mg-order-filter-refund");

        //合并流
        DataStream<OdsEmallOrder> unionStream = filterEmallOrderPaid.union(filterEmallOrderRefund);

        DataStream<FactMemberTotal> userTotalDs = unionStream
                .keyBy(new KeySelector<OdsEmallOrder, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple2<>(value.getShopId(), value.getMemberId());
                    }
                })
                .process(new PaidProcessFunction())
                .uid("mg-member-order-total")
                .name("mg-member-order-total");


        SingleOutputStreamOperator<FactMemberOrder> factMemberOrder = unionStream.keyBy(new KeySelector<OdsEmallOrder, Tuple3<String, Integer, Integer>>() {
            @Override
            public Tuple3<String, Integer, Integer> getKey(OdsEmallOrder element) throws Exception {
                Timestamp eventTime = (element.getUpdatedAt() != null) ?
                        element.getUpdatedAt() : element.getCreatedAt();

                return new Tuple3<>(TimeUtil.convertTimeStampToDay(eventTime), element.getShopId(), element.getMemberId());
            }
        }).process(new MemberOrderProcessFunction())
                .uid("mg-fact-member-order-count")
                .name("mg-fact-member-order-count");


        toKafkaSink(userTotalDs, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);

        toKafkaSink(factMemberOrder, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);

        //写入mysql
        toJdbcUpsertSink(userTotalDs, Tables.SINK_TABLE_MEMBER_FACT_UNION, FactMemberTotal.class);

        toJdbcUpsertSink(factMemberOrder, Tables.SINK_TABLE_MEMBER_ORDER, FactMemberOrder.class);

        toJdbcUpsertSink(factMemberOrderLastAddr, Tables.SINK_TABLE_MEMBER_FACT_UNION, MgFactMemberOrderLastAddr.class);

        toJdbcUpsertSink(factMemberLastConsumeServiceDs, Tables.SINK_TABLE_MEMBER_FACT_UNION, FactMemberLastConsumeService.class);
    }


    public static class MemberOrderProcessFunction extends KeyedProcessFunction<Tuple3<String, Integer, Integer>, OdsEmallOrder, FactMemberOrder> {


        //下单的暂时不处理。
        private transient ValueState<Integer> orderCountState;
        private transient ValueState<BigDecimal> orderAmountState;


        private transient ValueState<Integer> paidCountState;
        private transient ValueState<BigDecimal> paidAmountState;

        private transient ValueState<Integer> refundCountState;
        private transient ValueState<BigDecimal> refundAmountState;


        private transient MapState<Long, Long> orderIdsState;
        private transient MapState<Long, Long> refundIdsState;


        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> orderTimesStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderCountState",
                    // type information
                    Types.INT);
            orderTimesStateDescriptor.enableTimeToLive(ttlConfig);
            orderCountState = getRuntimeContext().getState(orderTimesStateDescriptor);

            ValueStateDescriptor<BigDecimal> orderAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderAmountState",
                    // type information
                    Types.BIG_DEC);
            orderAmountStateDescriptor.enableTimeToLive(ttlConfig);
            orderAmountState = getRuntimeContext().getState(orderAmountStateDescriptor);


            ValueStateDescriptor<Integer> paidCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidCountState",
                    // type information
                    Types.INT);
            paidCountStateDescriptor.enableTimeToLive(ttlConfig);
            paidCountState = getRuntimeContext().getState(paidCountStateDescriptor);

            ValueStateDescriptor<BigDecimal> paidAmountStateStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidAmountState",
                    // type information
                    Types.BIG_DEC);
            paidAmountStateStateDescriptor.enableTimeToLive(ttlConfig);
            paidAmountState = getRuntimeContext().getState(paidAmountStateStateDescriptor);


            ValueStateDescriptor<Integer> refundCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundCountState",
                    // type information
                    Types.INT);
            refundCountStateDescriptor.enableTimeToLive(ttlConfig);
            refundCountState = getRuntimeContext().getState(refundCountStateDescriptor);

            ValueStateDescriptor<BigDecimal> refundAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundAmountState",
                    // type information
                    Types.BIG_DEC);
            refundAmountStateDescriptor.enableTimeToLive(ttlConfig);
            refundAmountState = getRuntimeContext().getState(refundAmountStateDescriptor);


            MapStateDescriptor<Long, Long> orderIdsStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "orderIdsState",
                    // type information,
                    Types.LONG,
                    Types.LONG);
            orderIdsStateDescriptor.enableTimeToLive(ttlConfig);
            orderIdsState = getRuntimeContext().getMapState(orderIdsStateDescriptor);

            MapStateDescriptor<Long, Long> refundIdsStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "refundIdsState",
                    // type information,
                    Types.LONG,
                    Types.LONG);
            refundIdsStateDescriptor.enableTimeToLive(ttlConfig);
            refundIdsState = getRuntimeContext().getMapState(refundIdsStateDescriptor);


        }

        @Override
        public void processElement(OdsEmallOrder value, Context ctx, Collector<FactMemberOrder> out) throws Exception {


            Integer paidCount = paidCountState.value();
            BigDecimal paidAmount = paidAmountState.value();


            Integer refundCount = refundCountState.value();
            BigDecimal refundAmount = refundAmountState.value();


            //支付
            if (value.getTransactionNo() != null && value.getStatus() != 9) {
                if (paidCount == null) {
                    paidCountState.update(1);
                } else {
                    paidCountState.update(paidCount + 1);
                }

                if (paidAmount == null) {
                    paidAmountState.update(value.getActualAmount());
                } else {
                    paidAmountState.update(paidAmount.add(value.getActualAmount()));
                }

                orderIdsState.put(value.getId(), value.getId());

            } else {
                //退款
                if (refundCount == null) {
                    refundCountState.update(1);
                } else {
                    refundCountState.update(refundCount + 1);
                }

                if (refundAmount == null) {
                    refundAmountState.update(value.getActualAmount());
                } else {
                    refundAmountState.update(refundAmount.add(value.getActualAmount()));
                }

                refundIdsState.put(value.getId(), value.getId());
            }


            FactMemberOrder memberOrder = new FactMemberOrder();

            memberOrder.setMemberId(ctx.getCurrentKey().f2);
            memberOrder.setShopId(ctx.getCurrentKey().f1);
            memberOrder.setAnalysisDate(ctx.getCurrentKey().f0);

            memberOrder.setOrderCount(orderCountState.value() != null ? orderCountState.value() : 0);
            memberOrder.setOrderAmount(orderAmountState.value() != null ? orderAmountState.value() : BigDecimal.ZERO);

            memberOrder.setPaidCount(paidCountState.value() != null ? paidCountState.value() : 0);
            memberOrder.setPaidAmount(paidAmountState.value() != null ? paidAmountState.value() : BigDecimal.ZERO);

            memberOrder.setRefundCount(refundCountState.value() != null ? refundCountState.value() : 0);
            memberOrder.setRefundAmount(refundAmountState.value() != null ? refundAmountState.value() : BigDecimal.ZERO);

            if (refundIdsState != null) {
                Stream<Long> stream = StreamSupport.stream(refundIdsState.values().spliterator(), false);
                List<Long> refundIds = stream.collect(Collectors.toList());
                memberOrder.setRefundIds(refundIds.toString());
            }

            if (orderIdsState != null) {
                Stream<Long> stream = StreamSupport.stream(orderIdsState.values().spliterator(), false);
                List<Long> orderIds = stream.collect(Collectors.toList());
                memberOrder.setOrderIds(orderIds.toString());
            }

            memberOrder.setDataType(DataType.MEMBER_ORDER);
            out.collect(memberOrder);
        }
    }


    public static class PaidProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, OdsEmallOrder, FactMemberTotal> {
        private transient ValueState<Integer> orderTimesState;
        private transient ValueState<BigDecimal> orderAmountState;
        private transient ValueState<Timestamp> firstOrderTimeState;
        private transient ValueState<Timestamp> lastOrderTimeState;
        private transient ValueState<Integer> refundTimesState;
        private transient ValueState<BigDecimal> refundAmountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> orderTimesStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderTimesState",
                    // type information
                    Types.INT);
            orderTimesState = getRuntimeContext().getState(orderTimesStateDescriptor);

            ValueStateDescriptor<BigDecimal> orderAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderAmountState",
                    // type information
                    Types.BIG_DEC);
            orderAmountState = getRuntimeContext().getState(orderAmountStateDescriptor);


            ValueStateDescriptor<Timestamp> firstOrderTimeDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "firstOrderTimeState",
                    // type information
                    Types.SQL_TIMESTAMP);
            firstOrderTimeState = getRuntimeContext().getState(firstOrderTimeDescriptor);

            ValueStateDescriptor<Timestamp> lastOrderTimeStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "lastOrderTimeState",
                    // type information
                    Types.SQL_TIMESTAMP);
            lastOrderTimeState = getRuntimeContext().getState(lastOrderTimeStateDescriptor);

            ValueStateDescriptor<Integer> refundTimesStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundTimesState",
                    // type information
                    Types.INT);
            refundTimesState = getRuntimeContext().getState(refundTimesStateDescriptor);

            ValueStateDescriptor<BigDecimal> refundAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundAmountState",
                    // type information
                    Types.BIG_DEC);
            refundAmountState = getRuntimeContext().getState(refundAmountStateDescriptor);
        }

        @Override
        public void processElement(OdsEmallOrder value, Context ctx, Collector<FactMemberTotal> out) throws Exception {
            Integer orderTimes = orderTimesState.value();
            BigDecimal orderAmount = orderAmountState.value();
            Timestamp firstOrderTime = firstOrderTimeState.value();

            Integer refundTimes = refundTimesState.value();
            BigDecimal refundAmount = refundAmountState.value();

            FactMemberTotal userTotal = new FactMemberTotal();
            Tuple2<Integer, Integer> currentKey = ctx.getCurrentKey();
            userTotal.setShopId(currentKey.f0);
            userTotal.setId(currentKey.f1);

            //支付
            if (value.getTransactionNo() != null && value.getStatus() != 9) {
                if (orderTimes == null) {
                    orderTimesState.update(1);
                } else {
                    orderTimesState.update(orderTimes + 1);
                }

                if (orderAmount == null) {
                    orderAmountState.update(value.getActualAmount());
                } else {
                    orderAmountState.update(orderAmount.add(value.getActualAmount()));
                }

                if (firstOrderTime == null) {
                    firstOrderTimeState.update(value.getPaidAt());
                }
                lastOrderTimeState.update(value.getPaidAt());
            } else {
                //退款
                if (refundTimes == null) {
                    refundTimesState.update(1);
                } else {
                    refundTimesState.update(refundTimes + 1);
                }

                if (refundAmount == null) {
                    refundAmountState.update(value.getActualAmount());
                } else {
                    refundAmountState.update(refundAmount.add(value.getActualAmount()));
                }
            }


            userTotal.setOrderCount(orderTimesState.value() != null ? orderTimesState.value() : 0);
            userTotal.setTotalOrderAmount(orderAmountState.value() != null ? orderAmountState.value() : BigDecimal.ZERO);
            userTotal.setRefundCount(refundTimesState.value() != null ? refundTimesState.value() : 0);
            userTotal.setRefundAmount(refundAmountState.value() != null ? refundAmountState.value() : BigDecimal.ZERO);
            userTotal.setLastOrderTime(lastOrderTimeState.value());
            userTotal.setFirstOrderTime(firstOrderTimeState.value());
            if (userTotal.getOrderCount() != 0) {
                userTotal.setAvgConsumeAmount(userTotal.getTotalOrderAmount().divide(new BigDecimal(userTotal.getOrderCount()), 2, RoundingMode.HALF_UP));
            } else {
                userTotal.setAvgConsumeAmount(BigDecimal.ZERO);
            }
            userTotal.calPurchaseRate(userTotal);
            userTotal.setDataType(DataType.MEMBER_TOTAL);

            out.collect(userTotal);
        }
    }

    //计算上次消费服务
    public static class LastConsumeProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, FactOrderItem, FactMemberLastConsumeService> {
        private transient MapState<Long, List<String>> lastConsumeService;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            MapStateDescriptor<Long, List<String>> lastConsumeServiceDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "lastConsumeService",
                    // type information,
                    Types.LONG,
                    Types.LIST(Types.STRING));
            lastConsumeServiceDescriptor.enableTimeToLive(ttlConfig);
            lastConsumeService = getRuntimeContext().getMapState(lastConsumeServiceDescriptor);
        }

        @Override
        public void processElement(FactOrderItem value, Context ctx, Collector<FactMemberLastConsumeService> out) throws Exception {
            List<String> products;
            if (lastConsumeService.contains(value.getOrderId())) {
                products = lastConsumeService.get(value.getOrderId());
            } else {
                products = new ArrayList<>();
            }
            //商品名==数量
            products.add(value.getName() + "==" + value.getQuantity());
            lastConsumeService.put(value.getOrderId(), products);

            Tuple2<Integer, Integer> currentKey = ctx.getCurrentKey();
            FactMemberLastConsumeService factMemberLastConsumeService = new FactMemberLastConsumeService();
            factMemberLastConsumeService.setShopId(currentKey.f0);
            factMemberLastConsumeService.setId(currentKey.f1);
            factMemberLastConsumeService.setLastConsumeServices(convertToJson(lastConsumeService.get(value.getOrderId())));

            out.collect(factMemberLastConsumeService);
        }
    }

    /**
     * 把上次消费服务转为json格式
     *
     * @param products
     * @return
     */
    private static String convertToJson(List<String> products) {
        if (products == null || products.size() <= 0) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();
        for (String s : products) {
            JSONObject jsonObject = new JSONObject();
            String[] lastConsumeService = s.split("==");
            jsonObject.put("count", Integer.valueOf(lastConsumeService[1]));
            jsonObject.put("name", lastConsumeService[0]);
            jsonArray.put(jsonObject);
        }
        return jsonArray.toString();
    }

    /**
     * row 转FactMemberOrderLastAddr
     *
     * @param value
     * @return
     */
    private static MgFactMemberOrderLastAddr convertToMgFactMemberOrderLastAddr(Row value) {
        MgFactMemberOrderLastAddr factMemberOrderLastAddr = new MgFactMemberOrderLastAddr();

        factMemberOrderLastAddr.setShopId((Integer) value.getField(0));
        factMemberOrderLastAddr.setId((Integer) value.getField(1));
        factMemberOrderLastAddr.setLastConsigneeAddress((String) value.getField(2));

        return factMemberOrderLastAddr;
    }
}
