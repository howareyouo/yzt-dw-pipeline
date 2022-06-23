package cn.yizhi.yzt.pipeline.jobs.factstream.member;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.common.EventType;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.RawLogStream;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberShopVisits;
import cn.yizhi.yzt.pipeline.model.fact.member.FactSubscribedQuery;
import cn.yizhi.yzt.pipeline.model.ods.*;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import cn.yizhi.yzt.pipeline.model.ods.*;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.table.api.Table;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * 会员优惠券指标
 * winner
 * 2021-0105
 */
public class FactMemberInfoStreamJob extends StreamJob {
    //400天清除
    private static final StateTtlConfig ttlConfig = StateTtlConfig
        .newBuilder(org.apache.flink.api.common.time.Time.days(400))
        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
        .build();

    private static final StateTtlConfig dayConfig = StateTtlConfig
        .newBuilder(org.apache.flink.api.common.time.Time.days(1))
        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
        .build();


    public final static int TEST_SHOP = 261;

    @Override
    public void defineJob() throws Exception {

        tagAndGroupAndUnion();

        wxRelation();

        shopVisit();

    }


    private void tagAndGroupAndUnion() {
        //群组定义数据
        SingleOutputStreamOperator<OdsMemberGroup> memberGroupSingleOutputStreamOperator = this.createStreamFromKafka(SourceTopics.TOPIC_MEMBER_GROUP, OdsMemberGroup.class)
            .keyBy(new KeySelector<OdsMemberGroup, Tuple1<Integer>>() {
                @Override
                public Tuple1<Integer> getKey(OdsMemberGroup value) throws Exception {
                    return new Tuple1<>(value.getShopId());
                }
            }).process(new FactMemberGroupProcessFunction())
            .uid("fact-member-group-log")
            .name("fact-member-group-log");

        //智能标签定义数据
        SingleOutputStreamOperator<OdsTagDefinition> tagDefinitionSingleOutputStreamOperator = this.createStreamFromKafka(SourceTopics.TOPIC_TAG_DEFINITION, OdsTagDefinition.class)
            .filter(new FilterFunction<OdsTagDefinition>() {
                @Override
                public boolean filter(OdsTagDefinition value) throws Exception {
                    //过滤下，智能标签的才能过。
                    if (value.getType() == OdsTagDefinition.SMART_TAG) {
                        return true;
                    }
                    return false;
                }
            })
            .keyBy(new KeySelector<OdsTagDefinition, Tuple1<Integer>>() {
                @Override
                public Tuple1<Integer> getKey(OdsTagDefinition value) throws Exception {
                    return new Tuple1<>(value.getShopId());
                }
            }).process(new FactMemberTagDefinitionProcessFunction())
            .uid("fact-member-tag-definition-log")
            .name("fact-member-tag-definition-log");


        //群组定义数据
        SingleOutputStreamOperator<OdsMemberTag> memberTagSingleOutputStreamOperator = this.createStreamFromKafka(SourceTopics.TOPIC_MEMBER_TAG, OdsMemberTag.class)

            .keyBy(new KeySelector<OdsMemberTag, Tuple1<Integer>>() {
                @Override
                public Tuple1<Integer> getKey(OdsMemberTag value) throws Exception {
                    return new Tuple1<>(value.getShopId());
                }
            }).process(new FactMemberTagProcessFunction())
            .uid("fact-member-tag-log")
            .name("fact-member-tag-log");

        //OdsMemberUnion
        SingleOutputStreamOperator<OdsMemberUnion> odsMemberUnionSingleOutputStreamOperator = this.createStreamFromKafka(SourceTopics.TOPIC_MEMBER_UNION, OdsMemberUnion.class)
            .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsMemberUnion>() {

                @Override
                public long extractAscendingTimestamp(OdsMemberUnion element) {
                    return (element.getUpdatedAt() != null) ?
                        element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                }
            })
            .keyBy(new KeySelector<OdsMemberUnion, Tuple1<Integer>>() {
                @Override
                public Tuple1<Integer> getKey(OdsMemberUnion value) throws Exception {
                    return new Tuple1<>(value.getShopId());
                }
            }).process(new FactMemberUnionProcessFunction())
            .uid("fact-member-union-cast-log")
            .name("fact-member-union-cast-log");


