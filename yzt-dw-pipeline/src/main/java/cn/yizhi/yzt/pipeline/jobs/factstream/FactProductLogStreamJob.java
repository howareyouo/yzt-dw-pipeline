package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactProductLog;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

import java.util.TreeSet;

/**
 * @author hucheng
 * 计算商品日志相关指标
 * 指标维度包含 时间（天）、店铺、活动（0表示不是通过活动浏览）、渠道
 * @date 2020/10/27 17:00
 */
public class FactProductLogStreamJob extends StreamJob {

    private  static  StateTtlConfig ttlConfig = StateTtlConfig
            //设置状态保留3天
            .newBuilder(org.apache.flink.api.common.time.Time.days(3))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

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

                        if (eventName.equals("ViewGoods") || eventName.equals("ViewGoodsByActivity")
                                || eventName.equals("ShareGoods") || eventName.equals("ShareGoodsByActivity")
                                || eventName.equals("AddToShoppingCart") || eventName.equals("AddToShoppingCartByActivity")) {
                            return true;
                        }

                        return false;
                    }
                });

        //分渠道-商品   维度：时间-渠道-店铺-活动(活动类型-活动id)-商品 (必须包含活动id和活动类型)
        DataStream<FactProductLog> factPromotionProductLogDs = filterOdsLogStream
                .filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        return value.getPromotionType() != null && value.getPromotionId() != null;
                    }
                }).keyBy(new KeySelector<OdsLogStream, Tuple6<String, String, Integer, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple6<String, String, Integer, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple6<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), value.getTaroEnv(), value.getShopId(), value.getPromotionType(), value.getPromotionId(), value.getGoodsId());
                    }
                })
                .process(new ProductLogStreamProcessFunction())
                .uid("time-shop-channel-promotion-product-process")
                .name("time-shop-channel-promotion-product-process");

        //分渠道-商品   维度：时间-渠道-店铺-商品
        DataStream<FactProductLog> factProductLogDs = filterOdsLogStream
                .keyBy(new KeySelector<OdsLogStream, Tuple6<String, String, Integer, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple6<String, String, Integer, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple6<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), value.getTaroEnv(), value.getShopId(), 0, 0, value.getGoodsId());
                    }
                }).process(new ProductLogStreamProcessFunction())
                .uid("time-shop-channel-product-process")
                .name("time-shop-channel-product-process");


        //全渠道 维度：时间-渠道-店铺-活动(活动类型-活动id)-商品 (必须包含活动id和活动类型)
        DataStream<FactProductLog> factPromotionProductAllChannelLogDs = filterOdsLogStream
                .filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        return value.getPromotionType() != null && value.getPromotionId() != null;
                    }
                })
                .keyBy(new KeySelector<OdsLogStream, Tuple6<String, String, Integer, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple6<String, String, Integer, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple6<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), "all", value.getShopId(), value.getPromotionType(), value.getPromotionId(), value.getGoodsId());
                    }
                })
                .process(new ProductLogStreamProcessFunction())
                .uid("time-shop-all-promotion-product-process")
                .name("time-shop-all-promotion-product-process");


        //全渠道 维度：时间-渠道-店铺-商品
        DataStream<FactProductLog> factProductAllLogDs = filterOdsLogStream
                .keyBy(new KeySelector<OdsLogStream, Tuple6<String, String, Integer, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple6<String, String, Integer, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple6<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), "all", value.getShopId(), 0, 0, value.getGoodsId());
                    }
                }).process(new ProductLogStreamProcessFunction())
                .uid("time-shop-all-product-process")
                .name("time-shop-all-product-process");

        //输出kafka
        toKafkaSink(factPromotionProductLogDs, SourceTopics.TOPIC_FACT_PRODUCT_LOG);
        toKafkaSink(factProductLogDs, SourceTopics.TOPIC_FACT_PRODUCT_LOG);
        toKafkaSink(factPromotionProductAllChannelLogDs, SourceTopics.TOPIC_FACT_PRODUCT_LOG);
        toKafkaSink(factProductAllLogDs, SourceTopics.TOPIC_FACT_PRODUCT_LOG);

    }


    public static class ProductLogStreamProcessFunction extends KeyedProcessFunction<Tuple6<String, String, Integer, Integer, Integer, Integer>, OdsLogStream, FactProductLog> {
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
            paidOrderStateDescriptor.enableTimeToLive(ttlConfig);
            shareCountState = getRuntimeContext().getListState(paidOrderStateDescriptor);

            ListStateDescriptor<String> pvAndUvStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "pvAndUvState",
                    // type information
                    Types.STRING);

            pvAndUvStateDescriptor.enableTimeToLive(ttlConfig);
            pvAndUvState = getRuntimeContext().getListState(pvAndUvStateDescriptor);


            ListStateDescriptor<String> addShoppingCartStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "addShoppingCartState",
                    // type information
                    Types.STRING);
            addShoppingCartStateDescriptor.enableTimeToLive(ttlConfig);
            addShoppingCartState = getRuntimeContext().getListState(addShoppingCartStateDescriptor);
        }

        @Override
        public void processElement(OdsLogStream value, Context ctx, Collector<FactProductLog> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getEventTime().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;


            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 172800000;
            String eventName = value.getEventName();
            String deviceId = value.getDeviceId();

            if (judge > watermark) {

                //浏览商品，还需判断结束浏览时间为空
                if (("ViewGoods".equals(eventName) || "ViewGoodsByActivity".equals(eventName))) {
                    pvAndUvState.add(deviceId);
                }

                //分享商品
                if ("ShareGoods".equals(eventName) || "ShareGoodsByActivity".equals(eventName)) {
                    shareCountState.add(deviceId);
                }

                //加入购物车
                if ("AddToShoppingCart".equals(eventName) || "AddToShoppingCartByActivity".equals(eventName)) {
                    addShoppingCartState.add(deviceId);
                }
            }

            Tuple6<String, String, Integer, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            FactProductLog factProductLog = new FactProductLog();
            factProductLog.setRowTime(currentKey.f0);
            factProductLog.setChannel(currentKey.f1);
            factProductLog.setShopId(currentKey.f2);
            factProductLog.setPromotionType(currentKey.f3);
            factProductLog.setPromotionId(currentKey.f4);
            factProductLog.setProductId(currentKey.f5);

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


            factProductLog.setPv(pv);
            factProductLog.setUv(uvSet.size());
            factProductLog.setShareCount(shareCount);
            factProductLog.setShareUserCount(shareCountSet.size());
            factProductLog.setAddShoppingCartCount(addShopCount);
            factProductLog.setAddShoppingCartUserCount(addCartCountSet.size());

            out.collect(factProductLog);
        }
    }
}
