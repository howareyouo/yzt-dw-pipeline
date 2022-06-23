package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotionItem;
import cn.yizhi.yzt.pipeline.model.fact.product.FactProduct;
import cn.yizhi.yzt.pipeline.model.fact.FactProductLog;
import cn.yizhi.yzt.pipeline.model.fact.factjoin.FactProductSkuJoinStream;
import cn.yizhi.yzt.pipeline.model.fact.product.FactProductPvUv;
import cn.yizhi.yzt.pipeline.model.ods.ProductSku;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
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
 * @author aorui created on 2020/10/29
 */
public class FactProductNewStreamJob extends StreamJob {

    private static StateTtlConfig ttlConfig = StateTtlConfig
            //设置状态保留3天
            .newBuilder(org.apache.flink.api.common.time.Time.days(3))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {

        //订单明细流
        DataStream<FactOrderPromotionItem> factOrderPromotionItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_PROMOTION_ITEM, FactOrderPromotionItem.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<FactOrderPromotionItem>() {
                    @Override
                    public long extractAscendingTimestamp(FactOrderPromotionItem element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        //productSku流
        DataStream<ProductSku> productSkuDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_PRODUCT_SKU, ProductSku.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ProductSku>() {
                    @Override
                    public long extractAscendingTimestamp(ProductSku element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });


        //分渠道商品流
        DataStream<FactProductPvUv> factProductLogDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PRODUCT_LOG, FactProductLog.class)
                .map(new MapFunction<FactProductLog, FactProductLog>() {

                    @Override
                    public FactProductLog map(FactProductLog value) throws Exception {
                        if("wxweb" == value.getChannel()){
                            value.setChannel("web");
                        }
                        return value;
                    }
                })
                .filter(new FilterFunction<FactProductLog>() {
                    @Override
                    public boolean filter(FactProductLog value) throws Exception {
                        return value.getPromotionId() == 0 && value.getPromotionType() == 0 && !"all".equals(value.getChannel());
                    }
                }).map(new MapFunction<FactProductLog, FactProductPvUv>() {
                    @Override
                    public FactProductPvUv map(FactProductLog value) throws Exception {
                        FactProductPvUv factProduct = new FactProductPvUv();
                        factProduct.setRowDate(value.getRowTime());
                        factProduct.setProductId(value.getProductId());
                        factProduct.setShopId(value.getShopId());
                        factProduct.setChannel(value.getChannel());
                        factProduct.setPv(value.getPv());
                        factProduct.setUv(value.getUv());
                        factProduct.setAddcartCount(value.getAddShoppingCartCount());
                        factProduct.setAddcartNumber(value.getAddShoppingCartUserCount());
                        factProduct.setShareCount(value.getShareCount());
                        factProduct.setShareNumber(value.getShareUserCount());
                        return factProduct;
                    }
                });

        //全渠道商品流
        DataStream<FactProductPvUv> factProductAllLogDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_PRODUCT_LOG, FactProductLog.class)
                .filter(new FilterFunction<FactProductLog>() {
                    @Override
                    public boolean filter(FactProductLog value) throws Exception {
                        return value.getPromotionId() == 0 && value.getPromotionType() == 0 && "all".equals(value.getChannel());
                    }
                }).map(new MapFunction<FactProductLog, FactProductPvUv>() {
                    @Override
                    public FactProductPvUv map(FactProductLog value) throws Exception {
                        FactProductPvUv factProduct = new FactProductPvUv();
                        factProduct.setRowDate(value.getRowTime());
                        factProduct.setProductId(value.getProductId());
                        factProduct.setShopId(value.getShopId());
                        factProduct.setChannel(value.getChannel());
                        factProduct.setPv(value.getPv());
                        factProduct.setUv(value.getUv());
                        factProduct.setAddcartCount(value.getAddShoppingCartCount());
                        factProduct.setAddcartNumber(value.getAddShoppingCartUserCount());
                        factProduct.setShareCount(value.getShareCount());
                        factProduct.setShareNumber(value.getShareUserCount());
                        return factProduct;
                    }
                });

        //分渠道的商品与销量的流
        DataStream<FactProduct> FactProductSkuJoinStream = factOrderPromotionItemDs
                .keyBy(new KeySelector<FactOrderPromotionItem, Integer>() {
                    @Override
                    public Integer getKey(FactOrderPromotionItem value) throws Exception {
                        return value.getProductId();
                    }
                })
                //关联productSkuDataStream
                .intervalJoin(productSkuDataStream.keyBy(new KeySelector<ProductSku, Integer>() {
                    @Override
                    public Integer getKey(ProductSku value) throws Exception {
                        return value.getProductId();
                    }
                })).between(Time.minutes(-5), Time.minutes(10))
                .process(new ProcessJoinFunction<FactOrderPromotionItem, ProductSku, cn.yizhi.yzt.pipeline.model.fact.factjoin.FactProductSkuJoinStream>() {
                    @Override
                    public void processElement(FactOrderPromotionItem left, ProductSku right, Context ctx, Collector<FactProductSkuJoinStream> out) throws Exception {
                        out.collect(new FactProductSkuJoinStream(left, right));
                    }
                })
                .keyBy(new KeySelector<FactProductSkuJoinStream, Tuple4<String, Integer, Integer, String>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, String> getKey(FactProductSkuJoinStream value) throws Exception {
                        return new Tuple4(convertTimeStamp(value.getFactOrderPromotionItem().getUpdatedAt()), value.getFactOrderPromotionItem().getShopId(), value.getFactOrderPromotionItem().getProductId(), value.getFactOrderPromotionItem().getSource());
                    }
                }).process(new FactProductSkuResult())
                .uid("product-sku-stream")
                .name("product-sku-stream");

