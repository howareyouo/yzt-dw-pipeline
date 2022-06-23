package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactPromotionLogByDay;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
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
 * @author hucheng
 * 计算活动日志相关指标（每天）
 * @date 2020/10/30 14:07
 */
public class FactPromotionLogStreamJobByDay extends StreamJob {

    private static StateTtlConfig ttlConfig = StateTtlConfig
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
        DataStream<FactPromotionLogByDay> factPromotionLogDs = promotionFilterLogStream
                .keyBy(new KeySelector<OdsLogStream, Tuple5<String, String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple5<String, String, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple5<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), value.getTaroEnv(), value.getShopId(), value.getPromotionType(), value.getPromotionId());
                    }
                }).process(new PromotionLogStreamProcessFunction())
                .uid("time-shop-channel-promotion-process")
                .name("time-shop-channel-promotion-process");


        //全渠道
        DataStream<FactPromotionLogByDay> factPromotionAllLogDs = promotionFilterLogStream
                .keyBy(new KeySelector<OdsLogStream, Tuple5<String, String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple5<String, String, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple5<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), "all", value.getShopId(), value.getPromotionType(), value.getPromotionId());
                    }
                }).process(new PromotionLogStreamProcessFunction())
                .uid("time-shop-all-promotion-process")
                .name("time-shop-all-promotion-process");

        //写入kafka
        toKafkaSink(factPromotionLogDs, SourceTopics.TOPIC_FACT_PROMOTION_LOG);
        toKafkaSink(factPromotionAllLogDs, SourceTopics.TOPIC_FACT_PROMOTION_LOG);
    }


    public static class PromotionLogStreamProcessFunction extends KeyedProcessFunction<Tuple5<String, String, Integer, Integer, Integer>, OdsLogStream, FactPromotionLogByDay> {
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
            shareCountStateDescriptor.enableTimeToLive(ttlConfig);
            shareCountState = getRuntimeContext().getListState(shareCountStateDescriptor);


            ListStateDescriptor<String> pvAndUvStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "pvAndUvState",
                    // type information
                    Types.STRING);
            pvAndUvStateDescriptor.enableTimeToLive(ttlConfig);
            pvAndUvState = getRuntimeContext().getListState(pvAndUvStateDescriptor);
        }

        @Override
        public void processElement(OdsLogStream value, Context ctx, Collector<FactPromotionLogByDay> out) throws Exception {
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

            Tuple5<String, String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            FactPromotionLogByDay factPromotionLog = new FactPromotionLogByDay();
            factPromotionLog.setRowTime(currentKey.f0);
            factPromotionLog.setChannel(currentKey.f1);
            factPromotionLog.setShopId(currentKey.f2);
            factPromotionLog.setPromotionType(currentKey.f3);
            factPromotionLog.setPromotionId(currentKey.f4);

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

            factPromotionLog.setPv(pv);
            factPromotionLog.setUv(uvSet.size());
            factPromotionLog.setShareCount(shareCount);
            factPromotionLog.setShareUserCount(shareCountSet.size());

            out.collect(factPromotionLog);
        }
    }
}
