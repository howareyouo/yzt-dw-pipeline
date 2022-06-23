package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactPromotionProductLog;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

import java.util.TreeSet;

/**
 * @Author: HuCheng
 * 活动商品访问日志累计
 * @Date: 2020/11/28 01:49
 */
public class FactPromotionProductLogStreamAll extends StreamJob {

    @Override
    public void defineJob() throws Exception {
        DataStream<OdsLogStream> odsLogStreamDs = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, OdsLogStream.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsLogStream>() {
                    @Override
                    public long extractAscendingTimestamp(OdsLogStream element) {
                        return element.getEventTime().getTime();
                    }
                });


        DataStream<OdsLogStream> filterOdsLogStream = odsLogStreamDs
                .filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        String eventName = value.getEventName();

                        if (value.getGoodsId() == null || value.getGoodsId() == 0
                                || value.getDeviceId() == null || value.getEndTime() != null) {
                            return false;
                        }

                        //只有活动产生日志的才算
                        if (eventName.equals("ViewGoodsByActivity") || eventName.equals("ShareGoodsByActivity") || eventName.equals("AddToShoppingCartByActivity")) {
                            return true;
                        }

                        return false;
                    }
                });

        //分渠道-商品   维度：时间-渠道-店铺-活动(活动类型-活动id)-商品 (必须包含活动id和活动类型)
        DataStream<FactPromotionProductLog> factPromotionProductLogDs = filterOdsLogStream
                .filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        return value.getPromotionType() != null && value.getPromotionId() != null;
                    }
                }).keyBy(new KeySelector<OdsLogStream, Tuple5<String, Integer, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple5<String, Integer, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple5<>(value.getTaroEnv(), value.getShopId(), value.getPromotionType(), value.getPromotionId(), value.getGoodsId());
                    }
                })
                .process(new PromotionProductLogStreamProcessFunction())
                .uid("time-shop-channel-promotion-product-process")
                .name("time-shop-channel-promotion-product-process");


        //全渠道 维度：时间-渠道-店铺-活动(活动类型-活动id)-商品 (必须包含活动id和活动类型)
        DataStream<FactPromotionProductLog> factPromotionProductAllChannelLogDs = filterOdsLogStream
                .filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        return value.getPromotionType() != null && value.getPromotionId() != null;
                    }
                })
                .keyBy(new KeySelector<OdsLogStream, Tuple5<String, Integer, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple5<String, Integer, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple5<>("all", value.getShopId(), value.getPromotionType(), value.getPromotionId(), value.getGoodsId());
                    }
                })
                .process(new PromotionProductLogStreamProcessFunction())
                .uid("time-shop-all-promotion-product-process")
                .name("time-shop-all-promotion-product-process");

        //输出kafka
        toKafkaSink(factPromotionProductLogDs, SourceTopics.TOPIC_FACT_PROMOTION_PRODUCT_LOG_ALL);
        toKafkaSink(factPromotionProductAllChannelLogDs, SourceTopics.TOPIC_FACT_PROMOTION_PRODUCT_LOG_ALL);
    }


    public static class PromotionProductLogStreamProcessFunction extends KeyedProcessFunction<Tuple5<String, Integer, Integer, Integer, Integer>, OdsLogStream, FactPromotionProductLog> {
        //分享
        private transient ListState<String> shareCountState;
        //pv
        private transient ListState<String> pvAndUvState;
        //加购
        private transient ListState<String> addShoppingCartState;


        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ListStateDescriptor<String> paidOrderStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "shareCountState",
                    // type information
                    Types.STRING);
            shareCountState = getRuntimeContext().getListState(paidOrderStateDescriptor);

            ListStateDescriptor<String> pvAndUvStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "pvAndUvState",
                    // type information
                    Types.STRING);
            pvAndUvState = getRuntimeContext().getListState(pvAndUvStateDescriptor);


            ListStateDescriptor<String> addShoppingCartStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "addShoppingCartState",
                    // type information
                    Types.STRING);
            addShoppingCartState = getRuntimeContext().getListState(addShoppingCartStateDescriptor);
        }

        @Override
        public void processElement(OdsLogStream value, Context ctx, Collector<FactPromotionProductLog> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getEventTime().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;


            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;
            String eventName = value.getEventName();
            String deviceId = value.getDeviceId();

            if (judge > watermark) {

                //浏览商品，还需判断结束浏览时间为空
                if ("ViewGoodsByActivity".equals(eventName)) {
                    pvAndUvState.add(deviceId);
                }

                //分享商品
                if ("ShareGoodsByActivity".equals(eventName)) {
                    shareCountState.add(deviceId);
                }

                //加入购物车
                if ("AddToShoppingCartByActivity".equals(eventName)) {
                    addShoppingCartState.add(deviceId);
                }
            }

            Tuple5<String, Integer, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            FactPromotionProductLog factPromotionProductLog = new FactPromotionProductLog();
            factPromotionProductLog.setChannel(currentKey.f0);
            factPromotionProductLog.setShopId(currentKey.f1);
            factPromotionProductLog.setPromotionType(currentKey.f2);
            factPromotionProductLog.setPromotionId(currentKey.f3);
            factPromotionProductLog.setProductId(currentKey.f4);

            Iterable<String> iterablePvAndUv = pvAndUvState.get();
            Iterable<String> iterableShare = shareCountState.get();
            Iterable<String> iterableAddCart = addShoppingCartState.get();

            TreeSet<String> uvSet = new TreeSet<>();
            TreeSet<String> shareCountSet = new TreeSet<>();
            TreeSet<String> addCartCountSet = new TreeSet<>();

            int pv = 0;
            int shareCount = 0;
            int addShopCount = 0;

            for (String s : iterablePvAndUv) {
                pv += 1;
                uvSet.add(s);
            }

            for (String s : iterableShare) {
                shareCount += 1;
                shareCountSet.add(s);
            }

            for (String s : iterableAddCart) {
                addShopCount += 1;
                addCartCountSet.add(s);
            }


            factPromotionProductLog.setPv(pv);
            factPromotionProductLog.setUv(uvSet.size());
            factPromotionProductLog.setShareCount(shareCount);
            factPromotionProductLog.setShareUserCount(shareCountSet.size());
            factPromotionProductLog.setAddShoppingCartCount(addShopCount);
            factPromotionProductLog.setAddShoppingCartUserCount(addCartCountSet.size());

            out.collect(factPromotionProductLog);
        }
    }
}