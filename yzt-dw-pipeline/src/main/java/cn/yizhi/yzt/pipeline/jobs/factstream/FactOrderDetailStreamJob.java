package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.util.Beans;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderItem;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotionItem;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderItem;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderPromotion;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * @author hucheng
 * 订单相关事实明细表
 * 主要做order_promotion emall_order  order_item 三张表的关联，方便后续计算订单指标
 * emall_order.id = order_promotion.order_id = order_item.order_id
 * 订单失效的最长时间是24小时
 * 关联后消息发送到kafka，下游再消费计算
 * 发出的订单状态统一为 0为支付  1已支付  2已退款（暂时无）
 * 需要保证一个订单状态数据只会发送一次
 * 接收方处理数据的时候可能会有数据重复和关联错误，需做相应处理，因为order_promotion和order_item没有强关联性
 * @date 2020/10/27 10:07
 */
public class FactOrderDetailStreamJob extends StreamJob {
    @Override
    public void defineJob() throws Exception {
        //接收流
        DataStream<OdsEmallOrder> emallOrderDs = this.createStreamFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class)
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

        DataStream<OdsOrderPromotion> orderPromotionDs = this.createStreamFromKafka(SourceTopics.TOPIC_ORDER_PROMOTION, OdsOrderPromotion.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsOrderPromotion>() {

                    @Override
                    public long extractAscendingTimestamp(OdsOrderPromotion element) {
                        return element.getCreatedAt().getTime();
                    }
                });

        DataStream<OdsOrderItem> odsOrderItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_ORDER_ITEM, OdsOrderItem.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsOrderItem>() {
                    @Override
                    public long extractAscendingTimestamp(OdsOrderItem element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });


        DataStream<OdsEmallOrder> factOrderDs = emallOrderDs
                .keyBy(new KeySelector<OdsEmallOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new OrderFilter())
                .uid("order-filter")
                .name("order-filter");

        //做关联 订单商品关联
        DataStream<FactOrderItem> factOrderItemDs = factOrderDs
                .keyBy(new KeySelector<OdsEmallOrder, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getId());
                    }
                })
                .intervalJoin(odsOrderItemDs.keyBy(new KeySelector<OdsOrderItem, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> getKey(OdsOrderItem value) throws Exception {
                        return new Tuple1<>(value.getOrderId());
                    }
                }))
                .between(Time.days(-7), Time.days(7))
                .process(new ProcessJoinFunction<OdsEmallOrder, OdsOrderItem, FactOrderItem>() {
                    @Override
                    public void processElement(OdsEmallOrder left, OdsOrderItem right, Context ctx, Collector<FactOrderItem> out) throws Exception {
                        //订单和商品一定会有关联
                        out.collect(convertToOderItem(left, right));
                    }
                })
                .uid("order-item-interval-join")
                .name("order-item-interval-join");

        //订单活动关联（只有活动订单才会发出数据）
        DataStream<FactOrderPromotionItem> factOrderPromotionItemDs = factOrderItemDs
                .keyBy(new KeySelector<FactOrderItem, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> getKey(FactOrderItem value) throws Exception {
                        return new Tuple1<>(value.getOrderId());
                    }
                })
                .intervalJoin(orderPromotionDs.keyBy(new KeySelector<OdsOrderPromotion, Tuple1<Long>>() {
                    @Override
                    public Tuple1<Long> getKey(OdsOrderPromotion value) throws Exception {
                        return new Tuple1<>(value.getOrderId().longValue());
                    }
                }))
                .between(Time.days(-7), Time.days(7))
                .process(new ProcessJoinFunction<FactOrderItem, OdsOrderPromotion, FactOrderPromotionItem>() {
                    @Override
                    public void processElement(FactOrderItem left, OdsOrderPromotion right, Context ctx, Collector<FactOrderPromotionItem> out) throws Exception {
                        out.collect(convertToFactOrderPromotionItem(left, right));
                    }
                })
                .uid("order-promotion-interval-join")
                .name("order-promotion-interval-join");

        //数据写入kafka
        toKafkaSink(factOrderPromotionItemDs, SourceTopics.TOPIC_FACT_ORDER_PROMOTION_ITEM);
        toKafkaSink(factOrderItemDs, SourceTopics.TOPIC_FACT_ORDER_ITEM);
        toKafkaSink(factOrderDs, SourceTopics.TOPIC_FACT_ORDER);
    }

    //过滤订单
    public static class OrderFilter extends RichFilterFunction<OdsEmallOrder> {
        //订单state
        private transient MapState<Long, Boolean> paidOrderState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
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
        public boolean filter(OdsEmallOrder value) throws Exception {
            if (paidOrderState.contains(value.getId())) {
                if (!paidOrderState.get(value.getId())) {
                    if (value.getTransactionNo() != null) {
                        paidOrderState.put(value.getId(), true);
                        //状态为已付款 1
                        value.setStatus(1);
                        return true;
                    }
                }
            } else {
                if (value.getTransactionNo() == null || value.getTransactionNo().equals("")) {
                    paidOrderState.put(value.getId(), false);

                    //状态为未付款 0
                    value.setStatus(0);
                } else {
                    paidOrderState.put(value.getId(), true);

                    //状态为已付款 1
                    value.setStatus(1);
                }

                return true;
            }

            return false;
        }
    }

    private static FactOrderItem convertToOderItem(OdsEmallOrder emallOrder, OdsOrderItem odsOrderItem) {

        FactOrderItem factOrderItem = Beans.copyProperties(emallOrder, FactOrderItem.class);

        //特殊数据单独处理
        if (emallOrder != null) {
            factOrderItem.setOrderId(emallOrder.getId());
        }


        if (odsOrderItem != null) {
            factOrderItem.setProductId(odsOrderItem.getProductId());
            factOrderItem.setProductType(odsOrderItem.getProductType());
            factOrderItem.setSkuId(odsOrderItem.getSkuId());
            factOrderItem.setQuantity(odsOrderItem.getQuantity());
            factOrderItem.setRetailPrice(odsOrderItem.getRetailPrice());
            factOrderItem.setIsGiveaway(odsOrderItem.getIsGiveaway());
            factOrderItem.setProductActualAmount(odsOrderItem.getActualAmount());
            factOrderItem.setProductDiscountAmount(odsOrderItem.getDiscountAmount());
            factOrderItem.setName(odsOrderItem.getName());
            factOrderItem.setShowVoucher(odsOrderItem.getShowVoucher());
            factOrderItem.setParentId(odsOrderItem.getParentId());
        }

        return factOrderItem;
    }


    private static FactOrderPromotionItem convertToFactOrderPromotionItem(FactOrderItem factOrderItem, OdsOrderPromotion odsOrderPromotion) throws Exception {

        FactOrderPromotionItem factOrderPromotionItem = Beans.copyProperties(factOrderItem, FactOrderPromotionItem.class);

        if (odsOrderPromotion != null) {
            factOrderPromotionItem.setPromotionId(odsOrderPromotion.getPromotionId());
            factOrderPromotionItem.setPromotionType(odsOrderPromotion.getPromotionType());
            factOrderPromotionItem.setPromotionDiscountAmount(odsOrderPromotion.getDiscountAmount());
        }
        return factOrderPromotionItem;
    }
}