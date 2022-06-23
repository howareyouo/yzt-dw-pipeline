package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.function.FilterPaidOrderFunction;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberLog;
import cn.yizhi.yzt.pipeline.model.fact.fullreduce.*;
import cn.yizhi.yzt.pipeline.model.fact.userfullreduce.FactUserAmount;
import cn.yizhi.yzt.pipeline.model.fact.userfullreduce.FactUserCount;
import cn.yizhi.yzt.pipeline.model.fact.userfullreduce.FactUserShare;
import cn.yizhi.yzt.pipeline.model.ods.*;
import cn.yizhi.yzt.pipeline.model.fact.fullreduce.FactFullReduceItemFullJoinSteam;
import cn.yizhi.yzt.pipeline.model.fact.fullreduce.FactFullReduceUserJoinSteam;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsFullReducePromotionOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsFullReducePromotionOrderItem;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author aorui created on 2020/11/4
 */
public class FactUserFullReduceSteamJob extends StreamJob {

    private static StateTtlConfig ttlConfig = StateTtlConfig
            //设置状态保留3天
            .newBuilder(org.apache.flink.api.common.time.Time.days(3))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {

        //满减活动记录流
        DataStream<OdsFullReducePromotionOrder> fullReducePromotionOrderDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_FULL_REDUCE_PROMOTION_ORDER, OdsFullReducePromotionOrder.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsFullReducePromotionOrder>() {
                    @Override
                    public long extractAscendingTimestamp(OdsFullReducePromotionOrder element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                })
                .keyBy(new KeySelector<OdsFullReducePromotionOrder, Tuple1<Integer>>() {

                    @Override
                    public Tuple1<Integer> getKey(OdsFullReducePromotionOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new RichFilterFunction<OdsFullReducePromotionOrder>() {

                    private transient MapState<Long, Boolean> paidOrderState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        // Tuple2记录了查找起始点和上次的订单总金额
                        MapStateDescriptor<Long, Boolean> descriptor =
                                new MapStateDescriptor<>(
                                        // the state name
                                        "paid-order-map",
                                        // type information
                                        Types.LONG,
                                        Types.BOOLEAN);
                        StateTtlConfig ttlConfig = StateTtlConfig
                                // 保留90天, 取决于订单生命周期的时间跨度
                                .newBuilder(org.apache.flink.api.common.time.Time.days(90))
                                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                                .build();
                        descriptor.enableTimeToLive(ttlConfig);
                        paidOrderState = getRuntimeContext().getMapState(descriptor);
                    }

                    @Override
                    public boolean filter(OdsFullReducePromotionOrder value) throws Exception {
                        if (paidOrderState.contains(value.getId())) {
                            return false;
                        }
                        boolean foundNewPaidOrder = false;
                        //已支付
                        if (value.getIsPaid() != null && value.getIsPaid() == 1) {
                            foundNewPaidOrder = true;
                        }
                        if (foundNewPaidOrder) {
                            paidOrderState.put(value.getId(), true);
                        }
                        return foundNewPaidOrder;
                    }
                });


        //满减活动商品项记录流
        DataStream<OdsFullReducePromotionOrderItem> odsFullReducePromotionOrderItemDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_FULL_REDUCE_PROMOTION_ORDER_ITEM, OdsFullReducePromotionOrderItem.class)
                .filter(new FilterFunction<OdsFullReducePromotionOrderItem>() {
                    @Override
                    public boolean filter(OdsFullReducePromotionOrderItem value) throws Exception {
                        return value.getProductId() != null;
                    }
                });

        //订单明细流
        DataStream<OdsEmallOrder> odsEmallOrderDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsEmallOrder>() {
                    @Override
                    public long extractAscendingTimestamp(OdsEmallOrder element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                }).keyBy(new KeySelector<OdsEmallOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new FilterPaidOrderFunction());

        //合并满减活动与活动记录
        DataStream<FactUserCount> factFullReduceItemJoinSteamDataStream = fullReducePromotionOrderDataStream.keyBy(new KeySelector<OdsFullReducePromotionOrder, Long>() {
            @Override
            public Long getKey(OdsFullReducePromotionOrder value) throws Exception {
                return value.getId();
            }
        }).intervalJoin(odsFullReducePromotionOrderItemDataStream.keyBy(new KeySelector<OdsFullReducePromotionOrderItem, Long>() {
            @Override
            public Long getKey(OdsFullReducePromotionOrderItem value) throws Exception {
                return value.getFullReducePromotionOrderId();
            }
        })).between(Time.minutes(-1), Time.minutes(1)).process(new ProcessJoinFunction<OdsFullReducePromotionOrder, OdsFullReducePromotionOrderItem, FactFullReduceItemFullJoinSteam>() {
            @Override
            public void processElement(OdsFullReducePromotionOrder left, OdsFullReducePromotionOrderItem right, Context ctx, Collector<FactFullReduceItemFullJoinSteam> out) throws Exception {
                out.collect(new FactFullReduceItemFullJoinSteam(left, right));
            }
        }).keyBy(new KeySelector<FactFullReduceItemFullJoinSteam, Tuple4<String, Integer, Integer, Integer>>() {
            @Override
            public Tuple4<String, Integer, Integer, Integer> getKey(FactFullReduceItemFullJoinSteam value) throws Exception {
                return new Tuple4<>(convertTimeStamp(value.getLeft().getCreatedAt()), value.getLeft().getShopId(), value.getLeft().getMemberId(), value.getLeft().getPromotionId());
            }
        }).process(new FactUserFullReduceResult())
                .uid("user-full-reduce-count")
                .name("user-full-reduce-count");

        //计算支付和优惠
        DataStream<FactUserAmount> userFullReduceAmountDs = fullReducePromotionOrderDataStream.keyBy(new KeySelector<OdsFullReducePromotionOrder, Long>() {

            @Override
            public Long getKey(OdsFullReducePromotionOrder value) throws Exception {
                return value.getOrderId();
            }
        }).intervalJoin(odsEmallOrderDataStream.keyBy(new KeySelector<OdsEmallOrder, Long>() {
            @Override
            public Long getKey(OdsEmallOrder value) throws Exception {
                return value.getId();
            }
        })).between(Time.minutes(-1), Time.minutes(1)).process(new ProcessJoinFunction<OdsFullReducePromotionOrder, OdsEmallOrder, FactFullReduceUserJoinSteam>() {

            @Override
            public void processElement(OdsFullReducePromotionOrder left, OdsEmallOrder right, Context ctx, Collector<FactFullReduceUserJoinSteam> out) throws Exception {
                out.collect(new FactFullReduceUserJoinSteam(left, right));
            }
        }).keyBy(new KeySelector<FactFullReduceUserJoinSteam, Tuple4<String, Integer, Integer, Integer>>() {
            @Override
            public Tuple4<String, Integer, Integer, Integer> getKey(FactFullReduceUserJoinSteam value) throws Exception {
                return new Tuple4<>(convertTimeStamp(value.getLeft().getCreatedAt()), value.getLeft().getShopId(), value.getLeft().getMemberId(), value.getLeft().getPromotionId());
            }
        }).process(new FactUserFullReduceAmountResult())
                .uid("user-full-reduce-amount")
                .name("user-full-reduce-amount");

        //用户分享统计
        DataStream<FactUserShare> memberDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_MEMBER_LOG, FactMemberLog.class)
                .filter(new FilterFunction<FactMemberLog>() {
                    @Override
                    public boolean filter(FactMemberLog value) throws Exception {
                        return value.getPromotionType() != null && value.getPromotionType() == 2;
                    }
                })
                .process(new ProcessFunction<FactMemberLog, FactUserShare>() {
                    @Override
                    public void processElement(FactMemberLog value, Context ctx, Collector<FactUserShare> out) throws Exception {
                        FactUserShare factUserFullReduce = new FactUserShare();
                        factUserFullReduce.setRowDate(value.getRowTime());
                        factUserFullReduce.setShopId(value.getShopId());
                        factUserFullReduce.setMemberId(value.getMemberId());
                        factUserFullReduce.setFullReduceId(value.getPromotionId());
                        factUserFullReduce.setShareCountFullReduce(value.getShareActivityCount());
                        out.collect(factUserFullReduce);
                    }
                })
                .uid("user-full-reduce-share")
                .name("user-full-reduce-share");

