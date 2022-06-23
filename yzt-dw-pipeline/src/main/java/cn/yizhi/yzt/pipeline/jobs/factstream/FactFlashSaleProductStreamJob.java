package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotionItem;
import cn.yizhi.yzt.pipeline.model.fact.FactPromotionProductLog;
import cn.yizhi.yzt.pipeline.model.fact.flashsale.product.*;
import cn.yizhi.yzt.pipeline.model.ods.OdsFlashSaleActivityProduct;
import cn.yizhi.yzt.pipeline.model.ods.OdsFlashSaleAppointment;
import cn.yizhi.yzt.pipeline.model.ods.ProductSkuExtend;
import cn.yizhi.yzt.pipeline.model.fact.flashsale.product.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.table.api.Table;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hucheng
 * 计算秒杀下商品的指标
 * @date 2020/10/27 16:35
 */
public class FactFlashSaleProductStreamJob extends StreamJob {
    private static final StateTtlConfig ttlConfig = StateTtlConfig
            //设置状态保留3天
            .newBuilder(org.apache.flink.api.common.time.Time.days(365))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {
        //注册sql文件
        this.registerSql("fact-flash-sale.yml");

        DataStream<FactOrderPromotionItem> factOrderPromotionItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_PROMOTION_ITEM, FactOrderPromotionItem.class);

