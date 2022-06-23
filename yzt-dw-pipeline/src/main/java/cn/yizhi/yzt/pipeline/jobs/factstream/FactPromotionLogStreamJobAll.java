package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactPromotionLogAll;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

import java.util.TreeSet;

/**
 * @Author: HuCheng
 * 活动累计埋点数据
 * @Date: 2020/11/28 01:34
 */
public class FactPromotionLogStreamJobAll extends StreamJob {

    @Override
    public void defineJob() throws Exception {
        DataStream<OdsLogStream> odsLogStreamDs = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, OdsLogStream.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsLogStream>() {
                    @Override
                    public long extractAscendingTimestamp(OdsLogStream element) {
                        return element.getEventTime().getTime();
                    }
                });


        DataStream<OdsLogStream> promotionFilterLogStream = odsLogStreamDs.filter(new FilterFunction<OdsLogStream>() {
            @Override
            public boolean filter(OdsLogStream value) throws Exception {
                String eventName = value.getEventName();
                if (value.getPromotionId() == null && value.getPromotionType() == null) {
                    return false;
                }

                if (eventName.equals("ViewActivity") || eventName.equals("ShareActivity") || eventName.equals("EnterLiveRoom")) {
                    return true;
                }

                //限时购 商品日志数据 也算 活动
                if (value.getPromotionType() == 3 && ("ViewGoodsByActivity".equals(eventName) || "ShareGoodsByActivity".equals(eventName))) {
                    return true;
                }

                return false;
            }
        }).map(new MapFunction<OdsLogStream, OdsLogStream>() {
            @Override
            public OdsLogStream map(OdsLogStream value) throws Exception {
                String eventName = value.getEventName();
                //直播活动的渠道只有在微信小程序访问，渠道改为source（点击，分享）
                if (eventName.equals("EnterLiveRoom")) {
                    value.setPromotionId(value.getLiveRoomId());
                    value.setPromotionType(4);
                    value.setTaroEnv(value.getSource());
                }
                return value;
            }
        });


        //分渠道
        DataStream<FactPromotionLogAll> factPromotionLogDs = promotionFilterLogStream
                .keyBy(new KeySelector<OdsLogStream, Tuple4<String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple4<>(value.getTaroEnv(), value.getShopId(), value.getPromotionType(), value.getPromotionId());
                    }
                }).process(new PromotionLogStreamAllProcessFunction())
                .uid("time-shop-channel-promotion-process")
                .name("time-shop-channel-promotion-process");


        //全渠道
        DataStream<FactPromotionLogAll> factPromotionAllLogDs = promotionFilterLogStream
                .keyBy(new KeySelector<OdsLogStream, Tuple4<String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple4<>("all", value.getShopId(), value.getPromotionType(), value.getPromotionId());
                    }
                }).process(new PromotionLogStreamAllProcessFunction())
                .uid("time-shop-all-promotion-process")
                .name("time-shop-all-promotion-process");

        //写入kafka
        toKafkaSink(factPromotionLogDs, SourceTopics.TOPIC_FACT_PROMOTION_LOG_ALL);
        toKafkaSink(factPromotionAllLogDs, SourceTopics.TOPIC_FACT_PROMOTION_LOG_ALL);

        //写入mysql
        toJdbcUpsertSink(factPromotionLogDs, Tables.SINK_TABLE_PROMOTION_LOG, FactPromotionLogAll.class);
        toJdbcUpsertSink(factPromotionAllLogDs, Tables.SINK_TABLE_PROMOTION_LOG, FactPromotionLogAll.class);
    }


    public static class PromotionLogStreamAllProcessFunction extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, OdsLogStream, FactPromotionLogAll> {
        //分享
        private transient ListState<String> shareCountState;
        //pv
        private transient ListState<String> pvAndUvState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ListStateDescriptor<String> shareCountStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "shareCountState",
                    // type information
                    Types.STRING);
            shareCountState = getRuntimeContext().getListState(shareCountStateDescriptor);


            ListStateDescriptor<String> pvAndUvStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "pvAndUvState",
                    // type information
                    Types.STRING);
            pvAndUvState = getRuntimeContext().getListState(pvAndUvStateDescriptor);
        }

        @Override
        public void processElement(OdsLogStream value, Context ctx, Collector<FactPromotionLogAll> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getEventTime().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;
            long judge = oneTime + 172800000;
            String eventName = value.getEventName();
            String deviceId = value.getDeviceId();

            if (judge > watermark) {
                //浏览商品，还需判断结束浏览时间为空
                if ("ViewActivity".equals(eventName) || "EnterLiveRoom".equals(eventName) || "ViewGoodsByActivity".equals(eventName)) {
                    pvAndUvState.add(deviceId);
                }

                //分享商品
                if ("ShareActivity".equals(eventName) || "ShareGoodsByActivity".equals(eventName)) {
                    shareCountState.add(deviceId);
                }
            }

            Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            FactPromotionLogAll factPromotionLogAll = new FactPromotionLogAll();
            factPromotionLogAll.setChannel(currentKey.f0);
            factPromotionLogAll.setShopId(currentKey.f1);
            factPromotionLogAll.setPromotionType(currentKey.f2);
            factPromotionLogAll.setPromotionId(currentKey.f3);

            Iterable<String> iterablePvAndUv = pvAndUvState.get();
            Iterable<String> iterableShare = shareCountState.get();

            TreeSet<String> uvSet = new TreeSet<>();
            TreeSet<String> shareCountSet = new TreeSet<>();

            int pv = 0;
            int shareCount = 0;

            for (String s : iterablePvAndUv) {
                pv += 1;
                uvSet.add(s);
            }

            for (String s : iterableShare) {
                shareCount += 1;
                shareCountSet.add(s);
            }

            factPromotionLogAll.setPv(pv);
            factPromotionLogAll.setUv(uvSet.size());
            factPromotionLogAll.setShareCount(shareCount);
            factPromotionLogAll.setShareUserCount(shareCountSet.size());

            out.collect(factPromotionLogAll);
        }
    }
}