        //输出
        toJdbcUpsertSink(factFullReduceItemJoinSteamDataStream, Tables.SINK_TABLE_USER_FULL_REDUCE, FactUserCount.class);
        toJdbcUpsertSink(userFullReduceAmountDs, Tables.SINK_TABLE_USER_FULL_REDUCE, FactUserAmount.class);
        toJdbcUpsertSink(memberDs, Tables.SINK_TABLE_USER_FULL_REDUCE, FactUserShare.class);
    }

    public static class FactUserFullReduceResult extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, FactFullReduceItemFullJoinSteam, FactUserCount> {

        //通过满减活动的订单数
        private transient ListState<Long> orderIdState;

        //通过满减活动商品购买数
        private transient ValueState<Integer> quantityState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ListStateDescriptor<Long> orderIdStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "orderIdState",
                    // type information
                    Types.LONG);
            orderIdStateDescriptor.enableTimeToLive(ttlConfig);
            orderIdState = getRuntimeContext().getListState(orderIdStateDescriptor);

            ValueStateDescriptor<Integer> quantityStateDescriptor = new ValueStateDescriptor<Integer>(
                    // the state name
                    "quantityState",
                    // type information
                    Types.INT);
            quantityStateDescriptor.enableTimeToLive(ttlConfig);
            quantityState = getRuntimeContext().getState(quantityStateDescriptor);

        }

        @Override
        public void processElement(FactFullReduceItemFullJoinSteam value, Context ctx, Collector<FactUserCount> out) throws Exception {
            Integer quantity = quantityState.value();
            if (quantity == null) {
                quantityState.update(value.getRight().getQuantity());
            } else {
                quantityState.update(quantity + value.getRight().getQuantity());
            }

            orderIdState.add(value.getLeft().getOrderId());
            Set<Long> orderIdSet = new HashSet<>();
            orderIdState.get().forEach(e -> {
                orderIdSet.add(e);
            });

            //数据封装
            FactUserCount factUserFullReduce = new FactUserCount();
            Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            factUserFullReduce.setRowDate(currentKey.f0);
            factUserFullReduce.setShopId(currentKey.f1);
            factUserFullReduce.setMemberId(currentKey.f2);
            factUserFullReduce.setFullReduceId(currentKey.f3);
            factUserFullReduce.setOrderCount(orderIdSet.size());
            factUserFullReduce.setSaleCount(quantityState.value());
            out.collect(factUserFullReduce);
        }
    }

    public static class FactUserFullReduceAmountResult extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, FactFullReduceUserJoinSteam, FactUserAmount> {

        //活动商品销售额
        private transient ValueState<BigDecimal> actualAmountState;

        //优惠总金额
        private transient ValueState<BigDecimal> discountAmountState;

        //时间
        private transient ValueState<Timestamp> lastTimeState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<BigDecimal> actualAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "actualAmountState",
                    // type information
                    Types.BIG_DEC);
            actualAmountStateDescriptor.enableTimeToLive(ttlConfig);
            actualAmountState = getRuntimeContext().getState(actualAmountStateDescriptor);

            ValueStateDescriptor<BigDecimal> discountAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "discountAmountState",
                    // type information
                    Types.BIG_DEC);
            discountAmountStateDescriptor.enableTimeToLive(ttlConfig);
            discountAmountState = getRuntimeContext().getState(discountAmountStateDescriptor);

            ValueStateDescriptor<Timestamp> lastTimeStateDescriptor = new ValueStateDescriptor<Timestamp>(
                    // the state name
                    "lastTimeState",
                    // type information
                    Types.SQL_TIMESTAMP);
            lastTimeStateDescriptor.enableTimeToLive(ttlConfig);
            lastTimeState = getRuntimeContext().getState(lastTimeStateDescriptor);
        }

        @Override
        public void processElement(FactFullReduceUserJoinSteam value, Context ctx, Collector<FactUserAmount> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getRight().getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算
            long judge = oneTime + 172800000;

            //未超时进行计算
            if (judge > watermark) {
                BigDecimal paidAmount = actualAmountState.value();
                BigDecimal discountAmount = discountAmountState.value();
                if (paidAmount == null) {
                    actualAmountState.update(value.getRight().getActualAmount());
                } else {
                    actualAmountState.update(paidAmount.add(value.getRight().getActualAmount()));
                }

                if (discountAmount == null) {
                    discountAmountState.update(value.getRight().getDiscountAmount());
                } else {
                    discountAmountState.update(discountAmount.add(value.getRight().getDiscountAmount()));
                }

                Timestamp lastTime = lastTimeState.value();
                if (lastTime == null) {
                    lastTimeState.update(value.getRight().getPaidAt());
                } else {
                    lastTimeState.update(lastTime.after(value.getRight().getPaidAt()) ? lastTime : value.getRight().getPaidAt());
                }
            }

            //数据封装
            FactUserAmount factUserFullReduce = new FactUserAmount();
            Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            factUserFullReduce.setRowDate(currentKey.f0);
            factUserFullReduce.setShopId(currentKey.f1);
            factUserFullReduce.setMemberId(currentKey.f2);
            factUserFullReduce.setFullReduceId(currentKey.f3);
            factUserFullReduce.setPayAmount(actualAmountState.value());
            factUserFullReduce.setDiscountAmount(discountAmountState.value());
            factUserFullReduce.setLastJoinTime(lastTimeState.value());
            out.collect(factUserFullReduce);
        }
    }

    private static String convertTimeStamp(Timestamp timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd").format(timestamp);
    }
}
