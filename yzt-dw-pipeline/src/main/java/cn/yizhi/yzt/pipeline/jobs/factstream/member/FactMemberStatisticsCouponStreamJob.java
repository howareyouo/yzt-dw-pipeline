
package cn.yizhi.yzt.pipeline.jobs.factstream.member;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.common.EventType;
import cn.yizhi.yzt.pipeline.common.PackType;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.member.*;
import cn.yizhi.yzt.pipeline.model.fact.member.coupon.FactMemberCoupon;
import cn.yizhi.yzt.pipeline.model.fact.member.coupon.FactMemberCouponLog;
import cn.yizhi.yzt.pipeline.model.ods.Coupon;
import cn.yizhi.yzt.pipeline.model.ods.OdsLogStream;
import cn.yizhi.yzt.pipeline.model.fact.member.FactOrderItemPack;
import cn.yizhi.yzt.pipeline.model.fact.member.FactPackOdsLog;
import cn.yizhi.yzt.pipeline.model.fact.member.FactProductReGroup;
import cn.yizhi.yzt.pipeline.model.fact.member.OdsProductReGroup;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.table.api.Table;
import org.apache.flink.util.Collector;

import java.sql.Timestamp;

/**
 * @Author: HuCheng
 * 会员商品指标-天
 * @Date: 2021/1/5 14:22
 */
public class FactMemberStatisticsCouponStreamJob extends StreamJob {
    //3天清除
    private static final StateTtlConfig ttlConfig = StateTtlConfig
            .newBuilder(org.apache.flink.api.common.time.Time.days(3))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    //400天
    private static final StateTtlConfig groupConfig = StateTtlConfig
            .newBuilder(org.apache.flink.api.common.time.Time.days(400))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    private static final StateTtlConfig couponStateTtlConfig = StateTtlConfig
            .newBuilder(org.apache.flink.api.common.time.Time.days(400))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {

        //订单
        coupon();

    }

