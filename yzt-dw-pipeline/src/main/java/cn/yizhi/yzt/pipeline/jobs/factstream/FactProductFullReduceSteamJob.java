package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.function.FilterPaidOrderFunction;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactProductFullReduce;
import cn.yizhi.yzt.pipeline.model.fact.fullreduce.FactFullReduceItemJoinSteam;
import cn.yizhi.yzt.pipeline.model.fact.fullreduce.FactFullReduceJoinSteam;
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
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * @author aorui created on 2020/11/2
 */
public class FactProductFullReduceSteamJob extends StreamJob {

    private static StateTtlConfig ttlConfig = StateTtlConfig
            //设置状态保留3天
            .newBuilder(org.apache.flink.api.common.time.Time.days(3))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {

        //订单明细流
        DataStream<OdsEmallOrder> odsEmallOrderDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsEmallOrder>() {
                    @Override
                    public long extractAscendingTimestamp(OdsEmallOrder element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                })
                .keyBy(new KeySelector<OdsEmallOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new FilterPaidOrderFunction());

        //满减活动记录流
        DataStream<OdsFullReducePromotionOrder> fullReducePromotionDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_FULL_REDUCE_PROMOTION_ORDER, OdsFullReducePromotionOrder.class)
                .keyBy(new KeySelector<OdsFullReducePromotionOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsFullReducePromotionOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new RichFilterFunction<OdsFullReducePromotionOrder>() {
                    // 只在KeyedStream上使用时才可以访问
                    private transient MapState<Long, Boolean> paidOrderState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        // Tuple2记录了查找起始点和上次的订单总金额
                        MapStateDescriptor<Long, Boolean> descriptor =
                                new MapStateDescriptor<>(
                                        // the state name
                                        "full-reduce-promotion-order-map",
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
                        //只要支付过就算支付订单数据
                        if (value.getIsPaid() == 1) {
                            foundNewPaidOrder = true;
                        }
                        if (foundNewPaidOrder) {
                            paidOrderState.put(value.getId(), true);
                        }
                        return foundNewPaidOrder;
                    }
                }).assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsFullReducePromotionOrder>() {
                    @Override
                    public long extractAscendingTimestamp(OdsFullReducePromotionOrder element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
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

        //合并满减活动与活动记录
        DataStream<FactFullReduceItemJoinSteam> factFullReduceItemJoinSteamDataStream = fullReducePromotionDataStream
                .keyBy((KeySelector<OdsFullReducePromotionOrder, Long>) value -> value.getId())
                .intervalJoin(odsFullReducePromotionOrderItemDataStream
                        .keyBy((KeySelector<OdsFullReducePromotionOrderItem, Long>) value -> value.getFullReducePromotionOrderId()))
                .between(Time.minutes(-1), Time.minutes(1)).process(new ProcessJoinFunction<OdsFullReducePromotionOrder, OdsFullReducePromotionOrderItem, FactFullReduceItemJoinSteam>() {
                    @Override
                    public void processElement(OdsFullReducePromotionOrder left, OdsFullReducePromotionOrderItem right, Context ctx, Collector<FactFullReduceItemJoinSteam> out) throws Exception {
                        FactFullReduceItemJoinSteam factFullReduceItemJoinSteam = new FactFullReduceItemJoinSteam();
                        factFullReduceItemJoinSteam.setOrderId(left.getOrderId());
                        factFullReduceItemJoinSteam.setCreatedAt(left.getCreatedAt());
                        factFullReduceItemJoinSteam.setDiscountAmount(right.getDiscountAmount());
                        factFullReduceItemJoinSteam.setProductId(right.getProductId());
                        factFullReduceItemJoinSteam.setPromotionId(left.getPromotionId());
                        factFullReduceItemJoinSteam.setQuantity(right.getQuantity());
                        factFullReduceItemJoinSteam.setTotalPrice(right.getTotalPrice());
                        factFullReduceItemJoinSteam.setUpdatedAt(left.getUpdatedAt());
                        out.collect(factFullReduceItemJoinSteam);
                    }
                })
                .uid("promotion-item-join-stream")
                .name("promotion-item-join-stream");

        //合并明细活动记录
        DataStream<FactProductFullReduce> factProductFullReduceDataStream = odsEmallOrderDataStream.keyBy(new KeySelector<OdsEmallOrder, Long>() {
                    @Override
                    public Long getKey(OdsEmallOrder value) throws Exception {
                        return value.getId();
                    }
                }).intervalJoin(factFullReduceItemJoinSteamDataStream.keyBy(new KeySelector<FactFullReduceItemJoinSteam, Long>() {
                    @Override
                    public Long getKey(FactFullReduceItemJoinSteam value) throws Exception {
                        return value.getOrderId();
                    }
                })).between(Time.minutes(-1), Time.minutes(1)).process(new ProcessJoinFunction<OdsEmallOrder, FactFullReduceItemJoinSteam, FactFullReduceJoinSteam>() {
                    @Override
                    public void processElement(OdsEmallOrder left, FactFullReduceItemJoinSteam right, Context ctx, Collector<FactFullReduceJoinSteam> out) throws Exception {
                        out.collect(new FactFullReduceJoinSteam(left, right));
                    }
                }).keyBy(new KeySelector<FactFullReduceJoinSteam, Tuple4<String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, Integer> getKey(FactFullReduceJoinSteam value) throws Exception {
                        return new Tuple4<String, Integer, Integer, Integer>(convertTimeStamp(value.getOdsEmallOrder().getUpdatedAt()), value.getOdsEmallOrder().getShopId(), value.getFactFullReduceItemJoinSteam().getPromotionId(), value.getFactFullReduceItemJoinSteam().getProductId());
                    }
                }).process(new factProductFullReduceResult())
                .uid("fact-product-item-full-reduce")
                .name("fact-product-item-full-reduce");

        //输出
        toKafkaSink(factProductFullReduceDataStream, SourceTopics.TOPIC_PRODUCT_FULL_REDUCE);
        //输出到数据库
        toJdbcUpsertSink(factProductFullReduceDataStream, Tables.SINK_TABLE_PRODUCT_FULL_REDUCE, FactProductFullReduce.class);
    }


