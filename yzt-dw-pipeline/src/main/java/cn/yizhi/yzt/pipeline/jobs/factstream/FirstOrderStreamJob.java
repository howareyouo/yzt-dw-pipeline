package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jdbc.JdbcClient;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberFirstOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderPromotion;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hucheng
 * @date 2020/7/28 22:22
 */
public class FirstOrderStreamJob extends StreamJob {
    @Override
    public void defineJob() {

        DataStream<OdsEmallOrder> orderDs = this.createStreamFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsEmallOrder>() {

                    @Override
                    public long extractAscendingTimestamp(OdsEmallOrder element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });


        DataStream<OdsOrderPromotion> orderPromotionDs = this.createStreamFromKafka(SourceTopics.TOPIC_ORDER_PROMOTION, OdsOrderPromotion.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsOrderPromotion>() {

                    @Override
                    public long extractAscendingTimestamp(OdsOrderPromotion element) {
                        // OdsOrderPromotion没有时间戳，以processingTime代替。
                        return Instant.now().toEpochMilli();
                    }
                });

        DataStream<OdsEmallOrder> odsEmallOrderDataStream = orderDs.keyBy("shopId")
                .filter(new FilterPaidOrderFunction())
                .uid("paid-orders-filter")
                .name("paid-orders-filter");

        //创建广播流
        MapStateDescriptor<Long, List<OdsOrderPromotion>> promotionMapStateDescriptor = new MapStateDescriptor<Long, List<OdsOrderPromotion>>(
                "promotionState",
                Types.LONG,
                Types.LIST(TypeInformation.of(OdsOrderPromotion.class))
        );
        BroadcastStream<OdsOrderPromotion> broadcastStream = orderPromotionDs.broadcast(promotionMapStateDescriptor);

        //连接广播流
        DataStream<OrderAndPromotion> process = odsEmallOrderDataStream
            .connect(broadcastStream)
            .process(new BroadcastProcessFunction<OdsEmallOrder, OdsOrderPromotion, OrderAndPromotion>() {
            @Override
            public void processElement(OdsEmallOrder value, ReadOnlyContext ctx, Collector<OrderAndPromotion> out) throws Exception {

                ReadOnlyBroadcastState<Long, List<OdsOrderPromotion>> broadcastState = ctx.getBroadcastState(promotionMapStateDescriptor);
                OrderAndPromotion orderAndPromotion = new OrderAndPromotion();
                orderAndPromotion.order = value;
                if (broadcastState.contains(value.getId())) {
                    List<OdsOrderPromotion> odsOrderPromotionList = broadcastState.get(value.getId());
                    for (OdsOrderPromotion odsOrderPromotion : odsOrderPromotionList) {
                        orderAndPromotion.promotion = odsOrderPromotion;
                        out.collect(orderAndPromotion);
                    }
                } else {
                    out.collect(orderAndPromotion);
                }
            }

            @Override
            public void processBroadcastElement(OdsOrderPromotion value, Context ctx, Collector<OrderAndPromotion> out) throws Exception {
                //先拿出存在的广播流
                BroadcastState<Long, List<OdsOrderPromotion>> broadcastState = ctx.getBroadcastState(promotionMapStateDescriptor);

                //如果已存在key
                if (broadcastState.contains(Long.valueOf(value.getOrderId()))) {
                    List<OdsOrderPromotion> odsOrderPromotions = broadcastState.get(Long.valueOf(value.getOrderId()));
                    odsOrderPromotions.add(value);
                    broadcastState.put(Long.valueOf(value.getOrderId()), odsOrderPromotions);
                } else {
                    //不存在key 则重新加key
                    List<OdsOrderPromotion> promotionList = new ArrayList<>();
                    promotionList.add(value);
                    broadcastState.put(Long.valueOf(value.getOrderId()), promotionList);
                }

            }
        }).uid("order-promotion-id").name("order-promotion");


        DataStream<FactMemberFirstOrder> ds = process.keyBy("order.shopId", "order.memberId")
                .map(new NewCustomerMapFunction(this.serverConfig))
                .filter(new FilterFunction<FactMemberFirstOrder>() {
                    @Override
                    public boolean filter(FactMemberFirstOrder value) throws Exception {
                        return value != null;
                    }
                })
                .returns(FactMemberFirstOrder.class)
                .uid("first-order-map-processor-id")
                .name("first-order-promotion-map-processor");

        //写入数据到kafka
        toJdbcUpsertSink(ds, "fact_member_first_order", FactMemberFirstOrder.class);

    }

    public static class FilterPaidOrderFunction extends RichFilterFunction<OdsEmallOrder> {

        // 只在KeyedStream上使用时才可以访问
        // All state collection types support per-entry TTLs. This means that list elements and map entries expire independently.
        private transient MapState<Long, Boolean> paidOrderState;

