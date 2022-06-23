package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion.FactGroupBuyGroupV2;
import cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion.FactGroupBuyInventory;
import cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion.FactGroupBuyOrder;
import cn.yizhi.yzt.pipeline.model.fact.groupbuy.user.FactGroupBuyStaff;
import cn.yizhi.yzt.pipeline.model.ods.OdsGroupPromotionInstance;
import cn.yizhi.yzt.pipeline.model.ods.OdsGroupPromotionInstanceMember;
import cn.yizhi.yzt.pipeline.model.ods.ProductSkuExtend;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: HuCheng
 * 拼团二期数据统计
 * @Date: 2021/1/20 16:19
 */
public class FactGroupBuyV2StreamJob extends StreamJob {

    private static final StateTtlConfig ttlConfig = StateTtlConfig
            // 保留90天, 取决于订单生命周期的时间跨度
            .newBuilder(org.apache.flink.api.common.time.Time.days(90))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {
        //接收拼团订单数据流
        DataStream<OdsGroupPromotionInstanceMember> odsGroupPromotionInstanceMemberDs = this.createStreamFromKafka(SourceTopics.TOPIC_GROUP_BUY_PROMOTION_INSTANCE_MEMBER, OdsGroupPromotionInstanceMember.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsGroupPromotionInstanceMember>() {
                    @Override
                    public long extractAscendingTimestamp(OdsGroupPromotionInstanceMember element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });


        //成团
        DataStream<OdsGroupPromotionInstance> odsGroupPromotionInstanceDs = this.createStreamFromKafka(SourceTopics.TOPIC_GROUP_BUY_PROMOTION_INSTANCE, OdsGroupPromotionInstance.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsGroupPromotionInstance>() {
                    @Override
                    public long extractAscendingTimestamp(OdsGroupPromotionInstance element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        //库存
        DataStream<ProductSkuExtend> odsProductSkuExtendDs = this.createStreamFromKafka(SourceTopics.TOPIC_PRODUCT_SKU_EXTEND, ProductSkuExtend.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ProductSkuExtend>() {
                    @Override
                    public long extractAscendingTimestamp(ProductSkuExtend element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });


        //过滤活动数据
        DataStream<OdsGroupPromotionInstanceMember> filterOrder = odsGroupPromotionInstanceMemberDs
                .keyBy(new KeySelector<OdsGroupPromotionInstanceMember, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsGroupPromotionInstanceMember value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new GroupBuyOrderFilter())
                .name("group-buy-order-filter")
                .uid("group-buy-order-filter");

        //计算活动订单数据
        DataStream<FactGroupBuyOrder> factGroupBuyOrderDs = filterOrder
                .keyBy(new KeySelector<OdsGroupPromotionInstanceMember, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsGroupPromotionInstanceMember value) throws Exception {
                        return new Tuple1<>(value.getGroupPromotionId());
                    }
                })
                .process(new GroupBuyOrderPromotionProcessFunction())
                .name("group-buy-order-promotion-process")
                .uid("group-buy-order-promotion-process");

        //团数
        DataStream<FactGroupBuyGroupV2> factGroupBuyGroupV2Ds = odsGroupPromotionInstanceDs
                .keyBy(new KeySelector<OdsGroupPromotionInstance, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsGroupPromotionInstance value) throws Exception {
                        return new Tuple1<>(value.getGroupPromotionId());
                    }
                })
                .process(new GroupBuyGroupedProcessFunction())
                .name("group-buy-grouped-process")
                .uid("group-buy-grouped-process");

        //库存，一个活动下有多个库存
        DataStream<FactGroupBuyInventory> factGroupBugInventoryDs = odsProductSkuExtendDs
                .filter(new FilterFunction<ProductSkuExtend>() {
                    @Override
                    public boolean filter(ProductSkuExtend value) throws Exception {
                        //9 表示拼团
                        return value.getPromotionType() == 9;
                    }
                })
                .keyBy(new KeySelector<ProductSkuExtend, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(ProductSkuExtend value) throws Exception {
                        return new Tuple1<Integer>(value.getPromotionId());
                    }
                }).process(new GroupBugInventoryProcessFunction())
                .uid("group-buy-inventory-process")
                .name("group-buy-inventory-process");


        //计算受益人数据
        DataStream<FactGroupBuyStaff> factGroupBuyStaffDs = filterOrder
                .keyBy(new KeySelector<OdsGroupPromotionInstanceMember, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(OdsGroupPromotionInstanceMember value) throws Exception {
                        return new Tuple2<>(value.getGroupPromotionId(), value.getStaffId());
                    }
                })
                .process(new GroupBuyStaffProcessFunction())
                .name("group-buy-order-promotion-staff")
                .uid("group-buy-order-promotion-staff");