    private void coupon() {
        //日志流 、 分享了优惠券的。 有 coupon_template_id
        SingleOutputStreamOperator<FactMemberCouponLog> factMemberCouponLogStream = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, OdsLogStream.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsLogStream>() {
                    @Override
                    public long extractAscendingTimestamp(OdsLogStream element) {
                        return element.getEventTime().getTime();
                    }
                }).filter(new FilterFunction<OdsLogStream>() {
                    @Override
                    public boolean filter(OdsLogStream value) throws Exception {
                        return EventType.SHARE_COUPON.getName().equals(value.getEventName()) && value.getUserId() != null
                                && value.getCouponTemplateId() != null;
                    }
                }).keyBy(new KeySelector<OdsLogStream, Tuple4<String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, Integer> getKey(OdsLogStream value) throws Exception {
                        return new Tuple4<>(TimeUtil.convertTimeStampToDay(value.getEventTime()), value.getShopId(), value.getUserId(), value.getCouponTemplateId());
                    }
                }).process(new FactMemberCouponLogProcessFunction())
                .uid("mg-fact-member-coupon-log")
                .name("mg-fact-member-coupon-log");

        //输出到数据库
        toJdbcUpsertSink(factMemberCouponLogStream, Tables.SINK_TABLE_MEMBER_COUPON, FactMemberCouponLog.class);
        toKafkaSink(factMemberCouponLogStream, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);


        // 优惠券流
        DataStream<FactMemberCoupon> factMemberCouponDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_COUPON, Coupon.class)
                .keyBy(new KeySelector<Coupon, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(Coupon value) {
                        return new Tuple1<>(value.getShopId());
                    }
                })

                .filter(new RichFilterFunction<Coupon>() {
                    private transient MapState<Long, Integer> couponMapState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        MapStateDescriptor<Long, Integer> mapStateDescriptor = new MapStateDescriptor<>(
                                // the state name
                                "couponMapState",
                                // type information
                                Types.LONG, Types.INT);
                        mapStateDescriptor.enableTimeToLive(couponStateTtlConfig);
                        couponMapState = getRuntimeContext().getMapState(mapStateDescriptor);
                    }

                    @Override
                    public boolean filter(Coupon value) throws Exception {

                        if (couponMapState.contains(value.getId())) {
                            Integer state = couponMapState.get(value.getId());
                            // 是核销进来的。
                            // '核销类型，0:未核销 1:商城核销，2:验证工具核销', 核销时间是同一天
                            if (!value.getState().equals(state)) {
                                if (value.getVerificationType() != 0) {
                                    couponMapState.remove(value.getId());
                                }
                                return true;
                            }
                        } else {
                            // 不是核销，是领券
                            if (value.getState() == 0 && value.getUsedAt() == null) {
                                couponMapState.put(value.getId(), value.getState());
                                return true;
                            }
                        }

                        return false;
                    }
                })
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Coupon>() {
                    @Override
                    public long extractAscendingTimestamp(Coupon element) {
                        return (element.getUpdateAt() != null) ?
                                element.getUpdateAt().getTime() : element.getCreatedAt().getTime();
                    }
                })
                .keyBy(new KeySelector<Coupon, Tuple4<String, Integer, Integer, Integer>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, Integer> getKey(Coupon element) {
                        Timestamp eventTime = (element.getUpdateAt() != null) ?
                                element.getUpdateAt() : element.getCreatedAt();

                        return new Tuple4<>(TimeUtil.convertTimeStampToDay(eventTime), element.getShopId(), element.getMemberId(), element.getCouponTemplateId());
                    }
                }).process(new FactMemberCouponOdsProcessFunction())
                .uid("mg-fact-member-coupon-ods")
                .name("mg-fact-member-coupon-ods");


        toJdbcUpsertSink(factMemberCouponDataStream, Tables.SINK_TABLE_MEMBER_COUPON, FactMemberCoupon.class);
        toKafkaSink(factMemberCouponDataStream, SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP);
    }

    private void productPackGroup() {
        //注册sql文件
        this.registerSql("fact-product-re-group.yml");

        //注册表
        this.createTableFromJdbc("ods_product_re_group", "ods_product_re_group", OdsProductReGroup.class);

        //初始化规则数据
        Table factProductReGroupQuery = this.sqlQuery("factProductReGroupQuery");
        DataStream<FactProductReGroup> tableReGroupStream = tableEnv.toAppendStream(factProductReGroupQuery, FactProductReGroup.class);

        //更新的规则数据
        DataStream<OdsProductReGroup> topicReGroupStream = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_PRODUCT_RE_GROUP, OdsProductReGroup.class);

        //包装初始化规则-logStream
        DataStream<FactPackOdsLog> tableProductRefundDs = tableReGroupStream
                .map(new MapFunction<FactProductReGroup, FactPackOdsLog>() {
                    @Override
                    public FactPackOdsLog map(FactProductReGroup value) throws Exception {
                        FactPackOdsLog f = new FactPackOdsLog();
                        f.setPackType(PackType.PACKING_DATA);
                        f.setGroupId(value.getGroup_id());
                        f.setShopId(value.getShop_id());
                        f.setGoodsId(value.getProduct_id());
                        return f;
                    }
                });

        //包装初始化规则-订单
        DataStream<FactOrderItemPack> tablePackOrderItem = tableReGroupStream
                .map(new MapFunction<FactProductReGroup, FactOrderItemPack>() {
                    @Override
                    public FactOrderItemPack map(FactProductReGroup value) throws Exception {
                        FactOrderItemPack f = new FactOrderItemPack();
                        f.setPackType(PackType.PACKING_DATA);
                        f.setGroupId(value.getGroup_id());
                        f.setShopId(value.getShop_id());
                        f.setProductId(value.getProduct_id());
                        return f;
                    }
                });


        //包装更新的规则-logStream
        DataStream<FactPackOdsLog> topicPackOdsLog = topicReGroupStream
                .map(new MapFunction<OdsProductReGroup, FactPackOdsLog>() {
                    @Override
                    public FactPackOdsLog map(OdsProductReGroup value) throws Exception {
                        FactPackOdsLog f = new FactPackOdsLog();
                        f.setPackType(PackType.PACKING_DATA);
                        f.setGroupId(value.getGroupId());
                        f.setShopId(value.getShopId());
                        f.setGoodsId(value.getProductId());
                        return f;
                    }
                });

        //包装更新的规则-订单
        DataStream<FactOrderItemPack> topicOrderItemPack = topicReGroupStream
                .map(new MapFunction<OdsProductReGroup, FactOrderItemPack>() {
                    @Override
                    public FactOrderItemPack map(OdsProductReGroup value) throws Exception {
                        FactOrderItemPack f = new FactOrderItemPack();
                        f.setPackType(PackType.PACKING_DATA);
                        f.setGroupId(value.getGroupId());
                        f.setShopId(value.getShopId());
                        f.setProductId(value.getProductId());
                        return f;
                    }
                });


        //包装日志流-logSteam
        DataStream<FactPackOdsLog> packOdsLogDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_ODS_LOG_STREAM, FactPackOdsLog.class)
                .map(new MapFunction<FactPackOdsLog, FactPackOdsLog>() {
                    @Override
                    public FactPackOdsLog map(FactPackOdsLog value) throws Exception {
                        value.setPackType(PackType.RAW_DATA);
                        return value;
                    }
                });

        //包装订单流
        DataStream<FactOrderItemPack> factOrderItemPackSingleOutputStreamOperator = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_ITEM, FactOrderItemPack.class)
                .map(new MapFunction<FactOrderItemPack, FactOrderItemPack>() {
                    @Override
                    public FactOrderItemPack map(FactOrderItemPack value) throws Exception {
                        value.setPackType(PackType.RAW_DATA);
                        return value;
                    }
                });


        //写入kafka-logSteam
        //topic数据写入到 聚合topic
        toKafkaSink(topicPackOdsLog, SourceTopics.TOPIC_FACT_PACK_ODS_LOG);
        toKafkaSink(topicOrderItemPack, SourceTopics.TOPIC_FACT_PACK_ORDER_ITEM);

        //table数据写入到 聚合topic
        toKafkaSink(tablePackOrderItem, SourceTopics.TOPIC_FACT_PACK_ORDER_ITEM);
        toKafkaSink(tableProductRefundDs, SourceTopics.TOPIC_FACT_PACK_ODS_LOG);

        //原始数据
        toKafkaSink(packOdsLogDataStream, SourceTopics.TOPIC_FACT_PACK_ODS_LOG);
        toKafkaSink(factOrderItemPackSingleOutputStreamOperator, SourceTopics.TOPIC_FACT_PACK_ORDER_ITEM);
    }

    //计算日志数据
    public static class FactMemberCouponLogProcessFunction extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, OdsLogStream, FactMemberCouponLog> {
        //定义state

        private transient ValueState<Integer> shareTimesState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> shareTimesStateNumberStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "shareTimesState",
                    // type information
                    Types.INT);
            shareTimesStateNumberStateDescriptor.enableTimeToLive(ttlConfig);
            shareTimesState = getRuntimeContext().getState(shareTimesStateNumberStateDescriptor);
        }

        @Override
        public void processElement(OdsLogStream value, Context ctx, Collector<FactMemberCouponLog> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getEventTime().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            Integer templateIdStateCounts = shareTimesState.value();


            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 86400000;
            //未超时进行计算
            if (judge >= watermark) {
                if (templateIdStateCounts == null) {
                    shareTimesState.update(1);
                } else {
                    shareTimesState.update(templateIdStateCounts + 1);
                }

                FactMemberCouponLog memberCouponLog = new FactMemberCouponLog();
                Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
                memberCouponLog.setAnalysisDate(currentKey.f0);
                memberCouponLog.setShopId(currentKey.f1);
                memberCouponLog.setMemberId(currentKey.f2);
                memberCouponLog.setCouponTemplateId(currentKey.f3);
                if (value.getCouponType() != null) {
                    memberCouponLog.setCouponType(value.getCouponType());
                }
                memberCouponLog.setShareTimes(shareTimesState.value() == null ? 0 : shareTimesState.value());
                memberCouponLog.setDataType(DataType.COUPON_LOG);
                out.collect(memberCouponLog);
            }
        }
    }


    //计算日志数据
    public static class FactMemberCouponOdsProcessFunction extends KeyedProcessFunction<Tuple4<String, Integer, Integer, Integer>, Coupon, FactMemberCoupon> {
        //定义state
        //核销次数
        private transient ValueState<Integer> userdTimes;
        //领取次数
        private transient ValueState<Integer> applyTimes;
        //过期次数
        private transient ValueState<Integer> expiredTimes;
        // 失效次数
        private transient ValueState<Integer> deprecatedTimes;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> writeOffNumberStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "userdTimes",
                    // type information
                    Types.INT);
            writeOffNumberStateDescriptor.enableTimeToLive(ttlConfig);
            userdTimes = getRuntimeContext().getState(writeOffNumberStateDescriptor);

            ValueStateDescriptor<Integer> applyTimesNumberStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "applyTimes",
                    // type information
                    Types.INT);
            applyTimesNumberStateDescriptor.enableTimeToLive(ttlConfig);
            applyTimes = getRuntimeContext().getState(applyTimesNumberStateDescriptor);

            ValueStateDescriptor<Integer> expiredTimesNumberStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "expiredTimes",
                    // type information
                    Types.INT);
            expiredTimesNumberStateDescriptor.enableTimeToLive(ttlConfig);
            expiredTimes = getRuntimeContext().getState(expiredTimesNumberStateDescriptor);

            ValueStateDescriptor<Integer> deprecatedTimesNumberStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "deprecatedTimes",
                    // type information
                    Types.INT);
            deprecatedTimesNumberStateDescriptor.enableTimeToLive(ttlConfig);
            deprecatedTimes = getRuntimeContext().getState(deprecatedTimesNumberStateDescriptor);
        }

        @Override
        public void processElement(Coupon value, Context ctx, Collector<FactMemberCoupon> out) throws Exception {

            //数据时间,乘以1000就是毫秒的。
            Timestamp eventTime = (value.getUpdateAt() != null) ?
                    value.getUpdateAt() : value.getCreatedAt();

            Integer userdTimesCount = userdTimes.value();
            Integer applyTimesCount = applyTimes.value();
            Integer expiredTimesCount = expiredTimes.value();
            Integer deprecatedTimesCount = deprecatedTimes.value();

            // 核销
            //'核销类型，0:未核销 1:商城核销，2:验证工具核销', 核销时间是同一天
            if (value.getVerificationType() != 0 && TimeUtil.convertTimeStampToDay(value.getUsedAt()).equals(TimeUtil.convertTimeStampToDay(eventTime))) {
                if (userdTimesCount == null) {
                    userdTimes.update(1);
                } else {
                    userdTimes.update(userdTimesCount + 1);
                }
            }
            // 说明是领取优惠券
            if (TimeUtil.convertTimeStampToDay(value.getCreatedAt()).equals(TimeUtil.convertTimeStampToDay(eventTime)) && value.getState() == 0 && value.getUsedAt() == null) {
                if (applyTimesCount == null) {
                    applyTimes.update(1);
                } else {
                    applyTimes.update(applyTimesCount + 1);
                }
            }
            //'核销类型，0:未核销 1:商城核销，2:验证工具核销', 核销时间是同一天
            //'用券状态 0:未使用，1:已使用，2:已失效，3:作废
            if (value.getVerificationType() == 0 && value.getState() == 2) {
                if (expiredTimesCount == null) {
                    expiredTimes.update(1);
                } else {
                    expiredTimes.update(expiredTimesCount + 1);
                }
            }

            //'用券状态 0:未使用，1:已使用，2:已失效，3:作废
            if (value.getVerificationType() == 0 && value.getState() == 3) {
                if (deprecatedTimesCount == null) {
                    deprecatedTimes.update(1);
                } else {
                    deprecatedTimes.update(deprecatedTimesCount + 1);
                }
            }

            FactMemberCoupon memberCoupon = new FactMemberCoupon();
            Tuple4<String, Integer, Integer, Integer> currentKey = ctx.getCurrentKey();
            memberCoupon.setAnalysisDate(currentKey.f0);
            memberCoupon.setShopId(currentKey.f1);
            memberCoupon.setMemberId(currentKey.f2);
            memberCoupon.setCouponTemplateId(currentKey.f3);
            if (value.getCouponType() != null) {
                memberCoupon.setCouponType(value.getCouponType());
            }
            memberCoupon.setApplyTimes(applyTimes.value() == null ? 0 : applyTimes.value());
            memberCoupon.setUsedTimes(userdTimes.value() == null ? 0 : userdTimes.value());
            memberCoupon.setDeprecatedTimes(deprecatedTimes.value() == null ? 0 : deprecatedTimes.value());
            memberCoupon.setExpiredTimes(expiredTimes.value() == null ? 0 : expiredTimes.value());
            memberCoupon.setDataType(DataType.COUPON_RECEIVED);

            out.collect(memberCoupon);
        }
    }

}