        DataStream<FactPromotionProductLog> factProductLogDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PROMOTION_PRODUCT_LOG_ALL, FactPromotionProductLog.class);

        DataStream<ProductSkuExtend> odsProductSkuExtendDs = this.createStreamFromKafka(SourceTopics.TOPIC_PRODUCT_SKU_EXTEND, ProductSkuExtend.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ProductSkuExtend>() {
                    @Override
                    public long extractAscendingTimestamp(ProductSkuExtend element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        //秒杀提醒记录
        DataStream<OdsFlashSaleAppointment> odsFlashSaleAppointmentDs = this.createStreamFromKafka(SourceTopics.TOPIC_FLASHSALE_APPOINTMENT, OdsFlashSaleAppointment.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsFlashSaleAppointment>() {
                    @Override
                    public long extractAscendingTimestamp(OdsFlashSaleAppointment element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        //注册表
        this.createTableFromJdbc("flashsale_activity_product", Tables.SOURCE_TABLE_FLASH_ACTIVITY_PRODUCT, OdsFlashSaleActivityProduct.class);

        //维表关联筛选出秒杀商品
        streamToTable(FactOrderPromotionItem.class, factOrderPromotionItemDs, true);
        Table flashSaleActivityProductDimQuery = this.sqlQuery("flashSaleActivityProductDimQuery");
        DataStream<FactOrderPromotionItem> factFlashSaleOrderDs = tableEnv.toAppendStream(flashSaleActivityProductDimQuery, FactOrderPromotionItem.class);

        //秒杀商品埋点指标
        DataStream<FactFlashSaleProductLog> factFlashSaleProductDs = factProductLogDs.map(new MapFunction<FactPromotionProductLog, FactFlashSaleProductLog>() {
            @Override
            public FactFlashSaleProductLog map(FactPromotionProductLog value) throws Exception {
                if ("all".equals(value.getChannel()) && value.getPromotionType() == 3) {
                    FactFlashSaleProductLog factFlashSaleProductLog = new FactFlashSaleProductLog();
                    factFlashSaleProductLog.setActivityId(value.getPromotionId());
                    factFlashSaleProductLog.setProductId(value.getProductId());
                    factFlashSaleProductLog.setPv(value.getPv());
                    factFlashSaleProductLog.setUv(value.getUv());
                    factFlashSaleProductLog.setShareCount(value.getShareCount());
                    factFlashSaleProductLog.setShareUserCount(value.getShareUserCount());

                    return factFlashSaleProductLog;
                }
                return null;
            }
        }).filter(new FilterFunction<FactFlashSaleProductLog>() {
            @Override
            public boolean filter(FactFlashSaleProductLog value) throws Exception {
                return value != null;
            }
        });

        //当日下单
        DataStream<FactFlashSaleProductOrder> factFlashSaleProductOrderDs = factFlashSaleOrderDs
                .filter(new FilterFunction<FactOrderPromotionItem>() {
                    @Override
                    public boolean filter(FactOrderPromotionItem value) throws Exception {
                        //0元订单状态直接为已支付
                        return value.getActualAmount().compareTo(BigDecimal.ZERO) <= 0 || value.getStatus() == 0;
                    }
                })
                .keyBy(new KeySelector<FactOrderPromotionItem, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactOrderPromotionItem value) throws Exception {
                        return new Tuple2<>(value.getPromotionId(), value.getProductId());
                    }
                })
                .process(new FlashSaleProductOrderProcessFunction())
                .name("flash-sale-product-order-process")
                .uid("flash-sale-product-order-process");

        //支付
        DataStream<FactFlashSaleProductPaidOrder> factFlashSaleProductPaidOrderDs = factFlashSaleOrderDs
                .filter(new FilterFunction<FactOrderPromotionItem>() {
                    @Override
                    public boolean filter(FactOrderPromotionItem value) throws Exception {
                        return value.getStatus() == 1;
                    }
                })
                .keyBy(new KeySelector<FactOrderPromotionItem, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(FactOrderPromotionItem value) throws Exception {
                        return new Tuple2<>(value.getPromotionId(), value.getProductId());
                    }
                })
                .process(new FlashSaleProductPaidOrderProcessFunction())
                .name("flash-sale-product-paid-order-process")
                .uid("flash-sale-product-paid-order-process");

        //库存
        DataStream<FactFlashSaleProductInventory> factFlashSaleProductInventoryDs = odsProductSkuExtendDs
                .filter(new FilterFunction<ProductSkuExtend>() {
                    @Override
                    public boolean filter(ProductSkuExtend value) throws Exception {
                        //3 表示秒杀
                        return value.getPromotionType() == 3;
                    }
                })
                .keyBy(new KeySelector<ProductSkuExtend, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(ProductSkuExtend value) throws Exception {
                        return new Tuple2<Integer, Integer>(value.getPromotionId(), value.getProductId());
                    }
                }).process(new FlashSaleSaleCountProcessFunction())
                .uid("flash-sale-product-inventory-soldCount-process")
                .name("flash-sale-product-inventory-soldCount-process");


        //提醒次数
        DataStream<FactFlashSaleProductAppointment> factFlashSaleProductAppointmentDs = odsFlashSaleAppointmentDs
                .keyBy(new KeySelector<OdsFlashSaleAppointment, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> getKey(OdsFlashSaleAppointment value) throws Exception {
                        return new Tuple2<Integer, Integer>(value.getActivityId(), value.getProductId());
                    }
                })
                .process(new FlashSaleAppointmentProcessFunction())
                .uid("flash-sale-product-appointment-process")
                .name("flash-sale-product-appointment-process");


        //写入kafka方便计算活动指标(销量和库存可以复用)
        toKafkaSink(factFlashSaleProductInventoryDs, SourceTopics.TOPIC_FACT_FLASH_SALE_PRODUCT_INVENTORY);

        //写入mysql
        toJdbcUpsertSink(factFlashSaleProductDs, Tables.SINK_TABLE_FLASH_ACTIVITY_PRODUCT, FactFlashSaleProductLog.class);
        toJdbcUpsertSink(factFlashSaleProductOrderDs, Tables.SINK_TABLE_FLASH_ACTIVITY_PRODUCT, FactFlashSaleProductOrder.class);
        toJdbcUpsertSink(factFlashSaleProductOrderDs, Tables.SINK_TABLE_FLASH_ACTIVITY_PRODUCT, FactFlashSaleProductOrder.class);
        toJdbcUpsertSink(factFlashSaleProductPaidOrderDs, Tables.SINK_TABLE_FLASH_ACTIVITY_PRODUCT, FactFlashSaleProductPaidOrder.class);
        toJdbcUpsertSink(factFlashSaleProductInventoryDs, Tables.SINK_TABLE_FLASH_ACTIVITY_PRODUCT, FactFlashSaleProductInventory.class);
        toJdbcUpsertSink(factFlashSaleProductAppointmentDs, Tables.SINK_TABLE_FLASH_ACTIVITY_PRODUCT, FactFlashSaleProductAppointment.class);
    }

    //计算下单人数和下单次数
    public static class FlashSaleProductOrderProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, FactOrderPromotionItem, FactFlashSaleProductOrder> {
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
        public void processElement(FactOrderPromotionItem value, Context ctx, Collector<FactFlashSaleProductOrder> out) throws Exception {
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
            Tuple2<Integer, Integer> currentKey = ctx.getCurrentKey();

            FactFlashSaleProductOrder factFlashSaleProductOrder = new FactFlashSaleProductOrder();
            factFlashSaleProductOrder.setActivityId(currentKey.f0);
            factFlashSaleProductOrder.setProductId(currentKey.f1);
            factFlashSaleProductOrder.setOrderCount(orderCount);
            factFlashSaleProductOrder.setOrderNumber(orderCountSet.size());

            out.collect(factFlashSaleProductOrder);

        }
    }


    public static class FlashSaleProductPaidOrderProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, FactOrderPromotionItem, FactFlashSaleProductPaidOrder> {
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
        public void processElement(FactOrderPromotionItem value, Context ctx, Collector<FactFlashSaleProductPaidOrder> out) throws Exception {
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

            Tuple2<Integer, Integer> currentKey = ctx.getCurrentKey();

            FactFlashSaleProductPaidOrder factFlashSaleProductPaidOrder = new FactFlashSaleProductPaidOrder();

            factFlashSaleProductPaidOrder.setActivityId(currentKey.f0);
            factFlashSaleProductPaidOrder.setProductId(currentKey.f1);
            factFlashSaleProductPaidOrder.setPayCount(paidCount);
            factFlashSaleProductPaidOrder.setPayNumber(paidCountSet.size());
            factFlashSaleProductPaidOrder.setPayTotal(paidAmountState.value());
            factFlashSaleProductPaidOrder.setPayCouponTotal(discountAmountState.value());
            factFlashSaleProductPaidOrder.setSaleCount(soldCountState.value());

            out.collect(factFlashSaleProductPaidOrder);
        }

    }


    //库存
    public static class FlashSaleSaleCountProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, ProductSkuExtend, FactFlashSaleProductInventory> {
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
        public void processElement(ProductSkuExtend value, Context ctx, Collector<FactFlashSaleProductInventory> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;


            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                inventoryState.put(value.getProductSkuId(), value.getInventory());
            }


            AtomicInteger inventory = new AtomicInteger();
            inventoryState.values().forEach(inventory::addAndGet);

            Tuple2<Integer, Integer> currentKey = ctx.getCurrentKey();

            FactFlashSaleProductInventory factFlashSaleProductInventory = new FactFlashSaleProductInventory();

            factFlashSaleProductInventory.setActivityId(currentKey.f0);
            factFlashSaleProductInventory.setProductId(currentKey.f1);
            factFlashSaleProductInventory.setInventory(inventory.get());

            out.collect(factFlashSaleProductInventory);
        }
    }


    //计算提醒人数
    public static class FlashSaleAppointmentProcessFunction extends KeyedProcessFunction<Tuple2<Integer, Integer>, OdsFlashSaleAppointment, FactFlashSaleProductAppointment> {

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
        public void processElement(OdsFlashSaleAppointment value, Context ctx, Collector<FactFlashSaleProductAppointment> out) throws Exception {
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

            Tuple2<Integer, Integer> currentKey = ctx.getCurrentKey();

            FactFlashSaleProductAppointment factFlashSaleAppointment = new FactFlashSaleProductAppointment();
            factFlashSaleAppointment.setActivityId(currentKey.f0);
            factFlashSaleAppointment.setProductId(currentKey.f1);
            factFlashSaleAppointment.setAppointmentCount(appointmentCount.get());
            factFlashSaleAppointment.setCancelAppointmentCount(cancelAppointmentCount.get());

            out.collect(factFlashSaleAppointment);
        }
    }
}