        //写入mysql
        toJdbcUpsertSink(factGroupBuyOrderDs, Tables.SINK_TABLE_GROUP_BUY_ORDER_V1, FactGroupBuyOrder.class);
        toJdbcUpsertSink(factGroupBuyGroupV2Ds, Tables.SINK_TABLE_GROUP_BUY_ORDER_V1, FactGroupBuyGroupV2.class);
        toJdbcUpsertSink(factGroupBugInventoryDs, Tables.SINK_TABLE_GROUP_BUY_ORDER_V1, FactGroupBuyInventory.class);

        toJdbcUpsertSink(factGroupBuyStaffDs, Tables.SINK_TABLE_GROUP_BUY_STAFF_V1, FactGroupBuyStaff.class);
    }

    /**
     * 过滤拼团订单，防止重复计算
     */
    public static class GroupBuyOrderFilter extends RichFilterFunction<OdsGroupPromotionInstanceMember> {

        private transient MapState<Integer, Boolean> paidOrderState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            MapStateDescriptor<Integer, Boolean> descriptor =
                    new MapStateDescriptor<>(
                            // the state name
                            "paid-order-map",
                            // type information
                            Types.INT,
                            Types.BOOLEAN);
            descriptor.enableTimeToLive(ttlConfig);
            paidOrderState = getRuntimeContext().getMapState(descriptor);

        }

        @Override
        public boolean filter(OdsGroupPromotionInstanceMember value) throws Exception {
            System.out.println(" ** 过滤前的OdsGroupPromotionInstanceMember ** " + value);

            //机器人成团 或者删除订单？？
            if (value.getOrderId() == 0 || value.getDeleted() == 1) {
                return false;
            }

            //退款成功
            if (value.getIsPaid() == 1 && value.getRefundStatus() == 2) {
                return true;
            }

            if (paidOrderState.contains(value.getId())) {
                if (!paidOrderState.get(value.getId()) && value.getIsPaid() == 1) {
                    paidOrderState.put(value.getId(), true);
                    return true;
                }
            } else {
                if (value.getIsPaid() == 0) {
                    paidOrderState.put(value.getId(), false);
                } else {
                    paidOrderState.put(value.getId(), true);
                }
                return true;
            }

            return false;
        }
    }

    public static class GroupBuyOrderPromotionProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, OdsGroupPromotionInstanceMember, FactGroupBuyOrder> {
        //付款
        private transient ValueState<Integer> paidOrderState;
        private transient ValueState<BigDecimal> paidAmountState;

        //退款
        private transient ValueState<Integer> refundOrderState;
        private transient ValueState<BigDecimal> refundAmountState;

        //新客
        private transient MapState<Integer,Integer> newUserCountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> paidOrderStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidOrderState-promotion",
                    // type information
                    Types.INT);
            paidOrderStateDescriptor.enableTimeToLive(ttlConfig);
            paidOrderState = getRuntimeContext().getState(paidOrderStateDescriptor);

            ValueStateDescriptor<BigDecimal> paidAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidAmountState-promotion",
                    // type information
                    Types.BIG_DEC);
            paidAmountStateDescriptor.enableTimeToLive(ttlConfig);
            paidAmountState = getRuntimeContext().getState(paidAmountStateDescriptor);

            ValueStateDescriptor<Integer> refundOrderStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundOrderState-promotion",
                    // type information
                    Types.INT);
            refundOrderStateDescriptor.enableTimeToLive(ttlConfig);
            refundOrderState = getRuntimeContext().getState(refundOrderStateDescriptor);

            ValueStateDescriptor<BigDecimal> refundAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundAmountState-promotion",
                    // type information
                    Types.BIG_DEC);
            refundAmountStateDescriptor.enableTimeToLive(ttlConfig);
            refundAmountState = getRuntimeContext().getState(refundAmountStateDescriptor);


            MapStateDescriptor<Integer,Integer> newUserCountStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "newUserCountState-promotion",
                    // type information
                    Types.INT,
                    Types.INT);
            newUserCountStateDescriptor.enableTimeToLive(ttlConfig);
            newUserCountState = getRuntimeContext().getMapState(newUserCountStateDescriptor);
        }

        @Override
        public void processElement(OdsGroupPromotionInstanceMember value, Context ctx, Collector<FactGroupBuyOrder> out) throws Exception {
            updateStateData(value, paidOrderState, paidAmountState, refundOrderState, refundAmountState, newUserCountState);

            Tuple1<Integer> currentKey = ctx.getCurrentKey();
            FactGroupBuyOrder factGroupBuyOrder = new FactGroupBuyOrder();
            factGroupBuyOrder.setPromotionId(currentKey.f0);

            AtomicInteger newUserCount = new AtomicInteger();
            newUserCountState.values().forEach(newUserCount::addAndGet);

            factGroupBuyOrder.setOrderNumber(paidOrderState.value() != null ? paidOrderState.value() : 0);
            factGroupBuyOrder.setPayAmount(paidAmountState.value() != null ? paidAmountState.value() : BigDecimal.ZERO);
            factGroupBuyOrder.setNewUserCount(newUserCount.intValue());
            factGroupBuyOrder.setRefundNumber(refundOrderState.value() != null ? refundOrderState.value() : 0);
            factGroupBuyOrder.setRefundAmount(refundAmountState.value() != null ? refundAmountState.value() : BigDecimal.ZERO);

            out.collect(factGroupBuyOrder);
        }
    }


    public static class GroupBuyStaffProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, OdsGroupPromotionInstanceMember, FactGroupBuyStaff> {
        //付款
        private transient ValueState<Integer> paidOrderState;
        private transient ValueState<BigDecimal> paidAmountState;

        //退款
        private transient ValueState<Integer> refundOrderState;
        private transient ValueState<BigDecimal> refundAmountState;

        //新客
        private transient MapState<Integer,Integer> newUserCountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ValueStateDescriptor<Integer> paidOrderStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidOrderState-staff",
                    // type information
                    Types.INT);
            paidOrderStateDescriptor.enableTimeToLive(ttlConfig);
            paidOrderState = getRuntimeContext().getState(paidOrderStateDescriptor);

            ValueStateDescriptor<BigDecimal> paidAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidAmountState-staff",
                    // type information
                    Types.BIG_DEC);
            paidAmountStateDescriptor.enableTimeToLive(ttlConfig);
            paidAmountState = getRuntimeContext().getState(paidAmountStateDescriptor);

            ValueStateDescriptor<Integer> refundOrderStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundOrderState-staff",
                    // type information
                    Types.INT);
            refundOrderStateDescriptor.enableTimeToLive(ttlConfig);
            refundOrderState = getRuntimeContext().getState(refundOrderStateDescriptor);

            ValueStateDescriptor<BigDecimal> refundAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundAmountState-staff",
                    // type information
                    Types.BIG_DEC);
            refundAmountStateDescriptor.enableTimeToLive(ttlConfig);
            refundAmountState = getRuntimeContext().getState(refundAmountStateDescriptor);


            MapStateDescriptor<Integer,Integer> newUserCountStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "newUserCountState-staff",
                    // type information
                    Types.INT,
                    Types.INT);
            newUserCountStateDescriptor.enableTimeToLive(ttlConfig);
            newUserCountState = getRuntimeContext().getMapState(newUserCountStateDescriptor);
        }

        @Override
        public void processElement(OdsGroupPromotionInstanceMember value, Context ctx, Collector<FactGroupBuyStaff> out) throws Exception {
            updateStateData(value, paidOrderState, paidAmountState, refundOrderState, refundAmountState, newUserCountState);

            Tuple2<Integer, Integer> currentKey = ctx.getCurrentKey();
            FactGroupBuyStaff factGroupBuyStaff = new FactGroupBuyStaff();
            factGroupBuyStaff.setPromotionId(currentKey.f0);
            factGroupBuyStaff.setStaffId(currentKey.f1);
            factGroupBuyStaff.setName(value.getStaffNickname());
            factGroupBuyStaff.setPhone(value.getStaffPhone());

            AtomicInteger newUserCount = new AtomicInteger();
            newUserCountState.values().forEach(newUserCount::addAndGet);

            factGroupBuyStaff.setOrderNumber(paidOrderState.value() != null ? paidOrderState.value() : 0);
            factGroupBuyStaff.setPayAmount(paidAmountState.value() != null ? paidAmountState.value() : BigDecimal.ZERO);
            factGroupBuyStaff.setNewUserCount(newUserCount.intValue());
            factGroupBuyStaff.setRefundNumber(refundOrderState.value() != null ? refundOrderState.value() : 0);
            factGroupBuyStaff.setRefundAmount(refundAmountState.value() != null ? refundAmountState.value() : BigDecimal.ZERO);

            out.collect(factGroupBuyStaff);
        }
    }


    //成团
    public static class GroupBuyGroupedProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, OdsGroupPromotionInstance, FactGroupBuyGroupV2> {
        //满团
        private transient ValueState<Integer> fullGroupedCountState;

        //成团
        private transient ValueState<Integer> groupedCountState;

        //成团失败
        private transient ValueState<Integer> groupedFailedState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> groupedCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "groupedCountState",
                    // type information
                    Types.INT);
            groupedCountStateDescriptor.enableTimeToLive(ttlConfig);
            groupedCountState = getRuntimeContext().getState(groupedCountStateDescriptor);

            ValueStateDescriptor<Integer> fullGroupedCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "fullGroupedCountState",
                    // type information
                    Types.INT);
            fullGroupedCountStateDescriptor.enableTimeToLive(ttlConfig);
            fullGroupedCountState = getRuntimeContext().getState(fullGroupedCountStateDescriptor);

            ValueStateDescriptor<Integer> groupedFailedCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "groupedFailedState",
                    // type information
                    Types.INT);
            groupedFailedCountStateDescriptor.enableTimeToLive(ttlConfig);
            groupedFailedState = getRuntimeContext().getState(groupedFailedCountStateDescriptor);
        }

        @Override
        public void processElement(OdsGroupPromotionInstance value, Context ctx, Collector<FactGroupBuyGroupV2> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;


            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                Integer fullGroupedCount = fullGroupedCountState.value();
                Integer groupedCount = groupedCountState.value();
                Integer groupedFailedCount = groupedFailedState.value();

                //拼团状态 0-开团未支付 1-待成团 2-满员成团 3-拼团失败 4-机器人成团 5-手动成团
                if (value.getStatus() == 2 || value.getStatus() == 4 || value.getStatus() == 5) {
                    if (groupedCount == null) {
                        groupedCountState.update(1);
                    } else {
                        groupedCountState.update(groupedCount + 1);
                    }
                }

                if (value.getStatus() == 2) {
                    if (fullGroupedCount == null) {
                        fullGroupedCountState.update(1);
                    } else {
                        fullGroupedCountState.update(fullGroupedCount + 1);
                    }
                }

                if (value.getStatus() == 3) {
                    if (groupedFailedCount == null) {
                        groupedFailedState.update(1);
                    } else {
                        groupedFailedState.update(groupedFailedCount + 1);
                    }
                }
            }

            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactGroupBuyGroupV2 factGroupBuyGroupV2 = new FactGroupBuyGroupV2();
            factGroupBuyGroupV2.setPromotionId(currentKey.f0);
            factGroupBuyGroupV2.setGroupedCount(groupedCountState.value() != null ? groupedCountState.value() : 0);
            factGroupBuyGroupV2.setFullGroupedCount(fullGroupedCountState.value() != null ? fullGroupedCountState.value() : 0);
            factGroupBuyGroupV2.setGroupedFailedCount(groupedFailedState.value() != null ? groupedFailedState.value() : 0);

            out.collect(factGroupBuyGroupV2);
        }
    }


    //库存
    public static class GroupBugInventoryProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, ProductSkuExtend, FactGroupBuyInventory> {
        //定义商品库存state  key为sku_id value为剩余库存
        private transient MapState<Integer, Integer> inventoryState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            MapStateDescriptor<Integer, Integer> inventoryStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "inventoryState",
                    // type information
                    Types.INT,
                    Types.INT);
            inventoryStateDescriptor.enableTimeToLive(ttlConfig);
            inventoryState = getRuntimeContext().getMapState(inventoryStateDescriptor);
        }

        @Override
        public void processElement(ProductSkuExtend value, Context ctx, Collector<FactGroupBuyInventory> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;


            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                inventoryState.put(value.getProductSkuId(), value.getInventory());
            }


            AtomicInteger inventory = new AtomicInteger();
            inventoryState.values().forEach(inventory::addAndGet);

            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactGroupBuyInventory factGroupBugInventory = new FactGroupBuyInventory();

            factGroupBugInventory.setPromotionId(currentKey.f0);
            factGroupBugInventory.setInventory(inventory.get());

            out.collect(factGroupBugInventory);
        }
    }

    /**
     * 更新状态数据
     *
     * @param value
     * @param paidOrderState
     * @param paidAmountState
     * @param refundOrderState
     * @param refundAmountState
     * @param newUserCountState
     * @throws Exception
     */
    private static void updateStateData(OdsGroupPromotionInstanceMember value, ValueState<Integer> paidOrderState, ValueState<BigDecimal> paidAmountState,
                                        ValueState<Integer> refundOrderState, ValueState<BigDecimal> refundAmountState,
                                        MapState<Integer,Integer> newUserCountState) throws Exception {
        System.out.println(" ** 过滤后的OdsGroupPromotionInstanceMember ** " + value);
        Integer paidOrder = paidOrderState.value();
        BigDecimal paidAmount = paidAmountState.value();

        Integer refundOrder = refundOrderState.value();
        BigDecimal refundAmount = refundAmountState.value();

        //新客计算不用管是否支付
        if(value.getIsNewMember() == 1 && value.getRefundStatus() != 2 && !newUserCountState.contains(value.getMemberId()) ){
            newUserCountState.put(value.getMemberId(),1);
        }

        //支付
        if (value.getIsPaid() == 1 && value.getRefundStatus() != 2) {
            if (paidOrder == null) {
                paidOrderState.update(1);
            } else {
                paidOrderState.update(1 + paidOrder);
            }

            if (paidAmount == null) {
                paidAmountState.update(value.getActualOrderAmount());
            } else {
                paidAmountState.update(value.getActualOrderAmount().add(paidAmount));
            }
        }

        //退款
        if (value.getIsPaid() == 1 && value.getRefundStatus() == 2) {
            if (refundOrder == null) {
                refundOrderState.update(1);
            } else {
                refundOrderState.update(1 + refundOrder);
            }

            if (refundAmount == null) {
                refundAmountState.update(value.getActualOrderAmount());
            } else {
                refundAmountState.update(value.getActualOrderAmount().add(refundAmount));
            }
        }
    }
}
