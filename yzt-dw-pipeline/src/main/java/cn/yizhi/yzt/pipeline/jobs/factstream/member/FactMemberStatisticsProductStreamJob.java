
package cn.yizhi.yzt.pipeline.jobs.factstream.member;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.common.EventType;
import cn.yizhi.yzt.pipeline.common.PackType;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderItem;
import cn.yizhi.yzt.pipeline.model.fact.member.FactOrderItemPack;
import cn.yizhi.yzt.pipeline.model.fact.member.FactPackOdsLog;
import cn.yizhi.yzt.pipeline.model.fact.member.OdsProductReGroup;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductLog;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductOrder;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductRefund;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderItem;
import cn.yizhi.yzt.pipeline.util.JsonMapper;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.table.api.Table;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author: HuCheng
 * 会员商品指标-天
 * @Date: 2021/1/5 14:22
 */
public class FactMemberStatisticsProductStreamJob extends StreamJob {
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
        //包装groupId
        productPackGroup();
        //商品信息
        product();

    }

    private void product() {
        //注册sql文件
        this.registerSql("fact-member-product.yml");

        //注册表
        this.createTableFromJdbc("ods_order_item", Tables.SOURCE_TABLE_ODS_ORDER_ITEM, OdsOrderItem.class);

        //日志流
        DataStream<FactPackOdsLog> odsLogStreamDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PACK_ODS_LOG, FactPackOdsLog.class);

        //订单流-不包括退款
        DataStream<FactOrderItemPack> factOrderItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PACK_ORDER_ITEM, FactOrderItemPack.class);

        //emall_order
        DataStream<OdsEmallOrder> orderDs = this.createStreamFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class)
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
                })
                .filter(new FilterFunction<OdsEmallOrder>() {
                    @Override
                    public boolean filter(OdsEmallOrder value) throws Exception {
                        // 9 为退款成功
                        return value.getStatus() == 9;
                    }
                });


        //订单流转表
        streamToTable(OdsEmallOrder.class, orderDs, true);
        //关联取出商品数据
        Table memberProductDimQuery = this.sqlQuery("memberProductDimQuery");
        DataStream<FactOrderItem> factRefundOrderItemDataStream = tableEnv.toAppendStream(memberProductDimQuery, FactOrderItem.class);


        //过滤日志流
        DataStream<FactPackOdsLog> filterLogStream = odsLogStreamDs.filter(new FilterFunction<FactPackOdsLog>() {
            @Override
            public boolean filter(FactPackOdsLog value) throws Exception {
                if (value.getPackType() == PackType.PACKING_DATA) {
                    return true;
                }
                String eventName = value.getEventName();
                return value.getShopId() != null && value.getShopId() != 0 && value.getEndTime() == null && value.getUserId() != null && value.getUserId() > 0
                        && value.getGoodsId() != null && (EventType.VIEW_GOODS.getName().equals(eventName) || EventType.VIEW_GOODS_BY_ACTIVITY.getName().equals(eventName)
                        || EventType.ADD_TO_SHOPPING_CART.getName().equals(eventName) || EventType.ADD_TO_SHOPPING_CART_BY_ACTIVITY.getName().equals(eventName)
                        || EventType.SEARCH_GOODS.getName().equals(eventName) || EventType.SHARE_GOODS_BY_ACTIVITY.getName().equals(eventName));
            }
        });

        //计算日志数据
        DataStream<FactMemberProductLog> factMemberProductLogDs = filterLogStream
                .keyBy(new KeySelector<FactPackOdsLog, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactPackOdsLog value) throws Exception {
                        return new Tuple2<>(value.getShopId(), value.getGoodsId());
                    }
                })
                .process(new FactMemberProductGroupProcessFunction())
                .keyBy(new KeySelector<FactPackOdsLog, Tuple4<String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, Integer> getKey(FactPackOdsLog value) throws Exception {
                        return new Tuple4<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), value.getShopId(), value.getUserId(), value.getGoodsId());
                    }
                })
                .process(new FactMemberProductLogProcessFunction())
                .uid("mg-fact-member-product-log")
                .name("mg-fact-member-product-log");

        //写入kafka
        toKafkaSink(factMemberProductLogDs, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        //写入mysql
        toJdbcUpsertSink(factMemberProductLogDs, Tables.SINK_TABLE_MEMBER_PRODUCT, FactMemberProductLog.class);


        //下单
        DataStream<FactMemberProductOrder> factMemberProductOrderDs = factOrderItemDs
                .filter(new FilterFunction<FactOrderItemPack>() {
                    @Override
                    public boolean filter(FactOrderItemPack value) throws Exception {

                        if (value.getParentId() != null && value.getParentId() > 0) {//过滤组合商品的子项
                            return false;
                        }
                        return true;
                    }
                }).keyBy(new KeySelector<FactOrderItemPack, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactOrderItemPack value) throws Exception {
                        return new Tuple2<>(value.getShopId(), value.getProductId());
                    }
                })
                .process(new FactItemOrderProcessFunction())
                .keyBy(new KeySelector<FactOrderItemPack, Tuple4<String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, Integer> getKey(FactOrderItemPack value) throws Exception {
                        return new Tuple4<>(TimeUtil.convertTimeStampToDay(value.getUpdatedAt()), value.getShopId(), value.getMemberId(), value.getProductId());
                    }
                })
                .process(new FactMemberProductOrderProcessFunction())
                .uid("mg-fact-member-product-order")
                .name("mg-fact-member-product-order");

        toKafkaSink(factMemberProductOrderDs, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        toJdbcUpsertSink(factMemberProductOrderDs, Tables.SINK_TABLE_MEMBER_PRODUCT, FactMemberProductOrder.class);


        //退款
        DataStream<FactMemberProductRefund> factMemberProductRefundDs = factRefundOrderItemDataStream
                .keyBy(new KeySelector<FactOrderItem, Tuple4<String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, Integer> getKey(FactOrderItem value) throws Exception {
                        return new Tuple4<>(TimeUtil.convertTimeStampToDay(value.getUpdatedAt()), value.getShopId(), value.getMemberId(), value.getProductId());
                    }
                })
                .process(new FactMemberProductRefundProcessFunction())
                .uid("mg-fact-member-product-refund")
                .name("mg-fact-member-product-refund");

        toKafkaSink(factMemberProductRefundDs, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        toJdbcUpsertSink(factMemberProductRefundDs, Tables.SINK_TABLE_MEMBER_PRODUCT, FactMemberProductRefund.class);
    }


    private void productPackGroup() {
        //更新的规则数据
        DataStream<OdsProductReGroup> topicReGroupStream = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_PRODUCT_RE_GROUP, OdsProductReGroup.class);


        //包装更新的规则-logStream
        DataStream<FactPackOdsLog> topicPackOdsLog = topicReGroupStream
                .map(new MapFunction<OdsProductReGroup, FactPackOdsLog>() {
                    @Override
                    public FactPackOdsLog map(OdsProductReGroup value) throws Exception {
                        FactPackOdsLog f = new FactPackOdsLog();
                        f.setPackType(PackType.PACKING_DATA);
                        f.setGroupId(value.getGroupId());
                        f.setShopId(value.getShopId());
                        f.setGoodsId(value.getProductId());
                        return f;
                    }
                });

        //包装更新的规则-订单
        DataStream<FactOrderItemPack> topicOrderItemPack = topicReGroupStream
                .map(new MapFunction<OdsProductReGroup, FactOrderItemPack>() {
                    @Override
                    public FactOrderItemPack map(OdsProductReGroup value) throws Exception {
                        FactOrderItemPack f = new FactOrderItemPack();
                        f.setPackType(PackType.PACKING_DATA);
                        f.setGroupId(value.getGroupId());
                        f.setShopId(value.getShopId());
                        f.setProductId(value.getProductId());
                        return f;
                    }
                });


        //包装日志流-logSteam
        DataStream<FactPackOdsLog> packOdsLogDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, FactPackOdsLog.class)
                .map(new MapFunction<FactPackOdsLog, FactPackOdsLog>() {
                    @Override
                    public FactPackOdsLog map(FactPackOdsLog value) throws Exception {
                        value.setPackType(PackType.RAW_DATA);
                        return value;
                    }
                });

        //包装订单流
        DataStream<FactOrderItemPack> factOrderItemPackSingleOutputStreamOperator = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_ITEM, FactOrderItemPack.class)
                .map(new MapFunction<FactOrderItemPack, FactOrderItemPack>() {
                    @Override
                    public FactOrderItemPack map(FactOrderItemPack value) throws Exception {
                        value.setPackType(PackType.RAW_DATA);
                        return value;
                    }
                });

        //写入kafka-logSteam
        //topic数据写入到 聚合topic
        toKafkaSink(topicPackOdsLog, SourceTopics.TOPIC_FACT_PACK_ODS_LOG);
        toKafkaSink(topicOrderItemPack, SourceTopics.TOPIC_FACT_PACK_ORDER_ITEM);

        //原始数据
        toKafkaSink(packOdsLogDataStream, SourceTopics.TOPIC_FACT_PACK_ODS_LOG);
        toKafkaSink(factOrderItemPackSingleOutputStreamOperator, SourceTopics.TOPIC_FACT_PACK_ORDER_ITEM);
    }


    /**
     * 分组
     */
    public static class FactMemberProductGroupProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, FactPackOdsLog, FactPackOdsLog> {

        private transient MapState<Integer, List<Integer>> shopGroupState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            MapStateDescriptor<Integer, List<Integer>> groupState = new MapStateDescriptor<>(
                    // the state name
                    "groupState",
                    // type information
                    Types.INT,
                    Types.LIST(Types.INT));
            groupState.enableTimeToLive(groupConfig);
            shopGroupState = getRuntimeContext().getMapState(groupState);
        }

        @Override
        public void processElement(FactPackOdsLog value, Context ctx, Collector<FactPackOdsLog> out) throws Exception {
            //更新
            if (value.getPackType() == PackType.PACKING_DATA) {
                List<Integer> groupIds = shopGroupState.get(value.getShopId());
                if (groupIds == null || groupIds.size() == 0) {
                    groupIds = new ArrayList<>();
                }
                groupIds.add(value.getGroupId());
                Set s = new HashSet<>();
                groupIds.forEach(e -> {
                    s.add(e);
                });
                groupIds.clear();
                groupIds.addAll(s);
                shopGroupState.put(value.getShopId(), groupIds);
            } else {
                value.setGroupIds(shopGroupState.get(value.getShopId()));
                out.collect(value);
            }
        }
    }

    /**
     * 分组
     */
    public static class FactItemOrderProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, FactOrderItemPack, FactOrderItemPack> {

        private transient MapState<Integer, List<Integer>> shopGroupState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            MapStateDescriptor<Integer, List<Integer>> groupState = new MapStateDescriptor<>(
                    // the state name
                    "itemGroupState",
                    // type information
                    Types.INT,
                    Types.LIST(Types.INT));
            groupState.enableTimeToLive(groupConfig);
            shopGroupState = getRuntimeContext().getMapState(groupState);
        }

        @Override
        public void processElement(FactOrderItemPack value, Context ctx, Collector<FactOrderItemPack> out) throws Exception {
            //更新
            if (value.getPackType() == PackType.PACKING_DATA) {
                List<Integer> groupIds = shopGroupState.get(value.getShopId());
                if (groupIds == null || groupIds.size() == 0) {
                    groupIds = new ArrayList<>();
                }
                groupIds.add(value.getGroupId());
                Set s = new HashSet<>();
                groupIds.forEach(e -> {
                    s.add(e);
                });
                groupIds.clear();
                groupIds.addAll(s);
                shopGroupState.put(value.getShopId(), groupIds);
            } else {
                value.setGroupIds(shopGroupState.get(value.getShopId()));
                out.collect(value);
            }
        }
    }

    //计算日志数据
    public static class FactMemberProductLogProcessFunction extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, FactPackOdsLog, FactMemberProductLog> {
        //定义state
        private transient ValueState<Integer> viewedTimesState;
        private transient ValueState<Integer> shareTimesState;
        private transient ValueState<Integer> cartAddTimesState;
        private transient ValueState<Timestamp> cartAddDateState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> viewedTimesStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "viewedTimesState",
                    // type information
                    Types.INT);
            viewedTimesStateDescriptor.enableTimeToLive(ttlConfig);
            viewedTimesState = getRuntimeContext().getState(viewedTimesStateDescriptor);

            ValueStateDescriptor<Integer> shareTimesStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shareTimesState",
                    // type information
                    Types.INT);
            shareTimesStateDescriptor.enableTimeToLive(ttlConfig);
            shareTimesState = getRuntimeContext().getState(shareTimesStateDescriptor);

            ValueStateDescriptor<Integer> cartAddTimesDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "cartAddTimes",
                    // type information
                    Types.INT);
            cartAddTimesDescriptor.enableTimeToLive(ttlConfig);
            cartAddTimesState = getRuntimeContext().getState(cartAddTimesDescriptor);

            ValueStateDescriptor<Timestamp> cartAddDateStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "cartAddDateState",
                    // type information
                    Types.SQL_TIMESTAMP);
            cartAddDateStateDescriptor.enableTimeToLive(ttlConfig);
            cartAddDateState = getRuntimeContext().getState(cartAddDateStateDescriptor);
        }

        @Override
        public void processElement(FactPackOdsLog value, Context ctx, Collector<FactMemberProductLog> out) throws Exception {
            String eventName = value.getEventName();

            long oneTime = value.getEventTime().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 86400000;
            if (judge >= watermark) {
                Integer viewTimes = viewedTimesState.value();
                Integer shareTimes = shareTimesState.value();
                Integer cartAddTimes = cartAddTimesState.value();

                FactMemberProductLog factMemberProductLog = new FactMemberProductLog();
                Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
                factMemberProductLog.setAnalysisDate(currentKey.f0);
                factMemberProductLog.setShopId(currentKey.f1);
                factMemberProductLog.setMemberId(currentKey.f2);
                factMemberProductLog.setProductId(currentKey.f3);

                //浏览商品
                if (EventType.VIEW_GOODS.getName().equals(eventName) || EventType.VIEW_GOODS_BY_ACTIVITY.getName().equals(eventName)) {
                    if (viewTimes == null) {
                        viewedTimesState.update(1);
                    } else {
                        viewedTimesState.update(viewTimes + 1);
                    }
                }

                //分享商品
                if (EventType.SHARE_GOODS.getName().equals(eventName) || EventType.SHARE_GOODS_BY_ACTIVITY.getName().equals(eventName)) {
                    if (shareTimes == null) {
                        shareTimesState.update(1);
                    } else {
                        shareTimesState.update(shareTimes + 1);
                    }
                }

                //加入购物车
                if (EventType.ADD_TO_SHOPPING_CART.getName().equals(eventName) || EventType.ADD_TO_SHOPPING_CART_BY_ACTIVITY.getName().equals(eventName)) {
                    if (cartAddTimes == null) {
                        cartAddTimesState.update(1);
                    } else {
                        cartAddTimesState.update(cartAddTimes + 1);
                    }

                    cartAddDateState.update(value.getEventTime());
                }
                factMemberProductLog.setViewedTimes(viewedTimesState.value() != null ? viewedTimesState.value() : 0);
                factMemberProductLog.setShareTimes(shareTimesState.value() != null ? shareTimesState.value() : 0);
                factMemberProductLog.setCartAddTimes(cartAddTimesState.value() != null ? cartAddTimesState.value() : 0);
                factMemberProductLog.setCartAddDate(cartAddDateState.value());
                factMemberProductLog.setDataType(DataType.PRODUCT_LOG);
                List<Integer> groupIds = value.getGroupIds();
                if (!CollectionUtil.isNullOrEmpty(groupIds)) {
                    factMemberProductLog.setGroupIds(JsonMapper.nonEmptyMapper().toJson(groupIds));
                }

                factMemberProductLog.setGroups(value.getGroupIds());
                out.collect(factMemberProductLog);
            }
        }
    }

    //下单数据
    public static class FactMemberProductOrderProcessFunction extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, FactOrderItemPack, FactMemberProductOrder> {
        private transient ValueState<Integer> orderTimesState;
        private transient ValueState<BigDecimal> orderAmountState;
        private transient ValueState<Integer> paidTimesState;
        private transient ValueState<BigDecimal> paidAmountState;
        private transient ValueState<Integer> orderQuantityState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ValueStateDescriptor<Integer> orderTimesStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderTimesState",
                    // type information
                    Types.INT);
            orderTimesStateDescriptor.enableTimeToLive(ttlConfig);
            orderTimesState = getRuntimeContext().getState(orderTimesStateDescriptor);

            ValueStateDescriptor<BigDecimal> orderAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderAmountState",
                    // type information
                    Types.BIG_DEC);
            orderAmountStateDescriptor.enableTimeToLive(ttlConfig);
            orderAmountState = getRuntimeContext().getState(orderAmountStateDescriptor);


            ValueStateDescriptor<Integer> paidTimesStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidTimesState",
                    // type information
                    Types.INT);
            paidTimesStateDescriptor.enableTimeToLive(ttlConfig);
            paidTimesState = getRuntimeContext().getState(paidTimesStateDescriptor);

            ValueStateDescriptor<BigDecimal> paidAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidAmountState",
                    // type information
                    Types.BIG_DEC);
            paidAmountStateDescriptor.enableTimeToLive(ttlConfig);
            paidAmountState = getRuntimeContext().getState(paidAmountStateDescriptor);


            ValueStateDescriptor<Integer> orderQuantityStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderQuantityState",
                    // type information
                    Types.INT);
            orderQuantityStateDescriptor.enableTimeToLive(ttlConfig);
            orderQuantityState = getRuntimeContext().getState(orderQuantityStateDescriptor);


        }

        @Override
        public void processElement(FactOrderItemPack value, Context ctx, Collector<FactMemberProductOrder> out) throws Exception {
            Integer orderTimes = orderTimesState.value();
            BigDecimal orderAmount = orderAmountState.value();
            Integer paidTimes = paidTimesState.value();
            BigDecimal paidAmount = paidAmountState.value();
            Integer orderQuantity = orderQuantityState.value();

            Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            FactMemberProductOrder factMemberProductOrder = new FactMemberProductOrder();
            factMemberProductOrder.setAnalysisDate(currentKey.f0);
            factMemberProductOrder.setShopId(currentKey.f1);
            factMemberProductOrder.setMemberId(currentKey.f2);
            factMemberProductOrder.setProductId(currentKey.f3);

            //下单
            if (value.getStatus() == 0 || value.getActualAmount().compareTo(BigDecimal.ZERO) <= 0) {
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
            }

            //支付
            if (value.getStatus() == 1) {
                if (paidTimes == null) {
                    paidTimesState.update(1);
                } else {
                    paidTimesState.update(paidTimes + 1);
                }

                if (orderQuantity == null) {
                    orderQuantityState.update(value.getQuantity());
                } else {
                    orderQuantityState.update(orderQuantity + value.getQuantity());
                }

                if (paidAmount == null) {
                    paidAmountState.update(value.getActualAmount());
                } else {
                    paidAmountState.update(paidAmount.add(value.getActualAmount()));
                }

            }

            factMemberProductOrder.setOrderTimes(orderTimesState.value() != null ? orderTimesState.value() : 0);
            factMemberProductOrder.setOrderAmount(orderAmountState.value() != null ? orderAmountState.value() : BigDecimal.ZERO);
            factMemberProductOrder.setPaidTimes(paidTimesState.value() != null ? paidTimesState.value() : 0);
            factMemberProductOrder.setPaidAmount(paidAmountState.value() != null ? paidAmountState.value() : BigDecimal.ZERO);
            factMemberProductOrder.setOrderQuantity(orderQuantityState.value() != null ? orderQuantityState.value() : 0);
            factMemberProductOrder.setDataType(DataType.PRODUCT_ORDER);
            List<Integer> groupIds = value.getGroupIds();
            if (!CollectionUtil.isNullOrEmpty(groupIds)) {
                factMemberProductOrder.setGroupIds(JsonMapper.nonEmptyMapper().toJson(groupIds));
            }
            factMemberProductOrder.setGroups(value.getGroupIds());
            out.collect(factMemberProductOrder);
        }
    }

    //退款
    public static class FactMemberProductRefundProcessFunction extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, FactOrderItem, FactMemberProductRefund> {

        private transient ValueState<Integer> refundTimesState;
        private transient ValueState<Integer> refundQuantityState;
        private transient ValueState<BigDecimal> refundAmountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ValueStateDescriptor<Integer> refundTimesStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundTimesState",
                    // type information
                    Types.INT);
            refundTimesStateDescriptor.enableTimeToLive(ttlConfig);
            refundTimesState = getRuntimeContext().getState(refundTimesStateDescriptor);

            ValueStateDescriptor<Integer> refundQuantityStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundQuantityState",
                    // type information
                    Types.INT);
            refundQuantityStateDescriptor.enableTimeToLive(ttlConfig);
            refundQuantityState = getRuntimeContext().getState(refundQuantityStateDescriptor);

            ValueStateDescriptor<BigDecimal> refundAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundAmountState",
                    // type information
                    Types.BIG_DEC);
            refundAmountStateDescriptor.enableTimeToLive(ttlConfig);
            refundAmountState = getRuntimeContext().getState(refundAmountStateDescriptor);

        }

        @Override
        public void processElement(FactOrderItem value, Context ctx, Collector<FactMemberProductRefund> out) throws Exception {
            Integer refundTimes = refundTimesState.value();
            Integer refundQuantity = refundQuantityState.value();
            BigDecimal refundAmount = refundAmountState.value();

            Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            FactMemberProductRefund factMemberProductRefund = new FactMemberProductRefund();
            factMemberProductRefund.setAnalysisDate(currentKey.f0);
            factMemberProductRefund.setShopId(currentKey.f1);
            factMemberProductRefund.setMemberId(currentKey.f2);
            factMemberProductRefund.setProductId(currentKey.f3);


            //退款
            if (value.getStatus() == 9) {
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

                if (refundQuantity == null) {
                    refundQuantityState.update(value.getQuantity());
                } else {
                    refundQuantityState.update(refundQuantity + value.getQuantity());
                }
            }

            factMemberProductRefund.setRefundTimes(refundTimesState.value() != null ? refundTimesState.value() : 0);
            factMemberProductRefund.setRefundQuantity(refundQuantityState.value() != null ? refundQuantityState.value() : 0);
            factMemberProductRefund.setRefundAmount(refundAmountState.value() != null ? refundAmountState.value() : BigDecimal.ZERO);
            factMemberProductRefund.setDataType(DataType.PRODUCT_REFUND);

            out.collect(factMemberProductRefund);
        }
    }

}