        //全渠道的商品与销量流
        DataStream<FactProduct> FactProductSkuAllJoinStream = factOrderPromotionItemDs
                .map(new MapFunction<FactOrderPromotionItem, FactOrderPromotionItem>() {
                    @Override
                    public FactOrderPromotionItem map(FactOrderPromotionItem value) throws Exception {
                        value.setSource("all");
                        return value;
                    }
                })
                .keyBy(new KeySelector<FactOrderPromotionItem, Integer>() {
                    @Override
                    public Integer getKey(FactOrderPromotionItem value) throws Exception {
                        return value.getProductId();
                    }
                })
                //关联productSkuDataStream
                .intervalJoin(productSkuDataStream.keyBy(new KeySelector<ProductSku, Integer>() {
                    @Override
                    public Integer getKey(ProductSku value) throws Exception {
                        return value.getProductId();
                    }
                })).between(Time.minutes(-5), Time.minutes(10))
                .process(new ProcessJoinFunction<FactOrderPromotionItem, ProductSku, FactProductSkuJoinStream>() {
                    @Override
                    public void processElement(FactOrderPromotionItem left, ProductSku right, Context ctx, Collector<FactProductSkuJoinStream> out) throws Exception {
                        out.collect(new FactProductSkuJoinStream(left, right));
                    }
                })
                .keyBy(new KeySelector<FactProductSkuJoinStream, Tuple4<String, Integer, Integer, String>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, String> getKey(FactProductSkuJoinStream value) throws Exception {
                        return new Tuple4(convertTimeStamp(value.getFactOrderPromotionItem().getUpdatedAt()), value.getFactOrderPromotionItem().getShopId(), value.getFactOrderPromotionItem().getProductId(), value.getFactOrderPromotionItem().getSource());
                    }
                }).process(new FactProductSkuResult())
                .uid("product-sku-all-stream")
                .name("product-sku-all-stream");


        //输出到数据库
        toJdbcUpsertSink(FactProductSkuJoinStream, Tables.SINK_TABLE_PRODUCT, FactProduct.class);
        toJdbcUpsertSink(FactProductSkuAllJoinStream, Tables.SINK_TABLE_PRODUCT, FactProduct.class);
        //公共指标输出到库
        toJdbcUpsertSink(factProductLogDataStream, Tables.SINK_TABLE_PRODUCT, FactProductPvUv.class);
        toJdbcUpsertSink(factProductAllLogDs, Tables.SINK_TABLE_PRODUCT, FactProductPvUv.class);
    }


    public static class FactProductSkuResult extends KeyedProcessFunction<Tuple4<String, Integer, Integer, String>, FactProductSkuJoinStream, FactProduct> {

        //销售额
        private transient ValueState<BigDecimal> saleTotalState;

        //销量
        private transient ValueState<Integer> saleCountState;

        //总单数
        private transient ListState<Long> orderCountState;

        //下单人数
        private transient ListState<Integer> orderNumberState;

        //付款单数
        private transient ListState<Long> payCountState;

        //付款人数
        private transient ListState<Integer> payNumberState;

        //剩余库存
        private transient ValueState<Integer> inventoryState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ValueStateDescriptor<BigDecimal> saleTotalStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "saleTotalState",
                    // type information
                    Types.BIG_DEC);
            saleTotalStateDescriptor.enableTimeToLive(ttlConfig);
            saleTotalState = getRuntimeContext().getState(saleTotalStateDescriptor);

            ValueStateDescriptor<Integer> saleCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "saleCountState",
                    // type information
                    Types.INT);
            saleCountStateDescriptor.enableTimeToLive(ttlConfig);
            saleCountState = getRuntimeContext().getState(saleCountStateDescriptor);

            ListStateDescriptor<Long> orderCountStateDescriptor = new ListStateDescriptor<Long>(
                    // the state name
                    "orderCountState",
                    // type information
                    Types.LONG);
            orderCountStateDescriptor.enableTimeToLive(ttlConfig);
            orderCountState = getRuntimeContext().getListState(orderCountStateDescriptor);

            ListStateDescriptor<Integer> orderNumberStateDescriptor = new ListStateDescriptor<Integer>(
                    // the state name
                    "orderNumberState",
                    // type information
                    Types.INT);
            orderNumberStateDescriptor.enableTimeToLive(ttlConfig);
            orderNumberState = getRuntimeContext().getListState(orderNumberStateDescriptor);

            ListStateDescriptor<Long> payCountStateDescriptor = new ListStateDescriptor<Long>(
                    // the state name
                    "payCountState",
                    // type information
                    Types.LONG);
            payCountStateDescriptor.enableTimeToLive(ttlConfig);
            payCountState = getRuntimeContext().getListState(payCountStateDescriptor);

            ListStateDescriptor<Integer> payNumberStateDescriptor = new ListStateDescriptor<Integer>(
                    // the state name
                    "payNumberState",
                    // type information
                    Types.INT);
            payNumberStateDescriptor.enableTimeToLive(ttlConfig);
            payNumberState = getRuntimeContext().getListState(payNumberStateDescriptor);

            ValueStateDescriptor<Integer> inventoryStateDescriptor = new ValueStateDescriptor<Integer>(
                    // the state name
                    "inventoryState",
                    // type information
                    Types.INT);
            inventoryStateDescriptor.enableTimeToLive(ttlConfig);
            inventoryState = getRuntimeContext().getState(inventoryStateDescriptor);
        }

        @Override
        public void processElement(FactProductSkuJoinStream value, Context ctx, Collector<FactProduct> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getFactOrderPromotionItem().getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算
            long judge = oneTime + 172800000;
            //未超时进行计算
            if (judge > watermark) {
                BigDecimal saleTotal = saleTotalState.value();
                Integer saleCount = saleCountState.value();
                Integer inventory = inventoryState.value();

                //单号不为空
                if (value.getFactOrderPromotionItem().getTransactionNo() != null) {
                    if (saleTotal == null) {
                        saleTotalState.update(value.getFactOrderPromotionItem().getActualAmount());
                    } else {
                        saleTotalState.update(saleTotal.add(value.getFactOrderPromotionItem().getActualAmount()));
                    }

                    if (saleCount == null) {
                        saleCountState.update(value.getFactOrderPromotionItem().getQuantity());
                    } else {
                        saleCountState.update(saleCount + value.getFactOrderPromotionItem().getQuantity());
                    }

                    orderCountState.add(value.getFactOrderPromotionItem().getOrderId());
                    orderNumberState.add(value.getFactOrderPromotionItem().getMemberId());
                }

                if (inventory == null) {
                    inventoryState.update(value.getProductSku().getInventory());
                } else {
                    inventoryState.update(inventory + value.getProductSku().getInventory());
                }

                payCountState.add(value.getFactOrderPromotionItem().getOrderId());
                payNumberState.add(value.getFactOrderPromotionItem().getMemberId());
            }

            //去重统计
            Set<Long> orderCount = new HashSet<>();
            Set<Integer> orderNumber = new HashSet<>();
            Set<Long> payCount = new HashSet<>();
            Set<Integer> payNumber = new HashSet<>();
            orderCountState.get().forEach(e -> {
                orderCount.add(e);
            });
            orderNumberState.get().forEach(e -> {
                orderNumber.add(e);
            });
            payCountState.get().forEach(e -> {
                payCount.add(e);
            });
            payNumberState.get().forEach(e -> {
                payNumber.add(e);
            });

            //数据封装
            FactProduct factProduct = new FactProduct();
            Tuple4<String, Integer, Integer, String> currentKey = ctx.getCurrentKey();
            factProduct.setRowDate(currentKey.f0);
            factProduct.setShopId(currentKey.f1);
            factProduct.setProductId(currentKey.f2);
            factProduct.setChannel(currentKey.f3);
            factProduct.setOrderCount(orderCount.size());
            factProduct.setSaleTotal(saleTotalState.value());
            factProduct.setSaleCount(saleCountState.value());
            factProduct.setOrderNumber(orderNumber.size());
            factProduct.setPayCount(payCount.size());
            factProduct.setPayNumber(payNumber.size());
            factProduct.setInventory(inventoryState.value());
            out.collect(factProduct);

        }
    }

    private static String convertTimeStamp(Timestamp timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd").format(timestamp);
    }

}
