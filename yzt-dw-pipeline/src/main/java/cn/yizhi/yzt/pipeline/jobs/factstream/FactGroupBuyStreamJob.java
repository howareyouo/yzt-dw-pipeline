package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.function.FilterOrderPromotionItemFunction;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotion;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotionItem;
import cn.yizhi.yzt.pipeline.model.fact.FactPromotionLogAll;
import cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion.*;
import cn.yizhi.yzt.pipeline.model.ods.*;
import cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion.*;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsGroupPromotionInstance;
import cn.yizhi.yzt.pipeline.model.ods.OrderPromotionTable;
import cn.yizhi.yzt.pipeline.model.ods.ProductSkuExtend;
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
import org.apache.flink.table.api.Table;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: HuCheng
 * 拼团活动指标计算
 * @Date: 2020/12/14 14:14
 */
public class FactGroupBuyStreamJob extends StreamJob {
    private static final StateTtlConfig ttlConfig = StateTtlConfig
            //设置状态保留一年
            .newBuilder(org.apache.flink.api.common.time.Time.days(365))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {
        //注册sql文件
        this.registerSql("fact-group-buy.yml");

        //支付
        DataStream<FactOrderPromotionItem> factOrderPromotionItemDs =
                this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_PROMOTION_ITEM, FactOrderPromotionItem.class)
                .filter(new FilterFunction<FactOrderPromotionItem>() {
                    @Override
                    public boolean filter(FactOrderPromotionItem value) throws Exception {
                        return value.getStatus() != null && value.getPromotionType() != null;
                    }
                });