    public static class factProductFullReduceResult extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, FactFullReduceJoinSteam, FactProductFullReduce> {

        //活动商品销售额
        private transient ValueState<BigDecimal> totalPriceState;

        //优惠金额
        private transient ValueState<BigDecimal> discountAmountState;

        //销量
        private transient ValueState<Integer> quantityState;

        //付款单数
        private transient ListState<Long> emallOrderIdState;

        //人数
        private transient ListState<Integer> memberIdState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<BigDecimal> totalPriceStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "totalPriceState",
                    // type information
                    Types.BIG_DEC);
            totalPriceStateDescriptor.enableTimeToLive(ttlConfig);
            totalPriceState = getRuntimeContext().getState(totalPriceStateDescriptor);

            ValueStateDescriptor<BigDecimal> discountAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "discountAmountState",
                    // type information
                    Types.BIG_DEC);
            discountAmountStateDescriptor.enableTimeToLive(ttlConfig);
            discountAmountState = getRuntimeContext().getState(discountAmountStateDescriptor);

            ValueStateDescriptor<Integer> quantityStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "quantityState",
                    // type information
                    Types.INT);
            quantityStateDescriptor.enableTimeToLive(ttlConfig);
            quantityState = getRuntimeContext().getState(quantityStateDescriptor);

            ListStateDescriptor<Long> emallOrderIdStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "emallOrderIdState",
                    // type information
                    Types.LONG);
            emallOrderIdStateDescriptor.enableTimeToLive(ttlConfig);
            emallOrderIdState = getRuntimeContext().getListState(emallOrderIdStateDescriptor);

            ListStateDescriptor<Integer> memberIdStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "memberIdState",
                    // type information
                    Types.INT);
            memberIdStateDescriptor.enableTimeToLive(ttlConfig);
            memberIdState = getRuntimeContext().getListState(memberIdStateDescriptor);
        }

        @Override
        public void processElement(FactFullReduceJoinSteam value, Context ctx, Collector<FactProductFullReduce> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getOdsEmallOrder().getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算
            long judge = oneTime + 172800000;

            //未超时进行计算
            if (judge > watermark) {
                BigDecimal paidAmount = totalPriceState.value();
                BigDecimal discountAmount = discountAmountState.value();
                Integer quantity = quantityState.value();
                if (paidAmount == null) {
                    totalPriceState.update(value.getFactFullReduceItemJoinSteam().getTotalPrice());
                } else {
                    totalPriceState.update(paidAmount.add(value.getFactFullReduceItemJoinSteam().getTotalPrice()));
                }

                if (discountAmount == null) {
                    discountAmountState.update(value.getFactFullReduceItemJoinSteam().getDiscountAmount());
                } else {
                    discountAmountState.update(discountAmount.add(value.getFactFullReduceItemJoinSteam().getDiscountAmount()));
                }
                if (quantity == null) {
                    quantityState.update(value.getFactFullReduceItemJoinSteam().getQuantity());
                } else {
                    quantityState.update(quantity + value.getFactFullReduceItemJoinSteam().getQuantity());
                }

                emallOrderIdState.add(value.getOdsEmallOrder().getId());
            }

            memberIdState.add(value.getOdsEmallOrder().getMemberId());

            //去重统计
            Set<Long> payCount = new HashSet<>();
            emallOrderIdState.get().forEach(e -> {
                payCount.add(e);
            });

            Set<Integer> memberId = new HashSet<>();
            memberIdState.get().forEach(e -> {
                memberId.add(e);
            });

            //数据封装
            FactProductFullReduce factProductFullReduce = new FactProductFullReduce();
            Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            factProductFullReduce.setRowDate(currentKey.f0);
            factProductFullReduce.setShopId(currentKey.f1);
            factProductFullReduce.setFullReduceId(currentKey.f2);
            factProductFullReduce.setProductId(currentKey.f3);
            factProductFullReduce.setPayCount(payCount.size());
            factProductFullReduce.setPayNumber(memberId.size());
            factProductFullReduce.setSaleTotal(totalPriceState.value().subtract(discountAmountState.value()));
            factProductFullReduce.setSaleCount(quantityState.value());
            factProductFullReduce.setDiscountAmount(discountAmountState.value());
            out.collect(factProductFullReduce);
        }
    }

    private static String convertTimeStamp(Timestamp timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd").format(timestamp);
    }
}