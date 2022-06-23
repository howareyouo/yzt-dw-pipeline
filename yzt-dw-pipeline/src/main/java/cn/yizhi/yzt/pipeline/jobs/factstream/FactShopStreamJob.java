package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.util.Beans;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.function.FilterOrderPromotionFunction;
import cn.yizhi.yzt.pipeline.function.FilterRefundFunction;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderItem;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotion;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotionItem;
import cn.yizhi.yzt.pipeline.model.fact.shop.*;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import cn.yizhi.yzt.pipeline.model.ods.ShopMember;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import cn.yizhi.yzt.pipeline.model.fact.shop.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hucheng
 * @date 2020/6/24 11:01
 */
public abstract class FactShopStreamJob extends StreamJob {
    //店铺统一设置成90天
    private static final StateTtlConfig ttlConfig = StateTtlConfig
            .newBuilder(org.apache.flink.api.common.time.Time.days(90))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    private static final String jobDescription = "店铺指标计算";

    @Override
    public String getJobDescription() {
        return jobDescription;
    }

    protected void defineJobShopJob(String format, String table) {
        DataStream<FactOrderPromotionItem> factOrderPromotionItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_PROMOTION_ITEM, FactOrderPromotionItem.class);

        DataStream<FactOrderItem> factOrderItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_ITEM, FactOrderItem.class);

        DataStream<OdsLogStream> odsLogStreamDs = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, OdsLogStream.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsLogStream>() {
                    @Override
                    public long extractAscendingTimestamp(OdsLogStream element) {
                        return element.getEventTime().getTime();
                    }
                });

        DataStream<OdsEmallOrder> factOrderDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER, OdsEmallOrder.class);

        //接收流
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
                });

        //接收会员信息，求新注册会员数
        DataStream<ShopMember> shopMemberDs = this.createStreamFromKafka(SourceTopics.TOPIC_SHOP_MEMBER, ShopMember.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ShopMember>() {
                    @Override
                    public long extractAscendingTimestamp(ShopMember element) {
                        return element.getCreatedAt().getTime();
                    }
                });


        DataStream<OdsLogStream> filterLogStream = odsLogStreamDs
                .filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        String eventName = value.getEventName();
                        return value.getShopId() != null && value.getShopId() != 0 && value.getEndTime() == null
                                && ("ViewShop".equals(eventName) || "ShareShop".equals(eventName)
                                || "ViewGoods".equals(eventName) || "ViewGoodsByActivity".equals(eventName)
                                || "AddToShoppingCart".equals(eventName) || "AddToShoppingCartByActivity".equals(eventName));
                    }
                });

        //店铺日志-分渠道
        DataStream<FactShopLog> factShopLogSingleDs = filterLogStream
                .keyBy(new KeySelector<OdsLogStream, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getEventTime(), format), value.getTaroEnv(), value.getShopId());
                    }
                })
                .process(new FactShopLogStreamProcessFunction())
                .uid("log-time-day-shop-mainShopId-channel-process")
                .name("log-time-day-shop-mainShopId-channel-process");


        //店铺日志-全渠道
        DataStream<FactShopLog> factShopLogSingleAllChannelDs = filterLogStream
                .keyBy(new KeySelector<OdsLogStream, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getEventTime(), format), "all", value.getShopId());
                    }
                })
                .process(new FactShopLogStreamProcessFunction())
                .uid("log-time-day-shop-mainShopId-all-process")
                .name("log-time-day-shop-mainShopId-all-process");


        //下单-分渠道
        DataStream<OdsEmallOrder> filterFactOrder = factOrderDs
                .filter(new FilterFunction<OdsEmallOrder>() {
                    @Override
                    public boolean filter(OdsEmallOrder value) throws Exception {
                        return value.getStatus() == 0 || value.getActualAmount().compareTo(BigDecimal.ZERO) <= 0;
                    }
                });

        DataStream<FactShopOrder> factShopOrderDs = filterFactOrder
                .keyBy(new KeySelector<OdsEmallOrder, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), value.getSource(), value.getShopId());
                    }
                })
                .process(new FactShopOrderProcessFunction())
                .uid("order-time-day-shop-channel-process")
                .name("order-time-day-shop-channel-process");

        //下单-全渠道
        DataStream<FactShopOrder> factShopOrderAllChannelDs = filterFactOrder
                .keyBy(new KeySelector<OdsEmallOrder, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), "all", value.getShopId());
                    }
                })
                .process(new FactShopOrderProcessFunction())
                .uid("order-time-day-shop-all-process")
                .name("order-time-day-shop-all-process");

        //支付
        DataStream<OdsEmallOrder> filterFactOrderPaid = factOrderDs
                .filter(new FilterFunction<OdsEmallOrder>() {
                    @Override
                    public boolean filter(OdsEmallOrder value) throws Exception {
                        return value.getStatus() == 1;
                    }
                });

        DataStream<FactShopPay> factShopPayDs = filterFactOrderPaid
                .keyBy(new KeySelector<OdsEmallOrder, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), value.getSource(), value.getShopId());
                    }
                })
                .process(new FactShopPayProcessFunction())
                .uid("paid-time-day-shop-channel-process")
                .name("paid-time-day-shop-channel-process");

        DataStream<FactShopPay> factShopPayDsAllChannel = filterFactOrderPaid
                .keyBy(new KeySelector<OdsEmallOrder, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), "all", value.getShopId());
                    }
                })
                .process(new FactShopPayProcessFunction())
                .uid("paid-time-day-shop-all-process")
                .name("paid-time-day-shop-all-process");


        //退款
        DataStream<OdsEmallOrder> filterEmallOrderRefund = orderDs
                .keyBy(new KeySelector<OdsEmallOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new FilterRefundFunction())
                .uid("order-filter-refund")
                .name("order-filter-refund");

        DataStream<FactShopRefund> factShopRefundDs = filterEmallOrderRefund
                .keyBy(new KeySelector<OdsEmallOrder, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), value.getSource(), value.getShopId());
                    }
                })
                .process(new FactShopRefundProcessFunction())
                .uid("refund-time-day-shop-channel-process")
                .name("refund-time-day-shop-channel-process");

        DataStream<FactShopRefund> factShopRefundAllChannelDs = filterEmallOrderRefund
                .keyBy(new KeySelector<OdsEmallOrder, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), "all", value.getShopId());
                    }
                })
                .process(new FactShopRefundProcessFunction())
                .uid("refund-time-day-shop-all-process")
                .name("refund-time-day-shop-all-process");


        //商品销量(需支付才能算销量)
        DataStream<FactOrderItem> orderItemFilterDs = factOrderItemDs.filter(new FilterFunction<FactOrderItem>() {
            @Override
            public boolean filter(FactOrderItem value) throws Exception {
                return value.getStatus() == 1;
            }
        });
        DataStream<FactShopProductSold> factShopProductSoldDs = orderItemFilterDs
                .keyBy(new KeySelector<FactOrderItem, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(FactOrderItem value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), value.getSource(), value.getShopId());
                    }
                }).process(new FactShopSoldCountProcessFunction())
                .uid("soldCount-time-day-shop-channel-process")
                .name("soldCount-time-day-shop-channel-process");

        DataStream<FactShopProductSold> factShopProductSoldAllChannelDs = orderItemFilterDs
                .keyBy(new KeySelector<FactOrderItem, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(FactOrderItem value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), "all", value.getShopId());
                    }
                }).process(new FactShopSoldCountProcessFunction())
                .uid("soldCount-time-day-shop-all-process")
                .name("soldCount-time-day-shop-all-process");

        //活动订单总额和优惠总金额
        DataStream<FactOrderPromotion> factOrderPromotionDs = factOrderPromotionItemDs
                .map(new MapFunction<FactOrderPromotionItem, FactOrderPromotion>() {
                    @Override
                    public FactOrderPromotion map(FactOrderPromotionItem value) throws Exception {
                        return Beans.copyProperties(value, FactOrderPromotion.class);
                    }
                })
                .filter(new FilterFunction<FactOrderPromotion>() {
                    @Override
                    public boolean filter(FactOrderPromotion value) throws Exception {
                        return value.getPromotionId() != null && value.getPromotionType() != null && value.getStatus() == 1;
                    }
                })
                .keyBy(new KeySelector<FactOrderPromotion, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactOrderPromotion value) throws Exception {
                        return new Tuple2<>(value.getPromotionType(), value.getPromotionId());
                    }
                })
                .filter(new FilterOrderPromotionFunction())
                .uid("paid-order-filter")
                .name("paid-order-filter");

        DataStream<FactShopPromotion> factShopPromotionDs = factOrderPromotionDs
                .keyBy(new KeySelector<FactOrderPromotion, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(FactOrderPromotion value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), value.getSource(), value.getShopId());
                    }
                })
                .process(new FactShopPromotionProcessFunction())
                .uid("promotion-time-day-shop-channel-process")
                .name("promotion-time-day-shop-channel-process");


        DataStream<FactShopPromotion> factShopPromotionAllChannelDs = factOrderPromotionDs
                .keyBy(new KeySelector<FactOrderPromotion, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(FactOrderPromotion value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), "all", value.getShopId());
                    }
                })
                .process(new FactShopPromotionProcessFunction())
                .uid("promotion-time-day-shop-all-process")
                .name("promotion-time-day-shop-all-process");

        //新注册用户
        DataStream<ShopMember> shopMemberFilterDs = shopMemberDs
                .keyBy(new KeySelector<ShopMember, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(ShopMember value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                })
                .filter(new ShopMemberFilterFunction())
                .uid("shop-member-filter")
                .name("shop-member-filter");

        DataStream<FactShopNewMember> factShopNewMemberDs = shopMemberFilterDs
                .map(new MapFunction<ShopMember, ShopMember>() {
                    @Override
                    public ShopMember map(ShopMember value) throws Exception {
                        if (!"wxapp".equals(value.getSource())) {
                            value.setSource("web");
                        }
                        return value;
                    }
                })
                .keyBy(new KeySelector<ShopMember, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(ShopMember value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), value.getSource(), value.getShopId());
                    }
                }).process(new FactShopNewMemberFunction())
                .uid("newMember-time-day-shop-channel-process")
                .name("newMember-time-day-shop-channel-process");

        DataStream<FactShopNewMember> factShopNewMemberAllChannelDs = shopMemberFilterDs
                .keyBy(new KeySelector<ShopMember, Tuple3<String, String, Integer>>() {
                    @Override
                    public Tuple3<String, String, Integer> getKey(ShopMember value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDefinedTime(value.getUpdatedAt(), format), "all", value.getShopId());
                    }
                }).process(new FactShopNewMemberFunction())
                .uid("newMember-time-day-shop-all-process")
                .name("newMember-time-day-shop-all-process");


        //写入mysql
        toJdbcUpsertSink(factShopLogSingleDs, table, FactShopLog.class);
        toJdbcUpsertSink(factShopLogSingleAllChannelDs, table, FactShopLog.class);

        toJdbcUpsertSink(factShopOrderDs, table, FactShopOrder.class);
        toJdbcUpsertSink(factShopOrderAllChannelDs, table, FactShopOrder.class);

        toJdbcUpsertSink(factShopPayDs, table, FactShopPay.class);
        toJdbcUpsertSink(factShopPayDsAllChannel, table, FactShopPay.class);

        toJdbcUpsertSink(factShopRefundDs, table, FactShopRefund.class);
        toJdbcUpsertSink(factShopRefundAllChannelDs, table, FactShopRefund.class);

        toJdbcUpsertSink(factShopProductSoldDs, table, FactShopProductSold.class);
        toJdbcUpsertSink(factShopProductSoldAllChannelDs, table, FactShopProductSold.class);

        toJdbcUpsertSink(factShopPromotionDs, table, FactShopPromotion.class);
        toJdbcUpsertSink(factShopPromotionAllChannelDs, table, FactShopPromotion.class);

        toJdbcUpsertSink(factShopNewMemberDs, table, FactShopNewMember.class);
        toJdbcUpsertSink(factShopNewMemberAllChannelDs, table, FactShopNewMember.class);
    }


    public static class FactShopLogStreamProcessFunction extends KeyedProcessFunction<Tuple3<String, String, Integer>, OdsLogStream, FactShopLog> {

        private transient ListState<String> factShopPvUvState;
        private transient ListState<String> factShopShareState;
        //店铺下商品加购
        private transient ListState<String> factShopProductAddCartState;
        private transient ListState<String> factShopProductViewState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ListStateDescriptor<String> factProductPvUvStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "factProductPvUvState",
                    // type information
                    Types.STRING);
            factProductPvUvStateDescriptor.enableTimeToLive(ttlConfig);
            factShopPvUvState = getRuntimeContext().getListState(factProductPvUvStateDescriptor);

            ListStateDescriptor<String> factShopShareStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "factProductShareState",
                    // type information
                    Types.STRING);
            factShopShareStateDescriptor.enableTimeToLive(ttlConfig);
            factShopShareState = getRuntimeContext().getListState(factShopShareStateDescriptor);

            ListStateDescriptor<String> factShopProductAddCartStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "factShopProductAddCartState",
                    // type information
                    Types.STRING);
            factShopProductAddCartStateDescriptor.enableTimeToLive(ttlConfig);
            factShopProductAddCartState = getRuntimeContext().getListState(factShopProductAddCartStateDescriptor);

            ListStateDescriptor<String> factShopProductViewStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "factShopProductView",
                    // type information
                    Types.STRING);
            factShopProductViewStateDescriptor.enableTimeToLive(ttlConfig);
            factShopProductViewState = getRuntimeContext().getListState(factShopProductViewStateDescriptor);
        }

        @Override
        public void processElement(OdsLogStream value, Context ctx, Collector<FactShopLog> out) throws Exception {

            long oneTime = value.getEventTime().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                String eventName = value.getEventName();
                String deviceId = value.getDeviceId();
                if ("ViewShop".equals(eventName)) {
                    factShopPvUvState.add(deviceId);
                }

                if ("ShareShop".equals(eventName)) {
                    factShopShareState.add(deviceId);
                }

                //浏览量
                if (("ViewGoods".equals(eventName) || "ViewGoodsByActivity".equals(eventName))) {
                    factShopProductViewState.add(deviceId);
                }

                //加入购物车
                if ("AddToShoppingCart".equals(eventName) || "AddToShoppingCartByActivity".equals(eventName)) {
                    factShopProductAddCartState.add(deviceId);
                }
            }

            TreeSet<String> uvSet = new TreeSet<>();
            TreeSet<String> shareSet = new TreeSet<>();
            TreeSet<String> productUvSet = new TreeSet<>();
            TreeSet<String> addCartCountSet = new TreeSet<>();


            Iterable<String> iterablePvAndUv = factShopPvUvState.get();
            Iterable<String> iterableShare = factShopShareState.get();
            Iterable<String> iterableAddCart = factShopProductAddCartState.get();
            Iterable<String> iterableProductView = factShopProductViewState.get();

            int pv = 0;
            int pvProduct = 0;
            int addCartCount = 0;
            int shareCount = 0;

            for (String s : iterablePvAndUv) {
                pv += 1;
                uvSet.add(s);
            }


            for (String s : iterableShare) {
                shareCount += 1;
                shareSet.add(s);
            }


            for (String s : iterableProductView) {
                pvProduct += 1;
                productUvSet.add(s);
            }


            for (String s : iterableAddCart) {
                addCartCount += 1;
                addCartCountSet.add(s);
            }


            Tuple3<String, String, Integer> currentKey = ctx.getCurrentKey();
            FactShopLog factShopLog = new FactShopLog();
            factShopLog.setRowTime(currentKey.f0);
            factShopLog.setChannel(currentKey.f1);
            factShopLog.setShopId(currentKey.f2);
            factShopLog.setPv(pv);
            factShopLog.setUv(uvSet.size());
            factShopLog.setShareCount(shareCount);
            factShopLog.setShareUserCount(shareSet.size());
            factShopLog.setAddCartCount(addCartCount);
            factShopLog.setAddCartUserCount(addCartCountSet.size());
            factShopLog.setPvProduct(pvProduct);
            factShopLog.setUvProduct(productUvSet.size());

            out.collect(factShopLog);
        }
    }


    public static class FactShopNewMemberFunction extends KeyedProcessFunction<Tuple3<String, String, Integer>, ShopMember, FactShopNewMember> {

        private transient MapState<Integer, Integer> memberState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            MapStateDescriptor<Integer, Integer> memberStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "memberState",
                    // type information
                    Types.INT,
                    Types.INT);
            memberStateDescriptor.enableTimeToLive(ttlConfig);
            memberState = getRuntimeContext().getMapState(memberStateDescriptor);
        }

        @Override
        public void processElement(ShopMember value, Context ctx, Collector<FactShopNewMember> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getCreatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //计算新注册用户昨天数据过来直接忽略不计
            long judge = oneTime + 86400000;

            if (judge > watermark) {
                if (!memberState.contains(value.getId())) {
                    memberState.put(value.getId(), 1);
                }
            }

            Tuple3<String, String, Integer> currentKey = ctx.getCurrentKey();
            FactShopNewMember factShopNewMember = new FactShopNewMember();
            factShopNewMember.setRowTime(currentKey.f0);
            factShopNewMember.setChannel(currentKey.f1);
            factShopNewMember.setShopId(currentKey.f2);

            AtomicInteger registerCount = new AtomicInteger();
            memberState.values().forEach(registerCount::addAndGet);

            factShopNewMember.setRegisterCount(registerCount.intValue());

            out.collect(factShopNewMember);
        }
    }


    public static class FactShopOrderProcessFunction extends KeyedProcessFunction<Tuple3<String, String, Integer>, OdsEmallOrder, FactShopOrder> {

        private transient ValueState<Integer> shopOrderState;
        private transient ListState<Integer> shopOrderMemberState;
        private transient ValueState<BigDecimal> shopOrderAmountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ValueStateDescriptor<Integer> shopOrderStateDescriptor = new ValueStateDescriptor<Integer>(
                    // the state name
                    "shopOrderState",
                    // type information
                    Types.INT);
            shopOrderStateDescriptor.enableTimeToLive(ttlConfig);
            shopOrderState = getRuntimeContext().getState(shopOrderStateDescriptor);

            ListStateDescriptor<Integer> shopOrderMemberStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "shopOrderMemberState",
                    // type information
                    Types.INT);
            shopOrderMemberStateDescriptor.enableTimeToLive(ttlConfig);
            shopOrderMemberState = getRuntimeContext().getListState(shopOrderMemberStateDescriptor);

            ValueStateDescriptor<BigDecimal> shopOrderAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shopOrderAmountState",
                    // type information
                    Types.BIG_DEC);
            shopOrderAmountStateDescriptor.enableTimeToLive(ttlConfig);
            shopOrderAmountState = getRuntimeContext().getState(shopOrderAmountStateDescriptor);
        }

        @Override
        public void processElement(OdsEmallOrder value, Context ctx, Collector<FactShopOrder> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            //取应付款作为下单金额
            if (judge > watermark) {
                Integer orderCount = shopOrderState.value();
                if (orderCount == null) {
                    shopOrderState.update(1);
                } else {
                    shopOrderState.update(orderCount + 1);
                }
                shopOrderMemberState.add(value.getMemberId());
                BigDecimal orderAmount = shopOrderAmountState.value();
                if (orderAmount == null) {
                    shopOrderAmountState.update(value.getActualAmount());
                } else {
                    shopOrderAmountState.update(orderAmount.add(value.getActualAmount()));
                }
            }

            Iterable<Integer> iterableOrderMember = shopOrderMemberState.get();

            TreeSet<Integer> orderMemberSet = new TreeSet<>();

            iterableOrderMember.forEach(e -> orderMemberSet.add(e));

            Tuple3<String, String, Integer> currentKey = ctx.getCurrentKey();
            FactShopOrder factShopOrder = new FactShopOrder();
            factShopOrder.setRowTime(currentKey.f0);
            factShopOrder.setChannel(currentKey.f1);
            factShopOrder.setShopId(currentKey.f2);
            factShopOrder.setOrderCount(shopOrderState.value());
            factShopOrder.setOrderUserCount(orderMemberSet.size());
            factShopOrder.setOrderAmount(shopOrderAmountState.value());

            out.collect(factShopOrder);
        }
    }


    public static class FactShopPayProcessFunction extends KeyedProcessFunction<Tuple3<String, String, Integer>, OdsEmallOrder, FactShopPay> {

        private transient ListState<Long> shopPaidState;
        private transient ListState<Integer> shopPaidMemberState;
        private transient ValueState<BigDecimal> shopPaidAmountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ListStateDescriptor<Long> shopPaidStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "shopPaidState",
                    // type information
                    Types.LONG);
            shopPaidStateDescriptor.enableTimeToLive(ttlConfig);
            shopPaidState = getRuntimeContext().getListState(shopPaidStateDescriptor);

            ListStateDescriptor<Integer> shopOrderMemberStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "shopPaidMemberState",
                    // type information
                    Types.INT);
            shopOrderMemberStateDescriptor.enableTimeToLive(ttlConfig);
            shopPaidMemberState = getRuntimeContext().getListState(shopOrderMemberStateDescriptor);


            ValueStateDescriptor<BigDecimal> shopPaidAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shopPaidAmountState",
                    // type information
                    Types.BIG_DEC);
            shopPaidAmountStateDescriptor.enableTimeToLive(ttlConfig);
            shopPaidAmountState = getRuntimeContext().getState(shopPaidAmountStateDescriptor);
        }

        @Override
        public void processElement(OdsEmallOrder value, Context ctx, Collector<FactShopPay> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 86400000;

            if (judge > watermark) {
                shopPaidState.add(value.getId());
                shopPaidMemberState.add(value.getMemberId());
                BigDecimal orderAmount = shopPaidAmountState.value();
                if (orderAmount == null) {
                    shopPaidAmountState.update(value.getActualAmount());
                } else {
                    shopPaidAmountState.update(orderAmount.add(value.getActualAmount()));
                }
            }

            Iterable<Long> iterableOrder = shopPaidState.get();
            Iterable<Integer> iterableOrderMember = shopPaidMemberState.get();

            TreeSet<Integer> paidMemberSet = new TreeSet<>();
            TreeSet<Long> orderSet = new TreeSet<>();
            iterableOrder.forEach(e -> orderSet.add(e));
            iterableOrderMember.forEach(e -> paidMemberSet.add(e));

            Tuple3<String, String, Integer> currentKey = ctx.getCurrentKey();
            FactShopPay factShopOrder = new FactShopPay();
            factShopOrder.setRowTime(currentKey.f0);
            factShopOrder.setChannel(currentKey.f1);
            factShopOrder.setShopId(currentKey.f2);
            factShopOrder.setPayCount(orderSet.size());
            factShopOrder.setPayUserCount(paidMemberSet.size());
            factShopOrder.setPayAmount(shopPaidAmountState.value());

            out.collect(factShopOrder);
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<FactShopPay> out) throws Exception {
            super.onTimer(timestamp, ctx, out);
        }
    }


    public static class FactShopPromotionProcessFunction extends KeyedProcessFunction<Tuple3<String, String, Integer>, FactOrderPromotion, FactShopPromotion> {

        //一个订单会参加多个活动
        //活动订单金额
        private transient MapState<Long, BigDecimal> shopPromotionAmountState;
        //活动优惠金额
        private transient MapState<Long, BigDecimal> shopPromotionDisCountAmountState;

        //产生过订单就是有效活动数
        private transient MapState<String, Integer> shopPromotionCountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            MapStateDescriptor<Long, BigDecimal> shopPromotionAmountStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "shopPromotionAmountState",
                    // type information
                    Types.LONG,
                    Types.BIG_DEC);
            shopPromotionAmountStateDescriptor.enableTimeToLive(ttlConfig);
            shopPromotionAmountState = getRuntimeContext().getMapState(shopPromotionAmountStateDescriptor);


            MapStateDescriptor<Long, BigDecimal> shopPromotionDisCountAmountStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "shopPromotionDisCountAmountState",
                    // type information
                    Types.LONG,
                    Types.BIG_DEC);
            shopPromotionDisCountAmountStateDescriptor.enableTimeToLive(ttlConfig);
            shopPromotionDisCountAmountState = getRuntimeContext().getMapState(shopPromotionDisCountAmountStateDescriptor);


            MapStateDescriptor<String, Integer> shopPromotionCountStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "shopPromotionCountState",
                    // type information
                    Types.STRING,
                    Types.INT);
            shopPromotionCountStateDescriptor.enableTimeToLive(ttlConfig);
            shopPromotionCountState = getRuntimeContext().getMapState(shopPromotionCountStateDescriptor);
        }

        @Override
        public void processElement(FactOrderPromotion value, Context ctx, Collector<FactShopPromotion> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                shopPromotionAmountState.put(value.getOrderId(), value.getActualAmount());
                shopPromotionDisCountAmountState.put(value.getOrderId(), value.getDiscountAmount());

                //有效活动数
                String promotionCountKey = "promotionType" + value.getPromotionType() + "promotionId" + value.getPromotionId();
                shopPromotionCountState.put(promotionCountKey, 1);
            }

            BigDecimal promotionOrderAmount = BigDecimal.ZERO;
            for (BigDecimal b : shopPromotionAmountState.values()) {
                promotionOrderAmount = promotionOrderAmount.add(b);
            }

            BigDecimal promotionDiscountAmount = BigDecimal.ZERO;
            for (BigDecimal b : shopPromotionDisCountAmountState.values()) {
                promotionDiscountAmount = promotionDiscountAmount.add(b);
            }

            int promotionCount = 0;
            for (Integer i : shopPromotionCountState.values()) {
                promotionCount = promotionCount + i;
            }

            Tuple3<String, String, Integer> currentKey = ctx.getCurrentKey();
            FactShopPromotion factShopPromotion = new FactShopPromotion();
            factShopPromotion.setRowTime(currentKey.f0);
            factShopPromotion.setChannel(currentKey.f1);
            factShopPromotion.setShopId(currentKey.f2);
            factShopPromotion.setPromotionOrderAmount(promotionOrderAmount);
            factShopPromotion.setPromotionDiscountAmount(promotionDiscountAmount);
            factShopPromotion.setPromotionCount(promotionCount);

            out.collect(factShopPromotion);
        }
    }


    public static class FactShopRefundProcessFunction extends KeyedProcessFunction<Tuple3<String, String, Integer>, OdsEmallOrder, FactShopRefund> {

        private transient ListState<Long> shopRefundState;
        private transient ListState<Integer> shopRefundMemberState;
        private transient ValueState<BigDecimal> shopRefundAmountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ListStateDescriptor<Long> shopRefundStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "shopRefundState",
                    // type information
                    Types.LONG);
            shopRefundStateDescriptor.enableTimeToLive(ttlConfig);
            shopRefundState = getRuntimeContext().getListState(shopRefundStateDescriptor);

            ListStateDescriptor<Integer> shopOrderMemberStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "shopRefundMemberState",
                    // type information
                    Types.INT);
            shopOrderMemberStateDescriptor.enableTimeToLive(ttlConfig);
            shopRefundMemberState = getRuntimeContext().getListState(shopOrderMemberStateDescriptor);

            ValueStateDescriptor<BigDecimal> shopRefundAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shopRefundAmountState",
                    // type information
                    Types.BIG_DEC);
            shopRefundAmountStateDescriptor.enableTimeToLive(ttlConfig);
            shopRefundAmountState = getRuntimeContext().getState(shopRefundAmountStateDescriptor);
        }

        @Override
        public void processElement(OdsEmallOrder value, Context ctx, Collector<FactShopRefund> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                shopRefundState.add(value.getId());
                BigDecimal orderAmount = shopRefundAmountState.value();
                if (orderAmount == null) {
                    shopRefundAmountState.update(value.getActualAmount());
                } else {
                    shopRefundAmountState.update(orderAmount.add(value.getActualAmount()));
                }
            }

            Iterable<Long> iterableOrder = shopRefundState.get();
            Iterable<Integer> iterableOrderMember = shopRefundMemberState.get();

            TreeSet<Integer> refundMemberSet = new TreeSet<>();
            int refundCount = 0;
            for (Long i : iterableOrder) {
                refundCount += 1;
            }
            iterableOrderMember.forEach(e -> refundMemberSet.add(e));

            Tuple3<String, String, Integer> currentKey = ctx.getCurrentKey();
            FactShopRefund factShopRefund = new FactShopRefund();
            factShopRefund.setRowTime(currentKey.f0);
            factShopRefund.setChannel(currentKey.f1);
            factShopRefund.setShopId(currentKey.f2);
            factShopRefund.setRefundCount(refundCount);
            factShopRefund.setRefundUserCount(refundMemberSet.size());
            factShopRefund.setRefundAmount(shopRefundAmountState.value());

            out.collect(factShopRefund);
        }

    }


    public static class FactShopSoldCountProcessFunction extends KeyedProcessFunction<Tuple3<String, String, Integer>, FactOrderItem, FactShopProductSold> {

        private transient ValueState<Integer> shopSoldCountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> shopPaidStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shopSoldCountState",
                    // type information
                    Types.INT);
            shopPaidStateDescriptor.enableTimeToLive(ttlConfig);
            shopSoldCountState = getRuntimeContext().getState(shopPaidStateDescriptor);
        }

        @Override
        public void processElement(FactOrderItem value, Context ctx, Collector<FactShopProductSold> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                Integer soldCount = shopSoldCountState.value();
                if (soldCount == null || soldCount == 0) {
                    shopSoldCountState.update(value.getQuantity());
                } else {
                    shopSoldCountState.update(soldCount + value.getQuantity());
                }
            }
            Tuple3<String, String, Integer> currentKey = ctx.getCurrentKey();

            FactShopProductSold factShopProductSold = new FactShopProductSold();
            factShopProductSold.setRowTime(currentKey.f0);
            factShopProductSold.setChannel(currentKey.f1);
            factShopProductSold.setShopId(currentKey.f2);
            factShopProductSold.setSaleCount(shopSoldCountState.value());

            out.collect(factShopProductSold);
        }
    }


    public static class ShopMemberFilterFunction extends RichFilterFunction<ShopMember> {
        private transient MapState<Integer, Boolean> memberState;

        @Override
        public void open(Configuration parameters) throws Exception {
            //会员状态不清除
            memberState = getRuntimeContext().getMapState(new MapStateDescriptor<Integer, Boolean>("memberState", Integer.class, Boolean.class));
        }

        @Override
        public boolean filter(ShopMember value) throws Exception {
            if (memberState.contains(value.getId())) {
                return false;
            } else {
                memberState.put(value.getId(), true);
                return true;
            }
        }
    }

}