package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberLog;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

/**
 * @author hucheng
 * 计算会员日志相关指标
 * @date 2020/10/30 14:48
 */
public class FactMemberLogStreamJob extends StreamJob {
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


        DataStream<FactMemberLog> factMemberLogDs = odsLogStreamDs
                .filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        String eventName = value.getEventName();
                        if (value.getUserId() == null || value.getUserId() == 0) {
                            return false;
                        }

                        if ("ShareGoodsByActivity".equals(eventName) || "ShareActivity".equals(eventName) || "ShareGoods".equals(eventName) || "ShareShop".equals(eventName)) {
                            return true;
                        }

                        return false;
                    }
                })
                .map(new MapFunction<OdsLogStream, OdsLogStream>() {
                    @Override
                    public OdsLogStream map(OdsLogStream value) throws Exception {
                        if (value.getPromotionType() == null || value.getPromotionId() == null) {
                            value.setPromotionType(0);
                            value.setPromotionId(0);
                        }
                        return value;
                    }
                })
                .keyBy(new KeySelector<OdsLogStream, Tuple5<String, Integer, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple5<String, Integer, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple5<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), value.getShopId(), value.getPromotionType(), value.getPromotionId(), value.getUserId());
                    }
                })
                .process(new MemberLogStreamProcessFunction())
                .name("time-shop-promotion-process-member")
                .uid("time-shop-promotion-process-member");


        toKafkaSink(factMemberLogDs, SourceTopics.TOPIC_FACT_MEMBER_LOG);
    }

    public static class MemberLogStreamProcessFunction extends KeyedProcessFunction<Tuple5<String, Integer, Integer, Integer, Integer>, OdsLogStream, FactMemberLog> {

        private transient ValueState<Integer> shareActivityCountState;
        private transient ValueState<Integer> shareActivityProductCountState;
        private transient ValueState<Integer> shareShopCountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);

            ValueStateDescriptor<Integer> shareActivityCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shareActivityCountState",
                    // type information
                    Types.INT);
            shareActivityCountStateDescriptor.enableTimeToLive(ttlConfig);
            shareActivityCountState = getRuntimeContext().getState(shareActivityCountStateDescriptor);


            ValueStateDescriptor<Integer> shareActivityProductCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shareActivityProductCountState",
                    // type information
                    Types.INT);
            shareActivityProductCountStateDescriptor.enableTimeToLive(ttlConfig);
            shareActivityProductCountState = getRuntimeContext().getState(shareActivityProductCountStateDescriptor);


            ValueStateDescriptor<Integer> shareShopCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shareShopCountState",
                    // type information
                    Types.INT);
            shareShopCountStateDescriptor.enableTimeToLive(ttlConfig);
            shareShopCountState = getRuntimeContext().getState(shareShopCountStateDescriptor);
        }

        @Override
        public void processElement(OdsLogStream value, Context ctx, Collector<FactMemberLog> out) throws Exception {
            Integer shareActivityCount = shareActivityCountState.value();
            Integer shareActivityProductCount = shareActivityProductCountState.value();
            Integer shareShopCount = shareShopCountState.value();

            String eventName = value.getEventName();

            if ("ShareGoodsByActivity".equals(eventName) || "ShareGoods".equals(eventName)) {
                if (shareActivityProductCount == null || shareActivityProductCount == 0) {
                    shareActivityProductCountState.update(1);
                } else {
                    shareActivityProductCount += 1;
                    shareActivityProductCountState.update(shareActivityProductCount);
                }
            }

            if ("ShareActivity".equals(eventName)) {
                if (shareActivityCount == null || shareActivityCount == 0) {
                    shareActivityCountState.update(1);
                } else {
                    shareActivityCount += 1;
                    shareActivityCountState.update(shareActivityCount);
                }
            }

            if ("ShareShop".equals(eventName)) {
                if (shareShopCount == null || shareShopCount == 0) {
                    shareShopCountState.update(1);
                } else {
                    shareShopCount += 1;
                    shareShopCountState.update(shareShopCount);
                }
            }

            Tuple5<String, Integer, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();

            FactMemberLog factMemberLog = new FactMemberLog();
            factMemberLog.setRowTime(currentKey.f0);
            factMemberLog.setShopId(currentKey.f1);
            factMemberLog.setPromotionType(currentKey.f2);
            factMemberLog.setPromotionId(currentKey.f3);
            factMemberLog.setMemberId(currentKey.f4);
            factMemberLog.setShareActivityCount(shareActivityCountState.value() != null ? shareActivityCountState.value() : 0);
            factMemberLog.setShareActivityProductCount(shareActivityProductCountState.value() != null ? shareActivityProductCountState.value() : 0);
            factMemberLog.setShareShopCount(shareShopCountState.value() != null ? shareShopCountState.value() : 0);

            out.collect(factMemberLog);
        }
    }

}
