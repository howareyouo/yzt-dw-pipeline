package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.function.FilterOrderPromotionItemFunction;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotionItem;
import cn.yizhi.yzt.pipeline.model.fact.FactPromotionLogAll;
import cn.yizhi.yzt.pipeline.model.fact.flashsale.*;
import cn.yizhi.yzt.pipeline.model.fact.flashsale.product.FactFlashSaleProductInventory;
import cn.yizhi.yzt.pipeline.model.ods.OdsFlashSaleAppointment;
import cn.yizhi.yzt.pipeline.model.fact.flashsale.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
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
 * @date 2020/10/20 10:43
 */
public class FactFlashSaleStreamJob extends StreamJob {
    private static final String jobDescription = "秒杀活动指标";

    private static final StateTtlConfig ttlConfig = StateTtlConfig
            //保存一年
            .newBuilder(org.apache.flink.api.common.time.Time.days(365))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public String getJobDescription() {
        return jobDescription;
    }

    @Override
    public void defineJob() throws Exception {

        DataStream<FactOrderPromotionItem> factOrderPromotionItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_PROMOTION_ITEM, FactOrderPromotionItem.class);

        //接收不去重的数据
        DataStream<FactPromotionLogAll> factPromotionLogDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PROMOTION_LOG_ALL, FactPromotionLogAll.class);

        //库存
        DataStream<FactFlashSaleProductInventory> factFlashSaleProductInventoryDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_FLASH_SALE_PRODUCT_INVENTORY, FactFlashSaleProductInventory.class);

        //秒杀提醒记录
        DataStream<OdsFlashSaleAppointment> odsFlashSaleAppointmentDs = this.createStreamFromKafka(SourceTopics.TOPIC_FLASHSALE_APPOINTMENT, OdsFlashSaleAppointment.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsFlashSaleAppointment>() {
                    @Override
                    public long extractAscendingTimestamp(OdsFlashSaleAppointment element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        DataStream<FactOrderPromotionItem> factOrderPromotionItemFilterDs = factOrderPromotionItemDs
                .filter(new FilterFunction<FactOrderPromotionItem>() {
                    @Override
                    public boolean filter(FactOrderPromotionItem value) throws Exception {
                        return value.getPromotionId() != null && value.getPromotionType() != null && value.getPromotionType() == 3
                                && value.getOrderType() != null && value.getOrderType() == 3;
                    }
                });

        //秒杀商品埋点指标
        DataStream<FactFlashSaleLog> factFlashSaleLogDs = factPromotionLogDs.map(new MapFunction<FactPromotionLogAll, FactFlashSaleLog>() {

            @Override
            public FactFlashSaleLog map(FactPromotionLogAll value) throws Exception {
                if ("all".equals(value.getChannel()) && value.getPromotionType() == 3) {
                    FactFlashSaleLog factFlashSaleLog = new FactFlashSaleLog();
                    factFlashSaleLog.setActivityId(value.getPromotionId());
                    factFlashSaleLog.setPv(value.getPv());
                    factFlashSaleLog.setUv(value.getUv());
                    factFlashSaleLog.setShareCount(value.getShareCount());
                    factFlashSaleLog.setShareUserCount(value.getShareUserCount());
                    return factFlashSaleLog;
                }
                return null;
            }
        }).filter(new FilterFunction<FactFlashSaleLog>() {
            @Override
            public boolean filter(FactFlashSaleLog value) throws Exception {
                return value != null;
            }
        });

        //下单
        DataStream<FactFlashSaleOrder> factFlashSaleOrderDs = factOrderPromotionItemFilterDs
                .filter(new FilterFunction<FactOrderPromotionItem>() {
                    @Override
                    public boolean filter(FactOrderPromotionItem value) throws Exception {
                        return value.getActualAmount().compareTo(BigDecimal.ZERO) <= 0 || value.getStatus() == 0;
                    }
                })
                .keyBy(new KeySelector<FactOrderPromotionItem, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(FactOrderPromotionItem value) throws Exception {
                        return new Tuple1<>(value.getPromotionId());
                    }
                })
                .filter(new FilterOrderPromotionItemFunction())
                .uid("order-filter")
                .name("order-filter")
                .keyBy(new KeySelector<FactOrderPromotionItem, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(FactOrderPromotionItem value) throws Exception {
                        return new Tuple1<>(value.getPromotionId());
                    }
                })
                .process(new FlashSaleOrderProcessFunction())
                .uid("flash-sale-order-process")
                .name("flash-sale-order-process");

        //支付
        DataStream<FactFlashSalePaidOrder> factFlashSalePaidOrderDs = factOrderPromotionItemFilterDs
                .filter(new FilterFunction<FactOrderPromotionItem>() {
                    @Override
                    public boolean filter(FactOrderPromotionItem value) throws Exception {
                        return value.getStatus() == 1;
                    }
                })
                .keyBy(new KeySelector<FactOrderPromotionItem, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(FactOrderPromotionItem value) throws Exception {
                        return new Tuple1<>(value.getPromotionId());
                    }
                })
                .filter(new FilterOrderPromotionItemFunction())
                .uid("paid-order-filter")
                .name("paid-order-filter")
                .keyBy(new KeySelector<FactOrderPromotionItem, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(FactOrderPromotionItem value) throws Exception {
                        return new Tuple1<>(value.getPromotionId());
                    }
                })
                .process(new FlashSalePaidOrderProcessFunction())
                .uid("flash-sale-paid-order-process")
                .name("flash-sale-paid-order-process");


        //库存
        DataStream<FactFlashSaleInventory> factFlashSaleInventoryDs = factFlashSaleProductInventoryDs
                .keyBy(new KeySelector<FactFlashSaleProductInventory, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(FactFlashSaleProductInventory value) throws Exception {
                        return new Tuple1<>(value.getActivityId());
                    }
                })
                .process(new FlashSaleSaleCountProcessFunction())
                .uid("flash-sale-inventory-process")
                .name("flash-sale-inventory-process");


        //提醒人数
        DataStream<FactFlashSaleAppointment> factFlashSaleAppointmentDs = odsFlashSaleAppointmentDs
                .keyBy(new KeySelector<OdsFlashSaleAppointment, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsFlashSaleAppointment value) throws Exception {
                        return new Tuple1<Integer>(value.getActivityId());
                    }
                }).process(new FlashSaleAppointmentProcessFunction())
                .uid("flash-sale-appointment-process")
                .name("flash-sale-appointment-process");


        //写入mysql
        toJdbcUpsertSink(factFlashSaleLogDs, Tables.SINK_TABLE_FLASH_ACTIVITY, FactFlashSaleLog.class);
        toJdbcUpsertSink(factFlashSaleOrderDs, Tables.SINK_TABLE_FLASH_ACTIVITY, FactFlashSaleOrder.class);
        toJdbcUpsertSink(factFlashSalePaidOrderDs, Tables.SINK_TABLE_FLASH_ACTIVITY, FactFlashSalePaidOrder.class);
        toJdbcUpsertSink(factFlashSaleInventoryDs, Tables.SINK_TABLE_FLASH_ACTIVITY, FactFlashSaleInventory.class);
        toJdbcUpsertSink(factFlashSaleAppointmentDs, Tables.SINK_TABLE_FLASH_ACTIVITY, FactFlashSaleAppointment.class);
    }


    //计算下单人数和下单次数
    public static class FlashSaleOrderProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, FactOrderPromotionItem, FactFlashSaleOrder> {
        //下单数据
        private transient ListState<Integer> orderState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ListStateDescriptor<Integer> orderStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "orderState",
                    // type information
                    Types.INT);
            orderStateDescriptor.enableTimeToLive(ttlConfig);
            orderState = getRuntimeContext().getListState(orderStateDescriptor);
        }

        @Override
        public void processElement(FactOrderPromotionItem value, Context ctx, Collector<FactFlashSaleOrder> out) throws Exception {
            TreeSet<Integer> orderCountSet = new TreeSet<>();

            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                orderState.add(value.getMemberId());
            }

            //根据list state获取pv和uv
            Iterable<Integer> iterableOrderCount = orderState.get();

            int orderCount = 0;
            for (Integer memberId : iterableOrderCount) {
                orderCount += 1;
                orderCountSet.add(memberId);
            }

            //获取当前key
            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactFlashSaleOrder factFlashSaleOrder = new FactFlashSaleOrder();
            factFlashSaleOrder.setActivityId(currentKey.f0);
            factFlashSaleOrder.setOrderCount(orderCount);
            factFlashSaleOrder.setOrderNumber(orderCountSet.size());

            out.collect(factFlashSaleOrder);
        }

    }


    public static class FlashSalePaidOrderProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, FactOrderPromotionItem, FactFlashSalePaidOrder> {
        //下单数据
        private transient ListState<Integer> paidOrderState;

        private transient ValueState<BigDecimal> paidAmountState;

        private transient ValueState<BigDecimal> discountAmountState;

        //销量
        private transient ValueState<Integer> soldCountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            //初始化状态
            ListStateDescriptor<Integer> paidOrderStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "paidOrderState",
                    // type information
                    Types.INT);
            paidOrderStateDescriptor.enableTimeToLive(ttlConfig);
            paidOrderState = getRuntimeContext().getListState(paidOrderStateDescriptor);

            ValueStateDescriptor<BigDecimal> paidAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidAmountState",
                    // type information
                    Types.BIG_DEC);
            paidAmountStateDescriptor.enableTimeToLive(ttlConfig);
            paidAmountState = getRuntimeContext().getState(paidAmountStateDescriptor);


            ValueStateDescriptor<BigDecimal> discountAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "discountAmountState",
                    // type information
                    Types.BIG_DEC);
            discountAmountStateDescriptor.enableTimeToLive(ttlConfig);
            discountAmountState = getRuntimeContext().getState(discountAmountStateDescriptor);


            ValueStateDescriptor<Integer> soldCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "soldCountState",
                    // type information
                    Types.INT);
            soldCountStateDescriptor.enableTimeToLive(ttlConfig);
            soldCountState = getRuntimeContext().getState(soldCountStateDescriptor);
        }

        @Override
        public void processElement(FactOrderPromotionItem value, Context ctx, Collector<FactFlashSalePaidOrder> out) throws Exception {
            TreeSet<Integer> paidCountSet = new TreeSet<>();
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;


            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                paidOrderState.add(value.getMemberId());
                BigDecimal paidAmount = paidAmountState.value();
                BigDecimal discountAmount = discountAmountState.value();
                if (paidAmount == null) {
                    paidAmountState.update(value.getActualAmount());
                } else {
                    paidAmountState.update(paidAmount.add(value.getActualAmount()));
                }

                if (discountAmount == null) {
                    discountAmountState.update(value.getProductDiscountAmount());
                } else {
                    discountAmountState.update(discountAmount.add(value.getProductDiscountAmount()));
                }

                Integer soldCount = soldCountState.value();
                if (soldCount == null || soldCount == 0) {
                    soldCountState.update(value.getQuantity());
                } else {
                    soldCountState.update(soldCount + value.getQuantity());
                }
            }

            Iterable<Integer> paidMembers = paidOrderState.get();
            int paidCount = 0;
            for (Integer memberId : paidMembers) {
                paidCount += 1;
                paidCountSet.add(memberId);
            }

            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactFlashSalePaidOrder factFlashSalePaidOrder = new FactFlashSalePaidOrder();

            factFlashSalePaidOrder.setActivityId(currentKey.f0);
            factFlashSalePaidOrder.setPayCount(paidCount);
            factFlashSalePaidOrder.setPayNumber(paidCountSet.size());
            factFlashSalePaidOrder.setPayTotal(paidAmountState.value());
            factFlashSalePaidOrder.setPayCouponTotal(discountAmountState.value());
            factFlashSalePaidOrder.setSaleCount(soldCountState.value());

            out.collect(factFlashSalePaidOrder);
        }
    }


    //销量和库存（满减用的是独立库存）
    public static class FlashSaleSaleCountProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, FactFlashSaleProductInventory, FactFlashSaleInventory> {
        //定义商品库存state  key为product value为剩余库存
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
        public void processElement(FactFlashSaleProductInventory value, Context ctx, Collector<FactFlashSaleInventory> out) throws Exception {
            inventoryState.put(value.getProductId(), value.getInventory());

            AtomicInteger inventory = new AtomicInteger();
            inventoryState.values().forEach(inventory::addAndGet);


            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactFlashSaleInventory factFlashSaleInventory = new FactFlashSaleInventory();

            factFlashSaleInventory.setActivityId(currentKey.f0);
            factFlashSaleInventory.setInventory(inventory.get());

            out.collect(factFlashSaleInventory);
        }

    }


    //计算提醒人数
    public static class FlashSaleAppointmentProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, OdsFlashSaleAppointment, FactFlashSaleAppointment> {

        private transient MapState<Integer, Integer> appointmentState;

        private transient MapState<Integer, Integer> cancelAppointmentState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            MapStateDescriptor<Integer, Integer> appointmentStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "appointmentState",
                    // type information
                    Types.INT,
                    Types.INT);
            appointmentStateDescriptor.enableTimeToLive(ttlConfig);
            appointmentState = getRuntimeContext().getMapState(appointmentStateDescriptor);


            MapStateDescriptor<Integer, Integer> cancelAppointmentStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "cancelAppointmentState",
                    // type information
                    Types.INT,
                    Types.INT);
            cancelAppointmentStateDescriptor.enableTimeToLive(ttlConfig);
            cancelAppointmentState = getRuntimeContext().getMapState(cancelAppointmentStateDescriptor);
        }

        @Override
        public void processElement(OdsFlashSaleAppointment value, Context ctx, Collector<FactFlashSaleAppointment> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                if (!value.get__deleted()) {
                    if (!appointmentState.contains(value.getUserId())) {
                        appointmentState.put(value.getUserId(), 1);
                    }
                } else {
                    if (!cancelAppointmentState.contains(value.getUserId())) {
                        cancelAppointmentState.put(value.getUserId(), 1);
                    }
                }

            }


            AtomicInteger appointmentCount = new AtomicInteger();
            appointmentState.values().forEach(appointmentCount::addAndGet);

            AtomicInteger cancelAppointmentCount = new AtomicInteger();
            cancelAppointmentState.values().forEach(cancelAppointmentCount::addAndGet);

            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactFlashSaleAppointment factFlashSaleAppointment = new FactFlashSaleAppointment();
            factFlashSaleAppointment.setActivityId(currentKey.f0);
            factFlashSaleAppointment.setAppointmentCount(appointmentCount.get());
            factFlashSaleAppointment.setCancelAppointmentCount(cancelAppointmentCount.get());

            out.collect(factFlashSaleAppointment);
        }

    }
}