        @Override
        public boolean filter(OdsEmallOrder value) throws Exception {

            if (paidOrderState.contains(value.getId())) {
                return false;
            }

            boolean foundNewPaidOrder = false;


            // 普通订单支付后的状态为1, 虚拟商品/电子卡券的支付后状态可能是2或者3
            if (value.getStatus() == 1 || value.getStatus() == 2 || value.getStatus() == 3) {
                foundNewPaidOrder = true;
            }

            if (foundNewPaidOrder) {
                paidOrderState.put(value.getId(), true);
            }

            return foundNewPaidOrder;
        }

        @Override
        public void open(Configuration conf) {
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
    }


    public static class OrderAndPromotion {
        public OdsEmallOrder order;
        public OdsOrderPromotion promotion;

    }

    public static class NewCustomerMapFunction extends RichMapFunction<OrderAndPromotion, FactMemberFirstOrder> {

        // per user in one shop
        private transient MapState<Long, FactMemberFirstOrder> memberFirstOrderState;

        private transient JdbcClient jdbcClient;
        private ServerConfig serverConfig;

        public NewCustomerMapFunction(ServerConfig config) {
            this.serverConfig = config;
        }

        @Override
        public FactMemberFirstOrder map(OrderAndPromotion value) throws Exception {
            // 检查state里是否有记录，没有则从DB里查询
            if (!memberFirstOrderState.isEmpty()) {
                if (memberFirstOrderState.contains(value.order.getId())) {
                    return build(value.order, value.promotion);
                }
                return null;
            }

            List<FactMemberFirstOrder> firstOrders = findInDB(value.order.getShopId(), value.order.getMemberId());

            //需要判断此订单不是刚入库的订单
            if (firstOrders.size() > 0) {
                memberFirstOrderState.put(firstOrders.get(0).getOrderId(), firstOrders.get(0));
                if (firstOrders.get(0).getOrderId().equals(value.order.getId())) {
                    return build(value.order, value.promotion);
                }
                return null;
            }

            // else: 未从ODS中找到，这个订单为首笔订单
            memberFirstOrderState.put(value.order.getId(), build(value.order, value.promotion));

            return build(value.order, value.promotion);
        }


        @Override
        public void open(Configuration conf) {
            // Tuple2记录了查找起始点和上次的订单总金额
            MapStateDescriptor<Long, FactMemberFirstOrder> descriptor =
                    new MapStateDescriptor<Long, FactMemberFirstOrder>(
                            // the state name
                            "member-first-order",
                            // type information,
                            Types.LONG,
                            TypeInformation.of(FactMemberFirstOrder.class));


            memberFirstOrderState = getRuntimeContext().getMapState(descriptor);

            //memberFirstOrderState.clear();
            jdbcClient = new JdbcClient(serverConfig);

        }

        private List<FactMemberFirstOrder> findInDB(int shopId, int memberId) {
            List<OdsEmallOrder> memberOrders = jdbcClient.query("select id as id,main_shop_id as mainShopId,shop_id as shopId," +
                            " member_id as memberId,union_no as unionNo,order_no as orderNo,created_at as createdAt," +
                            " paid_at as paidAt,payment_method as paymentMethod ,transaction_no as transactionNo,source as source" +
                            " from ods_emall_order where shop_id=? and member_id=? and paid_at is not null order by created_at asc limit 1",
                    new Object[]{shopId, memberId},
                    OdsEmallOrder.class);

            List<FactMemberFirstOrder> firstOrders = new ArrayList<>();

            if (memberOrders != null && memberOrders.size() > 0) {
                OdsEmallOrder found = memberOrders.get(0);
                List<OdsOrderPromotion> promotions = jdbcClient.query(" select id as id,order_id as orderId,promotion_type as promotionType,promotion_id as promotionId," +
                                " discount_amount as discountAmount from ods_order_promotion where order_id = ?",
                        new Object[]{found.getId()}, OdsOrderPromotion.class);
                if (promotions != null && promotions.size() > 0) {
                    promotions.forEach(p -> {
                        firstOrders.add(build(found, p));
                    });
                } else {
                    firstOrders.add(build(found, null));
                }
            }

            return firstOrders;
        }

        private FactMemberFirstOrder build(OdsEmallOrder order, OdsOrderPromotion promotion) {
            FactMemberFirstOrder firstOrder = new FactMemberFirstOrder();

            firstOrder.setOrderId(order.getId());
            firstOrder.setMainShopId(order.getMainShopId());
            firstOrder.setShopId(order.getShopId());
            firstOrder.setMemberId(order.getMemberId());
            firstOrder.setUnionNo(order.getUnionNo());
            firstOrder.setOrderNo(order.getOrderNo());
            firstOrder.setCreatedAt(order.getCreatedAt());
            firstOrder.setPaidAt(order.getPaidAt());
            firstOrder.setPaymentMethod(order.getPaymentMethod());
            firstOrder.setTransactionNo(order.getTransactionNo());
            firstOrder.setSource(order.getSource());

            //不是活动订单
            if (promotion != null) {
                firstOrder.setPromotionId(promotion.getPromotionId());
                firstOrder.setPromotionType(promotion.getPromotionType());
            } else {
                firstOrder.setPromotionId(0);
                firstOrder.setPromotionType(0);
            }

            return firstOrder;
        }


    }

}