        //接收日志计算流
        DataStream<FactPromotionLogAll> factPromotionLogDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PROMOTION_LOG_ALL, FactPromotionLogAll.class);

        //库存
        DataStream<ProductSkuExtend> odsProductSkuExtendDs = this.createStreamFromKafka(SourceTopics.TOPIC_PRODUCT_SKU_EXTEND, ProductSkuExtend.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ProductSkuExtend>() {
                    @Override
                    public long extractAscendingTimestamp(ProductSkuExtend element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        //退款
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
                })
                .filter(new FilterFunction<OdsEmallOrder>() {
                    @Override
                    public boolean filter(OdsEmallOrder value) throws Exception {
                        return value.getStatus() == 9;
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

        //注册表
        this.createTableFromJdbc("ods_order_promotion", Tables.SOURCE_TABLE_ORDER_PROMOTION, OrderPromotionTable.class);

        //订单流转表
        streamToTable(OdsEmallOrder.class, emallOrderDs, true);

        //关联取出活动信息
        Table groupBugDimQuery = this.sqlQuery("groupBugDimQuery");
        DataStream<FactOrderPromotion> factOrderPromotionDataStream = tableEnv.toAppendStream(groupBugDimQuery, FactOrderPromotion.class);

        //日志
        DataStream<FactGroupBugLog> groupBugLogDs = factPromotionLogDs
                .map(new MapFunction<FactPromotionLogAll, FactGroupBugLog>() {
                    @Override
                    public FactGroupBugLog map(FactPromotionLogAll value) throws Exception {
                        if ("all".equals(value.getChannel()) && value.getPromotionType() == 9) {
                            FactGroupBugLog factGroupBugLog = new FactGroupBugLog();
                            factGroupBugLog.setPromotionId(value.getPromotionId());
                            factGroupBugLog.setPv(value.getPv());
                            factGroupBugLog.setUv(value.getUv());
                            factGroupBugLog.setShareCount(value.getShareCount());
                            factGroupBugLog.setShareUserCount(value.getShareUserCount());

                            return factGroupBugLog;
                        }
                        return null;
                    }
                })
                .filter(new FilterFunction<FactGroupBugLog>() {
                    @Override
                    public boolean filter(FactGroupBugLog value) throws Exception {
                        return value != null;
                    }
                });

        //支付
        DataStream<FactGroupBugOrder> factGroupBugOrderDs = factOrderPromotionItemDs
                .filter(new FilterFunction<FactOrderPromotionItem>() {
                    @Override
                    public boolean filter(FactOrderPromotionItem value) throws Exception {
                        return value.getStatus() == 1 && value.getPromotionType() == 9;
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
                .process(new GroupBugPaidOrderProcessFunction())
                .uid("group-bug-paid-order-process")
                .name("group-bug-paid-order-process");

        //退款
        DataStream<FactGroupBugRefund> factGroupBugRefundDs = factOrderPromotionDataStream
                .keyBy(new KeySelector<FactOrderPromotion, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(FactOrderPromotion value) throws Exception {
                        return new Tuple1<>(value.getPromotionId());
                    }
                })
                .process(new GroupBugRefundOrderProcessFunction())
                .uid("group-bug-refund-order-process")
                .name("group-bug-refund-order-process");

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

        //成团数
        DataStream<FactGroupBuyGrouped> factGroupBugGroupedDs = odsGroupPromotionInstanceDs
                .keyBy(new KeySelector<OdsGroupPromotionInstance, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsGroupPromotionInstance value) throws Exception {
                        return new Tuple1<Integer>(value.getGroupPromotionId());
                    }
                })
                .process(new GroupBugGroupedProcessFunction())
                .uid("group-buy-grouped-process")
                .name("group-buy-grouped-process");

        //写入mysql
        toJdbcUpsertSink(groupBugLogDs,Tables.SINK_TABLE_GROUP_BUY_V1,FactGroupBugLog.class);
        toJdbcUpsertSink(factGroupBugOrderDs,Tables.SINK_TABLE_ORDER_GROUP_BUY_V1,FactGroupBugOrder.class);
        toJdbcUpsertSink(factGroupBugRefundDs,Tables.SINK_TABLE_ORDER_GROUP_BUY_V1,FactGroupBugRefund.class);
        toJdbcUpsertSink(factGroupBugInventoryDs,Tables.SINK_TABLE_GROUP_BUY_V1, FactGroupBuyInventory.class);
        toJdbcUpsertSink(factGroupBugGroupedDs,Tables.SINK_TABLE_GROUP_BUY_V1, FactGroupBuyGrouped.class);
    }


    public static class GroupBugPaidOrderProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, FactOrderPromotionItem, FactGroupBugOrder> {
        //支付数据
        private transient ListState<Integer> paidOrderState;

        private transient ValueState<BigDecimal> paidAmountState;

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

        }


        @Override
        public void processElement(FactOrderPromotionItem value, Context ctx, Collector<FactGroupBugOrder> out) throws Exception {
            TreeSet<Integer> paidCountSet = new TreeSet<>();
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                paidOrderState.add(value.getMemberId());
                BigDecimal paidAmount = paidAmountState.value();
                if (paidAmount == null) {
                    paidAmountState.update(value.getActualAmount());
                } else {
                    paidAmountState.update(paidAmount.add(value.getActualAmount()));
                }
            }

            Iterable<Integer> paidMembers = paidOrderState.get();
            int paidCount = 0;
            for (Integer memberId : paidMembers) {
                paidCount += 1;
                paidCountSet.add(memberId);
            }

            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactGroupBugOrder factGroupBugOrder = new FactGroupBugOrder();

            factGroupBugOrder.setGroupPromotionId(currentKey.f0);
            factGroupBugOrder.setOrderCount(paidCount);
            factGroupBugOrder.setOrderUserCount(paidCountSet.size());
            factGroupBugOrder.setOrderAmount(paidAmountState.value());

            out.collect(factGroupBugOrder);
        }
    }

    //退款
    public static class GroupBugRefundOrderProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, FactOrderPromotion, FactGroupBugRefund> {

        private transient ListState<Integer> refundOrderState;

        private transient ValueState<BigDecimal> refundAmountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            //初始化状态
            ListStateDescriptor<Integer> refundOrderStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "refundOrderState",
                    // type information
                    Types.INT);
            refundOrderStateDescriptor.enableTimeToLive(ttlConfig);
            refundOrderState = getRuntimeContext().getListState(refundOrderStateDescriptor);

            ValueStateDescriptor<BigDecimal> refundAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "refundAmountState",
                    // type information
                    Types.BIG_DEC);
            refundAmountStateDescriptor.enableTimeToLive(ttlConfig);
            refundAmountState = getRuntimeContext().getState(refundAmountStateDescriptor);

        }


        @Override
        public void processElement(FactOrderPromotion value, Context ctx, Collector<FactGroupBugRefund> out) throws Exception {
            TreeSet<Integer> refundCountSet = new TreeSet<>();
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                refundOrderState.add(value.getMemberId());
                BigDecimal paidAmount = refundAmountState.value();
                if (paidAmount == null) {
                    refundAmountState.update(value.getActualAmount());
                } else {
                    refundAmountState.update(paidAmount.add(value.getActualAmount()));
                }
            }

            Iterable<Integer> refundMembers = refundOrderState.get();
            int paidCount = 0;
            for (Integer memberId : refundMembers) {
                paidCount += 1;
                refundCountSet.add(memberId);
            }

            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactGroupBugRefund factGroupBugRefund = new FactGroupBugRefund();

            factGroupBugRefund.setGroupPromotionId(currentKey.f0);
            factGroupBugRefund.setRefundCount(paidCount);
            factGroupBugRefund.setRefundUserCount(refundCountSet.size());
            factGroupBugRefund.setRefundAmount(refundAmountState.value());

            out.collect(factGroupBugRefund);
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


    //成团
    public static class GroupBugGroupedProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, OdsGroupPromotionInstance, FactGroupBuyGrouped> {
        //已成团
        private transient ValueState<Integer> groupedCountState;

        //未成团
        private transient ValueState<Integer> groupingCountState;

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

            ValueStateDescriptor<Integer> groupingCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "groupingCountState",
                    // type information
                    Types.INT);
            groupingCountStateDescriptor.enableTimeToLive(ttlConfig);
            groupingCountState = getRuntimeContext().getState(groupingCountStateDescriptor);
        }

        @Override
        public void processElement(OdsGroupPromotionInstance value, Context ctx, Collector<FactGroupBuyGrouped> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;


            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;

            if (judge > watermark) {
                Integer groupingCount = groupingCountState.value();
                Integer groupedCount = groupedCountState.value();
                //1 待成团 2已成团
                if (value.getStatus() == 1) {
                    if (groupingCount == null) {
                        groupingCountState.update(1);
                    } else {
                        groupingCountState.update(groupingCount + 1);
                    }
                } else if (value.getStatus() == 2) {
                    if (groupedCount == null) {
                        groupedCountState.update(1);
                    } else {
                        groupedCountState.update(groupedCount + 1);
                    }
                }
            }

            Tuple1<Integer> currentKey = ctx.getCurrentKey();

            FactGroupBuyGrouped factGroupBugGrouped = new FactGroupBuyGrouped();
            factGroupBugGrouped.setPromotionId(currentKey.f0);
            factGroupBugGrouped.setGroupingCount(groupingCountState.value());
            factGroupBugGrouped.setGroupedCount(groupedCountState.value());

            out.collect(factGroupBugGrouped);
        }
    }

}