        toKafkaSink(memberGroupSingleOutputStreamOperator, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        toKafkaSink(tagDefinitionSingleOutputStreamOperator, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        toKafkaSink(memberTagSingleOutputStreamOperator, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);


        toKafkaSink(odsMemberUnionSingleOutputStreamOperator, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        toJdbcUpsertSink(odsMemberUnionSingleOutputStreamOperator, Tables.SINK_TABLE_MEMBER_FACT_UNION, OdsMemberUnion.class);
    }

    private void shopVisit() {
        //接收流
        DataStream<RawLogStream> rawLogStreamDs = this.createStreamFromKafka(SourceTopics.TOPIC_SDK_LOG_STREAM, RawLogStream.class);

        DataStream<FactMemberShopVisits> factStream = rawLogStreamDs
            .filter(new FilterFunction<RawLogStream>() {
                @Override
                public boolean filter(RawLogStream value) throws Exception {
                    //初级清洗
                    if (value.getShopId() == null || value.getShopId() <= 0
                        || value.getTaroEnv() == null || value.getEventName() == null
                        || value.getEventTime() == null || value.getDeviceId() == null) {
                        return false;
                    }

                    //心跳事件
                    if (value.getEventTime() != null && EventType.VIEW_SHOP_HEART_BEAT.getName().equals(value.getEventName()) && value.getShopId() != null) {
                        return true;
                    }
                    return false;
                }
            }).map(new MapFunction<RawLogStream, RawLogStream>() {
                @Override
                public RawLogStream map(RawLogStream value) throws Exception {
                    if (value.getUserId() == null) {
                        value.setUserId(0);
                    }
                    return value;
                }
            }).keyBy(new KeySelector<RawLogStream, Tuple3<Integer, Integer, String>>() {
                @Override
                public Tuple3<Integer, Integer, String> getKey(RawLogStream value) throws Exception {
                    return new Tuple3<Integer, Integer, String>(value.getMainShopId(), value.getShopId(), value.getDeviceId());
                }
            }).process(new VisitProcessFunction())
            .uid("fact_stream")
            .name("fact_stream");


        OutputTag<FactMemberShopVisits.FactMemberShopVisitsTimes> outvisit = new OutputTag<FactMemberShopVisits.FactMemberShopVisitsTimes>("visitTimes") {
        };
        //分流操作
        SingleOutputStreamOperator<FactMemberShopVisits> mainVisitStream = factStream.process(new ProcessFunction<FactMemberShopVisits, FactMemberShopVisits>() {
            @Override
            public void processElement(FactMemberShopVisits value, Context ctx, Collector<FactMemberShopVisits> out) throws Exception {
                if (value.getMemberId() != 0 && value.getShopId() != null) {
                    ctx.output(outvisit, FactMemberShopVisits.from(value));
                }
                out.collect(value);
            }
        });


        //输出到kafka
        toKafkaSink(mainVisitStream, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        //输出到db
        toJdbcUpsertSink(mainVisitStream, Tables.SINK_TABLE_MEMBER_SHOP_VISITS, FactMemberShopVisits.class);

        DataStream<FactMemberShopVisits.FactMemberShopVisitsTimes> sideOutput = mainVisitStream.getSideOutput(outvisit);

        toJdbcUpsertSink(sideOutput, Tables.SINK_TABLE_MEMBER_FACT_UNION, FactMemberShopVisits.FactMemberShopVisitsTimes.class);
    }

    private void wxRelation() {
        //注册sql文件
        registerSql("fact-member-wx-relation.yml");

        //注册SocialAccount表
        createTableFromJdbc("ods_social_account", "ods_social_account", SocialAccount.class);

        //注册WxRelation表
        createTableFromJdbc("ods_wx_relation", "ods_wx_relation", WxRelation.class);

        //注册WxUser表
        createTableFromJdbc("ods_wx_user", "ods_wx_user", WxUser.class);


        //用户openId绑定关系
        DataStream<SocialAccount> socialAccountDataStream = createStreamFromKafka(SourceTopics.TOPIC_MEMBER_SACIAL_ACCOUNT, SocialAccount.class);
        //转为table
        streamToTable(SocialAccount.class, socialAccountDataStream, true);
        //日志数据清洗
        Table factSocialAccountQuery = sqlQuery("factSocialAccountQuery");

        DataStream<FactSubscribedQuery> factSubscribedQueryWithTypeDataStream = tableEnv.toAppendStream(factSocialAccountQuery, FactSubscribedQuery.class);

        OutputTag<FactSubscribedQuery.FactSubscribedQueryWithType> withTypeOutputTag = new OutputTag<FactSubscribedQuery.FactSubscribedQueryWithType>("withTypeOutputTag") {
        };

        //分流操作
        SingleOutputStreamOperator<FactSubscribedQuery> mainStream = factSubscribedQueryWithTypeDataStream.process(new ProcessFunction<FactSubscribedQuery, FactSubscribedQuery>() {
            @Override
            public void processElement(FactSubscribedQuery value, Context ctx, Collector<FactSubscribedQuery> out) throws Exception {
                if (value.getId() != null && value.getShopId() != null) {
                    ctx.output(withTypeOutputTag, FactSubscribedQuery.from(value));

                }
                out.collect(value);

            }
        });

        //写入数据到kafka
        DataStream<FactSubscribedQuery.FactSubscribedQueryWithType> mainSideOutput = mainStream.getSideOutput(withTypeOutputTag);


        //写入数据到kafka
        toKafkaSink(mainSideOutput, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        toJdbcUpsertSink(mainSideOutput, Tables.SINK_TABLE_MEMBER_FACT_UNION, FactSubscribedQuery.FactSubscribedQueryWithType.class);

        //用户openId-公众号关注关系
        DataStream<WxUser> userDataStream = createStreamFromKafka(SourceTopics.TOPIC_MEMBER_WXMP_USER, WxUser.class);
        //转为table
        streamToTable(WxUser.class, userDataStream, true);
        //日志数据清洗
        Table factWxUserQuery = sqlQuery("factWxUserQuery");
        DataStream<FactSubscribedQuery> factWxUserQueryStreaem = tableEnv.toAppendStream(factWxUserQuery, FactSubscribedQuery.class);

        //分流操作
        SingleOutputStreamOperator<FactSubscribedQuery> mainfactWxUserQueryStream = factWxUserQueryStreaem.process(new ProcessFunction<FactSubscribedQuery, FactSubscribedQuery>() {
            @Override
            public void processElement(FactSubscribedQuery value, Context ctx, Collector<FactSubscribedQuery> out) throws Exception {
                if (value.getId() != null && value.getShopId() != null) {
                    ctx.output(withTypeOutputTag, FactSubscribedQuery.from(value));

                }
                out.collect(value);
            }
        });

        DataStream<FactSubscribedQuery.FactSubscribedQueryWithType> mainfactWxUserSideOutput = mainfactWxUserQueryStream.getSideOutput(withTypeOutputTag);


        //写入数据到kafka
        toKafkaSink(mainfactWxUserSideOutput, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        toJdbcUpsertSink(mainfactWxUserSideOutput, Tables.SINK_TABLE_MEMBER_FACT_UNION, FactSubscribedQuery.FactSubscribedQueryWithType.class);


        //店铺-公众号绑定关系
        DataStream<WxRelation> wxRelationDataStream = createStreamFromKafka(SourceTopics.TOPIC_MEMBER_WXAUTH_RELATION, WxRelation.class);
        //转为table
        streamToTable(WxRelation.class, wxRelationDataStream, true);
        //日志数据清洗
        Table factWxRelationQuery = sqlQuery("factWxRelationQuery");
        tableEnv.createTemporaryView("factWxRelationQuery", factWxRelationQuery);
        Table factWxRelationResultQuery = sqlQuery("factWxRelationResultQuery");

        DataStream<FactSubscribedQuery.FactSubscribedQueryWithRelation> factWxRelationResultQueryStream = tableEnv.toAppendStream(factWxRelationResultQuery, FactSubscribedQuery.FactSubscribedQueryWithRelation.class);
        //分流操作
        SingleOutputStreamOperator<FactSubscribedQuery.FactSubscribedQueryWithRelation> factWxRelationResultQueryMainStream = factWxRelationResultQueryStream.process(new ProcessFunction<FactSubscribedQuery.FactSubscribedQueryWithRelation, FactSubscribedQuery.FactSubscribedQueryWithRelation>() {
            @Override
            public void processElement(FactSubscribedQuery.FactSubscribedQueryWithRelation value, Context ctx, Collector<FactSubscribedQuery.FactSubscribedQueryWithRelation> out) throws Exception {
                if (value.getId() != null && value.getShopId() != null) {
                    ctx.output(withTypeOutputTag, FactSubscribedQuery.from(value));
                }
                out.collect(value);
            }
        });

        DataStream<FactSubscribedQuery.FactSubscribedQueryWithType> wxRelationSideOutput = factWxRelationResultQueryMainStream.getSideOutput(withTypeOutputTag);

        //写入数据到kafka
        toKafkaSink(wxRelationSideOutput, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
        toJdbcUpsertSink(wxRelationSideOutput, Tables.SINK_TABLE_MEMBER_FACT_UNION, FactSubscribedQuery.FactSubscribedQueryWithType.class);
    }


    public static class VisitProcessFunction extends KeyedProcessFunction<Tuple3<Integer, Integer, String>, RawLogStream, FactMemberShopVisits> {

        //心跳状态
        private transient ValueState<FactMemberShopVisits> factMemberShopVisitsValueState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<FactMemberShopVisits> factMemberShopVisitsValueStateDescriptor = new ValueStateDescriptor<FactMemberShopVisits>(
                // the state name
                "factMemberShopVisits",
                // type information
                FactMemberShopVisits.class);
            factMemberShopVisitsValueState = getRuntimeContext().getState(factMemberShopVisitsValueStateDescriptor);
        }

        @Override
        public void processElement(RawLogStream value, Context ctx, Collector<FactMemberShopVisits> out) throws Exception {
            FactMemberShopVisits memberShopVisits = factMemberShopVisitsValueState.value();
            boolean update = false;
            Timestamp preEendTime = null;
            if (memberShopVisits == null || memberShopVisits.getVisitsStart() == null) {
                FactMemberShopVisits factMemberShopVisits = new FactMemberShopVisits();
                factMemberShopVisits.setShopId(value.getShopId());
                factMemberShopVisits.setMemberId(value.getUserId());
                factMemberShopVisits.setDeviceId(value.getDeviceId());
                factMemberShopVisits.setVisitsStart(new Timestamp(value.getEventTime()));
                factMemberShopVisits.setVisitsEnd(new Timestamp(value.getEventTime() + 30 * 1000));// 添加默认30S的误差
                factMemberShopVisits.setVisitsDuration(30);//初始设置30秒
                factMemberShopVisits.setCreatedAt(TimeUtil.convertTimeStampToDay(factMemberShopVisits.getVisitsStart()));
                factMemberShopVisits.setUuid(UUID.randomUUID().toString());
                factMemberShopVisits.setDataType(DataType.SHOP_VIEW);
                factMemberShopVisitsValueState.update(factMemberShopVisits);
                out.collect(factMemberShopVisits);
            } else {
                update = true;
                preEendTime = memberShopVisits.getVisitsEnd();
                memberShopVisits.setVisitsEnd(new Timestamp(value.getEventTime()));
                factMemberShopVisitsValueState.update(memberShopVisits);
            }

            if (update) {
                Long timestamp = ctx.timestamp();
                ctx.timerService().deleteProcessingTimeTimer(timestamp);//删除之前的定时器， 新用定时器
                if (preEendTime != null && (value.getEventTime() - preEendTime.getTime() <= 70 * 1000)) {
                    ctx.timerService().registerProcessingTimeTimer(System.currentTimeMillis() + 40 * 1000);
                } else {
                    memberShopVisits.setVisitsEnd(new Timestamp(value.getEventTime() + 1 * 30 * 1000));
                    memberShopVisits.setVisitsDuration((int) ((memberShopVisits.getVisitsEnd().getTime() - memberShopVisits.getVisitsStart().getTime()) / 1000));
                    memberShopVisits.setDataType(DataType.SHOP_VIEW);
                    out.collect(memberShopVisits);
                    factMemberShopVisitsValueState.update(null);
                }
            } else {
                //新用定时器
                ctx.timerService().registerProcessingTimeTimer(System.currentTimeMillis() + 40 * 1000);
            }

        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<FactMemberShopVisits> out) throws Exception {
            FactMemberShopVisits memberShopVisits = factMemberShopVisitsValueState.value();
            if (memberShopVisits != null) {
                Timestamp visitsEnd = memberShopVisits.getVisitsEnd();//结束时间
                long visitsEndTime = visitsEnd.getTime();
                if (timestamp - visitsEndTime > 95 * 1000) {//最新时间减去上次 更新时间 大于95S  之前是有加 30 S的 误差
                    memberShopVisits.setVisitsEnd(new Timestamp(visitsEndTime + 1 * 30 * 1000));
                    memberShopVisits.setVisitsDuration((int) ((memberShopVisits.getVisitsEnd().getTime() - memberShopVisits.getVisitsStart().getTime()) / 1000));
                    memberShopVisits.setDataType(DataType.SHOP_VIEW);
                    out.collect(memberShopVisits);
                    factMemberShopVisitsValueState.update(null);
                } else {
                    //每次重新
                    ctx.timerService().registerProcessingTimeTimer(timestamp + 40 * 1000);
                }
            } else {
                factMemberShopVisitsValueState.update(null);
            }
        }
    }

    //标签关联
    public static class FactMemberTagProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, OdsMemberTag, OdsMemberTag> {

        @Override
        public void processElement(OdsMemberTag value, Context ctx, Collector<OdsMemberTag> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            value.setDataType(DataType.MEMBER_TAG);
            out.collect(value);
        }
    }

    //标签定义
    public static class FactMemberTagDefinitionProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, OdsTagDefinition, OdsTagDefinition> {

        @Override
        public void processElement(OdsTagDefinition value, Context ctx, Collector<OdsTagDefinition> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            value.setDataType(DataType.SHOP_MEMBER_TAG_DEFINITION);
            out.collect(value);
        }
    }


    //群组定义
    public static class FactMemberGroupProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, OdsMemberGroup, OdsMemberGroup> {

        @Override
        public void processElement(OdsMemberGroup value, Context ctx, Collector<OdsMemberGroup> out) throws Exception {
            value.setDataType(DataType.SHOP_MEMBER_GROUP);
            out.collect(value);
        }

    }


    //会员union 信息
    public static class FactMemberUnionProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, OdsMemberUnion, OdsMemberUnion> {

        @Override
        public void processElement(OdsMemberUnion value, Context ctx, Collector<OdsMemberUnion> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            value.setDataType(DataType.MEMBER_UNION);
            out.collect(value);
        }
    }


}
