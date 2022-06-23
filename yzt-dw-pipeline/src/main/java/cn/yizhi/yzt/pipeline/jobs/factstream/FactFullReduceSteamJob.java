package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.function.FilterPaidOrderFunction;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.*;
import cn.yizhi.yzt.pipeline.model.fact.fullreduce.FactFullReduceProductPv;
import cn.yizhi.yzt.pipeline.model.fact.fullreduce.FactFullReducePvUv;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.fact.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author aorui created on 2020/11/4
 */
public class FactFullReduceSteamJob extends StreamJob {

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

        //商品部分结果
        DataStream<FactFullReduceProduct> factProductFullReduceDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_PRODUCT_FULL_REDUCE, FactProductFullReduce.class).keyBy(new KeySelector<FactProductFullReduce, Tuple3<String, Integer, Integer>>() {
                    @Override
                    public Tuple3<String, Integer, Integer> getKey(FactProductFullReduce value) throws Exception {
                        return new Tuple3<String, Integer, Integer>(value.getRowDate(), value.getShopId(), value.getFullReduceId());
                    }
                }).process(new FactProductFullReduceProcess())
                .uid("fact-product-full-reduce-data")
                .name("fact-product-full-reduce-data");

        //订单明细流
        DataStream<FactFullReduce> factOrderPromotionItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_PROMOTION_ITEM, FactOrderPromotionItem.class).filter(new FilterFunction<FactOrderPromotionItem>() {
                    @Override
                    public boolean filter(FactOrderPromotionItem value) throws Exception {
                        return value.getTransactionNo() != null && value.getPromotionType() != null && value.getPromotionType() == 2;
                    }
                }).keyBy(new KeySelector<FactOrderPromotionItem, Tuple3<String, Integer, Integer>>() {

                    @Override
                    public Tuple3<String, Integer, Integer> getKey(FactOrderPromotionItem value) throws Exception {
                        return new Tuple3<String, Integer, Integer>(convertTimeStamp(value.getUpdatedAt()), value.getShopId(), value.getPromotionId());
                    }
                }).process(new ResultProcess())
                .uid("fact-full-reduce-without-pvuv")
                .name("fact-full-reduce-without-pvuv");

        //pv uv
        DataStream<FactFullReducePvUv> factPromotionLogDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PROMOTION_LOG, FactPromotionLogByDay.class).filter(new FilterFunction<FactPromotionLogByDay>() {
                    @Override
                    public boolean filter(FactPromotionLogByDay value) throws Exception {
                        return value.getPromotionType() != null && value.getPromotionType() == 2;
                    }
                }).map(new MapFunction<FactPromotionLogByDay, FactFullReducePvUv>() {
                    @Override
                    public FactFullReducePvUv map(FactPromotionLogByDay value) throws Exception {
                        FactFullReducePvUv factFullReducePvUv = new FactFullReducePvUv();
                        factFullReducePvUv.setRowDate(value.getRowTime());
                        factFullReducePvUv.setShopId(value.getShopId());
                        factFullReducePvUv.setPromotionId(value.getPromotionId());
                        factFullReducePvUv.setPv(value.getPv());
                        factFullReducePvUv.setUv(value.getUv());
                        factFullReducePvUv.setShareCount(value.getShareCount());
                        return factFullReducePvUv;
                    }
                })
                .uid("fact-full-reduce-pvuv")
                .name("fact-full-reduce-pvuv");

        //productPv
        DataStream<FactFullReduceProductPv> factProductAllLogDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PRODUCT_LOG, FactProductLog.class)
                .filter(new FilterFunction<FactProductLog>() {
                    @Override
                    public boolean filter(FactProductLog value) throws Exception {
                        return value != null
                                && value.getPromotionType() != null
                                && value.getPromotionType() == 2
                                && value.getPromotionId() != null
                                && value.getPromotionId() != 0;
                    }
                }).keyBy(new KeySelector<FactProductLog, Tuple3<String, Integer, Integer>>() {
                    @Override
                    public Tuple3<String, Integer, Integer> getKey(FactProductLog value) throws Exception {
                        return new Tuple3<>(value.getRowTime(), value.getShopId(), value.getPromotionId());
                    }
                }).process(new FactProductPvProcess())
                .uid("fact-full-reduce-product-pv")
                .name("fact-full-reduce-product-pv");

        //输出
        toJdbcUpsertSink(factProductFullReduceDataStream, Tables.SINK_TABLE_FULL_REDUCE, FactFullReduceProduct.class);
        toJdbcUpsertSink(factOrderPromotionItemDs, Tables.SINK_TABLE_FULL_REDUCE, FactFullReduce.class);
        toJdbcUpsertSink(factPromotionLogDataStream, Tables.SINK_TABLE_FULL_REDUCE, FactFullReducePvUv.class);
        toJdbcUpsertSink(factProductAllLogDs, Tables.SINK_TABLE_FULL_REDUCE, FactFullReduceProductPv.class);

    }

    public static class FactProductFullReduceProcess extends KeyedProcessFunction<Tuple3<String, Integer, Integer>, FactProductFullReduce, FactFullReduceProduct> {

        //销售额
        private transient MapState<Integer, BigDecimal> saleTotalState;

        //销量
        private transient MapState<Integer, Integer> saleCountState;

        //优惠金额
        private transient MapState<Integer, BigDecimal> discountAmountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            MapStateDescriptor<Integer, BigDecimal> saleTotalStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "saleTotalState",
                    // type information
                    Types.INT,
                    Types.BIG_DEC);
            saleTotalStateDescriptor.enableTimeToLive(ttlConfig);
            saleTotalState = getRuntimeContext().getMapState(saleTotalStateDescriptor);

            MapStateDescriptor<Integer, Integer> saleCountStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "saleCountState",
                    // type information
                    Types.INT,
                    Types.INT);
            saleCountStateDescriptor.enableTimeToLive(ttlConfig);
            saleCountState = getRuntimeContext().getMapState(saleCountStateDescriptor);

            MapStateDescriptor<Integer, BigDecimal> discountAmountStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "discountAmountState",
                    // type information
                    Types.INT,
                    Types.BIG_DEC);
            discountAmountStateDescriptor.enableTimeToLive(ttlConfig);
            discountAmountState = getRuntimeContext().getMapState(discountAmountStateDescriptor);

        }

        @Override
        public void processElement(FactProductFullReduce value, Context ctx, Collector<FactFullReduceProduct> out) throws Exception {


            saleTotalState.put(value.getProductId(), value.getSaleTotal());
            saleCountState.put(value.getProductId(), value.getSaleCount());
            discountAmountState.put(value.getProductId(), value.getDiscountAmount());

            AtomicReference<BigDecimal> saleTotal = new AtomicReference<>(BigDecimal.ZERO);
            saleTotalState.values().forEach(e -> {
                saleTotal.updateAndGet(v -> v.add(e));
            });

            AtomicReference<Integer> saleCount = new AtomicReference<>(0);
            saleCountState.values().forEach(e -> {
                saleCount.updateAndGet(v -> v + e);
            });

            AtomicReference<BigDecimal> preferentialAmount = new AtomicReference<>(BigDecimal.ZERO);
            discountAmountState.values().forEach(e -> {
                preferentialAmount.updateAndGet(v -> v.add(e));
            });

            Tuple3<String, Integer, Integer> currentKey = ctx.getCurrentKey();
            FactFullReduceProduct factFullReduce = new FactFullReduceProduct();
            factFullReduce.setRowDate(currentKey.f0);
            factFullReduce.setShopId(currentKey.f1);
            factFullReduce.setPromotionId(currentKey.f2);
            factFullReduce.setProductAmount(saleTotal.get());
            factFullReduce.setSaleCount(saleCount.get());
            factFullReduce.setPreferentialAmount(preferentialAmount.get());

            out.collect(factFullReduce);
        }

    }

    public static class ResultProcess extends KeyedProcessFunction<Tuple3<String, Integer, Integer>, FactOrderPromotionItem, FactFullReduce> {

        //订单数
        private transient ListState<Long> orderIdState;

        //总销售额
        private transient ValueState<BigDecimal> actualAmountState;

        //订单map
        private transient MapState<Long, Boolean> orderMapState;

        //人数
        private transient ListState<Integer> memberIdState;

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

            ValueStateDescriptor<BigDecimal> actualAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "actualAmountState",
                    // type information
                    Types.BIG_DEC);
            actualAmountStateDescriptor.enableTimeToLive(ttlConfig);
            actualAmountState = getRuntimeContext().getState(actualAmountStateDescriptor);

            MapStateDescriptor<Long, Boolean> orderMapDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "order-map",
                    // type information
                    Types.LONG,
                    Types.BOOLEAN);
            orderMapDescriptor.enableTimeToLive(ttlConfig);
            orderMapState = getRuntimeContext().getMapState(orderMapDescriptor);

            ListStateDescriptor<Integer> memberIdStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "memberIdState",
                    // type information
                    Types.INT);
            memberIdStateDescriptor.enableTimeToLive(ttlConfig);
            memberIdState = getRuntimeContext().getListState(memberIdStateDescriptor);
        }

        @Override
        public void processElement(FactOrderPromotionItem value, Context ctx, Collector<FactFullReduce> out) throws Exception {
            if (!orderMapState.contains(value.getOrderId())) {
                BigDecimal actualAmount = actualAmountState.value();
                if (actualAmount == null) {
                    actualAmountState.update(value.getActualAmount());
                } else {
                    actualAmountState.update(actualAmount.add(value.getActualAmount()));
                }
                orderIdState.add(value.getOrderId());
                memberIdState.add(value.getMemberId());
                //去重统计
                Set<Long> orderId = new HashSet<>();
                orderIdState.get().forEach(e -> {
                    orderId.add(e);
                });
                Set<Integer> memberId = new HashSet<>();
                memberIdState.get().forEach(e -> {
                    memberId.add(e);
                });

                Tuple3<String, Integer, Integer> currentKey = ctx.getCurrentKey();
                FactFullReduce factFullReduce = new FactFullReduce();
                factFullReduce.setRowDate(currentKey.f0);
                factFullReduce.setShopId(currentKey.f1);
                factFullReduce.setPromotionId(currentKey.f2);
                factFullReduce.setOrderCount(orderId.size());
                factFullReduce.setPayTotal(actualAmountState.value());
                factFullReduce.setPayNumber(memberId.size());

                orderMapState.put(value.getOrderId(), true);
                out.collect(factFullReduce);
            }

        }
    }

    public static class FactProductPvProcess extends KeyedProcessFunction<Tuple3<String, Integer, Integer>, FactProductLog, FactFullReduceProductPv> {

        //productPv
        private transient MapState<Integer, Integer> productPvState;

        //主页加购次数
        private transient MapState<Integer, Integer> addcartCountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            MapStateDescriptor<Integer, Integer> productPvStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "productPvState",
                    // type information
                    Types.INT,
                    Types.INT);
            productPvStateDescriptor.enableTimeToLive(ttlConfig);
            productPvState = getRuntimeContext().getMapState(productPvStateDescriptor);

            MapStateDescriptor<Integer, Integer> addcartCountStateDescriptor = new MapStateDescriptor<>(
                    // the state name
                    "addcartCountState",
                    // type information
                    Types.INT,
                    Types.INT);
            addcartCountStateDescriptor.enableTimeToLive(ttlConfig);
            addcartCountState = getRuntimeContext().getMapState(addcartCountStateDescriptor);

        }


        @Override
        public void processElement(FactProductLog value, Context ctx, Collector<FactFullReduceProductPv> out) throws Exception {

            productPvState.put(value.getProductId(), value.getPv());
            addcartCountState.put(value.getProductId(), value.getAddShoppingCartCount());


            AtomicReference<Integer> productPv = new AtomicReference<>(0);
            productPvState.values().forEach(e -> {
                productPv.updateAndGet(v -> v + e);
            });

            AtomicReference<Integer> addcartCount = new AtomicReference<>(0);
            addcartCountState.values().forEach(e -> {
                addcartCount.updateAndGet(v -> v + e);
            });

            FactFullReduceProductPv factFullReduceProductPv = new FactFullReduceProductPv();
            Tuple3<String, Integer, Integer> currentKey = ctx.getCurrentKey();
            factFullReduceProductPv.setRowDate(currentKey.f0);
            factFullReduceProductPv.setShopId(currentKey.f1);
            factFullReduceProductPv.setPromotionId(currentKey.f2);
            factFullReduceProductPv.setProductPv(productPv.get());
            factFullReduceProductPv.setAddcartCount(addcartCount.get());

            out.collect(factFullReduceProductPv);
        }
    }

    private static String convertTimeStamp(Timestamp timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd").format(timestamp);
    }
}
