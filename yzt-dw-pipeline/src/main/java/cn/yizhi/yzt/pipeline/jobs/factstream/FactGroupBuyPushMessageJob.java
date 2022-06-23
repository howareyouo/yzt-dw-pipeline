package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.common.EventType;
import cn.yizhi.yzt.pipeline.common.GroupBuyEventType;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactGroupBuyPushMessage;
import cn.yizhi.yzt.pipeline.model.ods.OdsGroupPromotionAppointment;
import cn.yizhi.yzt.pipeline.model.ods.OdsGroupPromotionInstanceMember;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.util.Collector;

/**
 * @author aorui created on 2020/12/15
 */
public class FactGroupBuyPushMessageJob extends StreamJob {


    @Override
    public void defineJob() throws Exception {
        //log流
        DataStream<OdsLogStream> odsLogStreamDs = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, OdsLogStream.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsLogStream>() {
                    @Override
                    public long extractAscendingTimestamp(OdsLogStream element) {
                        return element.getEventTime().getTime();
                    }
                });

        //浏览活动
        DataStream<FactGroupBuyPushMessage> viewGoodStream = odsLogStreamDs
                .filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        //过滤
                        String eventName = value.getEventName();
                        if (value.getPromotionType() != null
                                && value.getPromotionType() == 9
                                && value.getEventName() != null
                                && value.getUserName() != null
                                && value.getUserName() != ""
                                && value.getAvatar() != null
                                && value.getAvatar() != "") {
                            if (EventType.VIEW_ACTIVITY.getName().equals(eventName) || EventType.SHARE_ACTIVITY.getName().equals(eventName)) {
                                return true;
                            }
                        }
                        return false;
                    }
                }).process(new ProcessFunction<OdsLogStream, FactGroupBuyPushMessage>() {
                    @Override
                    public void processElement(OdsLogStream value, Context ctx, Collector<FactGroupBuyPushMessage> out) throws Exception {
                        FactGroupBuyPushMessage factGroupBuyPushMessage = new FactGroupBuyPushMessage();
                        factGroupBuyPushMessage.setShopId(value.getShopId());
                        factGroupBuyPushMessage.setPromotionId(value.getPromotionId());
                        factGroupBuyPushMessage.setGroupPromotionInstanceId(0);
                        factGroupBuyPushMessage.setUserName(value.getUserName());
                        factGroupBuyPushMessage.setUserPhone(value.getUserPhone());
                        if (EventType.VIEW_ACTIVITY.getName().equals(value.getEventName())) {
                            factGroupBuyPushMessage.setRecordType(GroupBuyEventType.VIEW.getCode());
                        } else {
                            factGroupBuyPushMessage.setRecordType(GroupBuyEventType.SHARE.getCode());
                        }
                        factGroupBuyPushMessage.setUserPhoto(value.getAvatar());
                        factGroupBuyPushMessage.setDesc(null);
                        factGroupBuyPushMessage.setRecordTime(value.getEventTime());
                        out.collect(factGroupBuyPushMessage);
                    }
                });


        //开团，入团记录
        DataStream<FactGroupBuyPushMessage> memberInstanceStream = this.createStreamFromKafka(SourceTopics.TOPIC_GROUP_BUY_PROMOTION_INSTANCE_MEMBER, OdsGroupPromotionInstanceMember.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsGroupPromotionInstanceMember>() {
                    @Override
                    public long extractAscendingTimestamp(OdsGroupPromotionInstanceMember element) {
                        return element.getCreatedAt().getTime();
                    }
                }).keyBy(new KeySelector<OdsGroupPromotionInstanceMember, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsGroupPromotionInstanceMember value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                }).filter(new RichFilterFunction<OdsGroupPromotionInstanceMember>() {
                    private transient MapState<Integer, Boolean> MemberInstanceState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);

                        MapStateDescriptor<Integer, Boolean> descriptor =
                                new MapStateDescriptor<>(
                                        "member-instance-map",
                                        Types.INT,
                                        Types.BOOLEAN);
                        StateTtlConfig ttlConfig = StateTtlConfig
                                // 保留90天, 取决于订单生命周期的时间跨度
                                .newBuilder(Time.days(90))
                                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                                .build();

                        descriptor.enableTimeToLive(ttlConfig);

                        MemberInstanceState = getRuntimeContext().getMapState(descriptor);
                    }

                    @Override
                    public boolean filter(OdsGroupPromotionInstanceMember value) throws Exception {
                        if (MemberInstanceState.contains(value.getId())) {
                            return false;
                        }
                        //过滤机器人
                        if (value.getMemberId() == 0) {
                            return false;
                        }
                        MemberInstanceState.put(value.getId(), true);
                        return true;
                    }
                }).process(new ProcessFunction<OdsGroupPromotionInstanceMember, FactGroupBuyPushMessage>() {
                    @Override
                    public void processElement(OdsGroupPromotionInstanceMember value, Context ctx, Collector<FactGroupBuyPushMessage> out) throws Exception {
                        FactGroupBuyPushMessage message = new FactGroupBuyPushMessage();
                        message.setShopId(value.getShopId());
                        message.setUserName(value.getMemberName());
                        message.setUserPhoto(value.getMemberPhoto());
                        message.setUserPhone(null);
                        message.setRecordTime(value.getCreatedAt());
                        if (value.getIsLeader() != null && value.getIsLeader() == 1) {
                            message.setRecordType(GroupBuyEventType.START_GROUP.getCode());
                        } else {
                            message.setRecordType(GroupBuyEventType.JOIN_GROUP.getCode());
                        }
                        message.setPromotionId(0);
                        message.setGroupPromotionInstanceId(value.getGroupPromotionInstanceId());
                        message.setDesc(null);
                        out.collect(message);
                    }
                });

        //预约记录
        DataStream<FactGroupBuyPushMessage> memberAppointmentStream = this.createStreamFromKafka(SourceTopics.TOPIC_GROUP_BUY_PROMOTION_APPOINTMENT, OdsGroupPromotionAppointment.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsGroupPromotionAppointment>() {
            @Override
            public long extractAscendingTimestamp(OdsGroupPromotionAppointment element) {
                return element.getCreatedAt().getTime();
            }
        }).keyBy(new KeySelector<OdsGroupPromotionAppointment, Tuple1<Integer>>() {
            @Override
            public Tuple1<Integer> getKey(OdsGroupPromotionAppointment value) throws Exception {
                return new Tuple1<>(value.getShopId());
            }
        }).filter(new RichFilterFunction<OdsGroupPromotionAppointment>() {
            private transient MapState<Integer, Boolean> MemberAppointmentState;

            @Override
            public void open(Configuration parameters) throws Exception {
                super.open(parameters);

                MapStateDescriptor<Integer, Boolean> descriptor =
                        new MapStateDescriptor<>(
                                "member-Appointment-map",
                                Types.INT,
                                Types.BOOLEAN);
                StateTtlConfig ttlConfig = StateTtlConfig
                        // 保留90天, 取决于订单生命周期的时间跨度
                        .newBuilder(Time.days(90))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                        .build();

                descriptor.enableTimeToLive(ttlConfig);

                MemberAppointmentState = getRuntimeContext().getMapState(descriptor);
            }

            @Override
            public boolean filter(OdsGroupPromotionAppointment value) throws Exception {
                if (MemberAppointmentState.contains(value.getId())) {
                    return false;
                }
                MemberAppointmentState.put(value.getId(), true);
                return true;
            }
        }).process(new ProcessFunction<OdsGroupPromotionAppointment, FactGroupBuyPushMessage>() {
                    @Override
                    public void processElement(OdsGroupPromotionAppointment value, Context ctx, Collector<FactGroupBuyPushMessage> out) throws Exception {
                        FactGroupBuyPushMessage message = new FactGroupBuyPushMessage();
                        message.setShopId(value.getShopId());
                        message.setUserName(value.getMemberName());
                        message.setUserPhoto(value.getMemberAvatar());
                        message.setUserPhone(value.getPhone());
                        message.setRecordTime(value.getCreatedAt());
                        message.setRecordType(GroupBuyEventType.APPOINT.getCode());
                        message.setPromotionId(value.getGroupPromotionId());
                        message.setGroupPromotionInstanceId(0);
                        message.setDesc(null);
                        out.collect(message);
                    }
                });

        //输出
        toKafkaSink(viewGoodStream, SourceTopics.TOPIC_GROUP_BUY_PROMOTION_PV_UV_MESSAGE);
        toKafkaSink(memberInstanceStream, SourceTopics.TOPIC_GROUP_BUY_PROMOTION_PV_UV_MESSAGE);
        toKafkaSink(memberAppointmentStream, SourceTopics.TOPIC_GROUP_BUY_PROMOTION_PV_UV_MESSAGE);
    }
}
