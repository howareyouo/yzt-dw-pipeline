package cn.yizhi.yzt.pipeline.jobs.factstream.member;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.jdbc.JdbcClient;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.dto.MemberGroupStreamResultDto;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberShopVisits;
import cn.yizhi.yzt.pipeline.model.fact.member.*;
import cn.yizhi.yzt.pipeline.model.fact.member.coupon.FactMemberCoupon;
import cn.yizhi.yzt.pipeline.model.fact.member.coupon.FactMemberCouponLog;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterChain;
import cn.yizhi.yzt.pipeline.model.fact.member.order.FactMemberOrder;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductLog;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductOrder;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductRefund;
import cn.yizhi.yzt.pipeline.model.ods.OdsMemberGroup;
import cn.yizhi.yzt.pipeline.model.ods.OdsMemberTag;
import cn.yizhi.yzt.pipeline.model.ods.OdsMemberUnion;
import cn.yizhi.yzt.pipeline.model.ods.OdsTagDefinition;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.apache.flink.api.common.time.Time.days;
import static org.apache.flink.api.common.time.Time.hours;

/**
 * ?????????????????????
 * winner
 * 2021-0105
 */
public class FactMemberCaluStreamJob extends StreamJob {
    //400?????????
    private static final StateTtlConfig ttlConfig = StateTtlConfig
        .newBuilder(days(400))
        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
        .build();

    private static final StateTtlConfig dayConfig = StateTtlConfig
        .newBuilder(days(1))
        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
        .build();

    private static final StateTtlConfig hourConfig = StateTtlConfig
        .newBuilder(hours(1))
        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
        .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
        .build();

    public static volatile Cache<String, Object> cache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .initialCapacity(20)
        .expireAfterWrite(3, TimeUnit.HOURS)
        .refreshAfterWrite(2, TimeUnit.HOURS)
        .build(key -> (key));


    public final static int TEST_SHOP = 261;

    @Override
    public void defineJob() throws Exception {


        calMemberGroup();
    }

    public void calMemberGroup() {
        //????????????
        DataStream<MgFactMemberUnion> mgFactMemberDs = this.createStreamFromKafka(SourceTopics.TOPIC_MG_FACT_MEMBER_UNION_GROUP, MgFactMemberUnion.class);

        DataStream<MemberGroupStreamResultDto> memberGroupStreamResultDtoDataStream = mgFactMemberDs
            .filter(a -> a.getShopId() != null && a.getShopId() != 0)
            .keyBy(new KeySelector<MgFactMemberUnion, Tuple1<Integer>>() {
                @Override
                public Tuple1<Integer> getKey(MgFactMemberUnion value) throws Exception {
                    return new Tuple1<>(value.getShopId());
                }
            })
            .process(new GroupProcessFunction(this.serverConfig))
            .uid("TOPIC_MG_FACT_MEMBER_DAILY")
            .name("TOPIC_MG_FACT_MEMBER_DAILY");


        OutputTag<MemberGroupStreamResultDto> outTag = new OutputTag<MemberGroupStreamResultDto>("tag") {
        };
        OutputTag<MemberGroupStreamResultDto> outGroup = new OutputTag<MemberGroupStreamResultDto>("group") {
        };
        //????????????
        SingleOutputStreamOperator<MemberGroupStreamResultDto> sideoutStream = memberGroupStreamResultDtoDataStream.process(new ProcessFunction<MemberGroupStreamResultDto, MemberGroupStreamResultDto>() {
            @Override
            public void processElement(MemberGroupStreamResultDto value, Context ctx, Collector<MemberGroupStreamResultDto> out) throws Exception {


                if (value.isUpdate() && value.getSourceType() != null && value.getSourceType().equals(MemberGroupStreamResultDto.SourceType.SMART_TAG)) {
                    // emit data to side output
                    ctx.output(outTag, value);

                } else if (value.isUpdate() && value.getSourceType() != null && value.getSourceType().equals(MemberGroupStreamResultDto.SourceType.GROUP)) {
                    ctx.output(outGroup, value);

                } else {
                    out.collect(value);
                }
            }
        });


        DataStream<MemberGroupStreamResultDto> factSmartMemberTagDataStream = sideoutStream.getSideOutput(outTag);

        DataStream<MemberGroupStreamResultDto> factMemberGroupPopulationDataStream = sideoutStream.getSideOutput(outGroup);

        toKafkaSink(factSmartMemberTagDataStream, SourceTopics.TOPIC_TAG_UPDATE_TO_MEMBER);

        toKafkaSink(factMemberGroupPopulationDataStream, SourceTopics.TOPIC_MEMBER_GROUP_UPDATE_TO_BIGDATA);


    }


    public static class GroupProcessFunction extends KeyedProcessFunction<Tuple1<Integer>, MgFactMemberUnion, MemberGroupStreamResultDto> {
        //?????????????????????????????????     ????????? ??????ID???Map<?????????Map< ??????ID:???????????????????????????????????????????????????>>
        private transient MapState<Integer, Map<String, Map<Integer, FactMemberProductLog>>> factMemberProductLogState;
        //????????????     ????????? ??????ID???Map<?????????Map< ??????ID:???????????????????????????????????????>>
        private transient MapState<Integer, Map<String, Map<Integer, FactMemberProductOrder>>> factMemberProductOrderState;

        //????????????????????????     ????????? ??????ID???Map<?????????Map< ??????ID:???????????????????????????????????????>>
        private transient MapState<Integer, Map<String, Map<Integer, FactMemberProductRefund>>> factMemberProductRefundState;

        //???????????????????????????     ????????? ??????ID???Map<?????????Map< ?????????ID:???????????????????????????????????????>>
        private transient MapState<Integer, Map<String, Map<Integer, FactMemberCoupon>>> factMemberCouponState;

        //?????????????????????     ????????? ??????ID???Map<?????????Map< ?????????ID:?????????????????????????????????>>
        private transient MapState<Integer, Map<String, Map<Integer, FactMemberCouponLog>>> factMemberCouponLogState;

        //?????????????????????     ????????? ??????ID???Map<?????????Map< ?????????ID:??????????????????>>
        private transient MapState<Integer, Map<String, FactMemberOrder>> factMemberOrderState;

        //????????????     ????????? ??????ID?????????????????????????????????
        private transient MapState<Integer, List<FactMemberShopVisits>> factMemberShopVisitsState;

        //??????????????????  ????????? ??????ID?????????????????????
        private transient MapState<Integer, FactMemberUnion> factMemberBaseState;

        //jdbc????????????
        private transient JdbcClient jdbcClient;


        //??????????????????   ????????? ??????ID?????????????????????
        private transient MapState<Integer, OdsMemberGroup> odsMemberGroupState;

        //?????????????????????????????????  ????????? ??????ID?????????ID??????
        private transient MapState<Integer, List<Integer>> odsMemberGroupMemberIds;

        //??????????????????     ????????? ??????ID?????????????????????
        private transient MapState<Integer, OdsTagDefinition> odsTagDefinitionState;

        //?????????????????????????????????      ????????? ??????ID?????????ID??????
        private transient MapState<Integer, List<Integer>> odsTagDefinitionMemberIds;


        //?????????memberIds ??????????????????????????????????????????
        private transient MapState<String, List<Integer>> todayMemberIds;

        //???????????????????????????????????????????????????
        private transient ValueState<Boolean> cleanState;

        //????????????state
        private transient ValueState<Boolean> timeState;

        protected ServerConfig serverConfig;

        public GroupProcessFunction(ServerConfig serverConfig) {
            this.serverConfig = serverConfig;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            MapStateDescriptor<Integer, Map<String, Map<Integer, FactMemberProductLog>>> factMemberProductLogStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "factMemberProductLogState",
                // type information,
                Types.INT,
                Types.MAP(Types.STRING, Types.MAP(Types.INT, TypeInformation.of(FactMemberProductLog.class))));
            factMemberProductLogStateDescriptor.enableTimeToLive(ttlConfig);
            factMemberProductLogState = getRuntimeContext().getMapState(factMemberProductLogStateDescriptor);


            MapStateDescriptor<Integer, Map<String, Map<Integer, FactMemberProductOrder>>> factMemberProductOrderStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "factMemberProductOrderState",
                // type information,
                Types.INT,
                Types.MAP(Types.STRING, Types.MAP(Types.INT, TypeInformation.of(FactMemberProductOrder.class))));
            factMemberProductOrderStateDescriptor.enableTimeToLive(ttlConfig);
            factMemberProductOrderState = getRuntimeContext().getMapState(factMemberProductOrderStateDescriptor);

            MapStateDescriptor<Integer, Map<String, Map<Integer, FactMemberProductRefund>>> factMemberProductRefundStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "factMemberProductRefundState",
                // type information,
                Types.INT,
                Types.MAP(Types.STRING, Types.MAP(Types.INT, TypeInformation.of(FactMemberProductRefund.class))));
            factMemberProductRefundStateDescriptor.enableTimeToLive(ttlConfig);
            factMemberProductRefundState = getRuntimeContext().getMapState(factMemberProductRefundStateDescriptor);


            MapStateDescriptor<Integer, Map<String, Map<Integer, FactMemberCoupon>>> factMemberCouponStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "factMemberCouponState",
                // type information,
                Types.INT,
                Types.MAP(Types.STRING, Types.MAP(Types.INT, TypeInformation.of(FactMemberCoupon.class))));
            factMemberCouponStateDescriptor.enableTimeToLive(ttlConfig);
            factMemberCouponState = getRuntimeContext().getMapState(factMemberCouponStateDescriptor);


            MapStateDescriptor<Integer, Map<String, Map<Integer, FactMemberCouponLog>>> factMemberCouponLogStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "factMemberCouponLogState",
                // type information,
                Types.INT,
                Types.MAP(Types.STRING, Types.MAP(Types.INT, TypeInformation.of(FactMemberCouponLog.class))));
            factMemberCouponLogStateDescriptor.enableTimeToLive(ttlConfig);
            factMemberCouponLogState = getRuntimeContext().getMapState(factMemberCouponLogStateDescriptor);


            MapStateDescriptor<Integer, Map<String, FactMemberOrder>> factMemberOrderStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "factMemberOrderState",
                // type information,
                Types.INT,
                Types.MAP(Types.STRING, TypeInformation.of(FactMemberOrder.class)));
            factMemberOrderStateDescriptor.enableTimeToLive(ttlConfig);
            factMemberOrderState = getRuntimeContext().getMapState(factMemberOrderStateDescriptor);


            MapStateDescriptor<Integer, List<FactMemberShopVisits>> factMemberShopVisitsStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "factMemberShopVisitsState",
                // type information,
                Types.INT,
                Types.LIST(TypeInformation.of(FactMemberShopVisits.class)));
            factMemberShopVisitsStateDescriptor.enableTimeToLive(ttlConfig);
            factMemberShopVisitsState = getRuntimeContext().getMapState(factMemberShopVisitsStateDescriptor);


            MapStateDescriptor<Integer, FactMemberUnion> factMemberBaseStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "factMemberBaseState",
                // type information
                Types.INT,
                TypeInformation.of(FactMemberUnion.class));
            factMemberBaseStateDescriptor.enableTimeToLive(ttlConfig);
            factMemberBaseState = getRuntimeContext().getMapState(factMemberBaseStateDescriptor);


            MapStateDescriptor<Integer, OdsMemberGroup> odsMemberGroupStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "odsMemberGroupState",
                // type information
                Types.INT,
                TypeInformation.of(OdsMemberGroup.class));
            odsMemberGroupStateDescriptor.enableTimeToLive(ttlConfig);
            odsMemberGroupState = getRuntimeContext().getMapState(odsMemberGroupStateDescriptor);


            MapStateDescriptor<Integer, OdsTagDefinition> odsTagDefinitionStateDescriptor = new MapStateDescriptor<>(
                // the state name
                "odsTagDefinitionState",
                // type information
                Types.INT,
                TypeInformation.of(OdsTagDefinition.class));
            odsTagDefinitionStateDescriptor.enableTimeToLive(ttlConfig);
            odsTagDefinitionState = getRuntimeContext().getMapState(odsTagDefinitionStateDescriptor);

            MapStateDescriptor<Integer, List<Integer>> odsMemberGroupMemberIdsDescriptor = new MapStateDescriptor<>(
                // the state name
                "odsMemberGroupMemberIds",
                // type information,
                Types.INT,
                Types.LIST(Types.INT));
            odsMemberGroupMemberIdsDescriptor.enableTimeToLive(ttlConfig);
            odsMemberGroupMemberIds = getRuntimeContext().getMapState(odsMemberGroupMemberIdsDescriptor);


            MapStateDescriptor<Integer, List<Integer>> odsTagDefinitionMemberIdsDescriptor = new MapStateDescriptor<>(
                // the state name
                "odsTagDefinitionMemberIds",
                // type information,
                Types.INT,
                Types.LIST(Types.INT));
            odsTagDefinitionMemberIdsDescriptor.enableTimeToLive(ttlConfig);
            odsTagDefinitionMemberIds = getRuntimeContext().getMapState(odsTagDefinitionMemberIdsDescriptor);

            MapStateDescriptor<String, List<Integer>> todayMemberIdsDescriptor = new MapStateDescriptor<>(
                // the state name
                "todayMemberIds",
                // type information,
                Types.STRING,
                Types.LIST(Types.INT));
            todayMemberIdsDescriptor.enableTimeToLive(dayConfig);//?????????????????????
            todayMemberIds = getRuntimeContext().getMapState(todayMemberIdsDescriptor);


            ValueStateDescriptor<Boolean> cleanStateStateDescriptor = new ValueStateDescriptor<>(
                // the state name
                "cleanStateState",
                // type information
                Types.BOOLEAN);
            cleanStateStateDescriptor.enableTimeToLive(dayConfig);
            cleanState = getRuntimeContext().getState(cleanStateStateDescriptor);

            ValueStateDescriptor<Boolean> timeStateDescriptor = new ValueStateDescriptor<>(
                // the state name
                "timeStateState",
                // type information
                Types.BOOLEAN);
            timeStateDescriptor.enableTimeToLive(hourConfig);
            timeState = getRuntimeContext().getState(timeStateDescriptor);


        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<MemberGroupStreamResultDto> out) throws Exception {
            if (timeState.value()) {
                boolean dealAll = false;
                LocalDate now = LocalDate.now(TimeUtil.ZONE_ID);
                //?????????????????????????????????????????? cleanState????????????????????????????????????false
                if (cleanState.value() == null || !cleanState.value()) {
                    cleanState.update(true);
                    dealAll = true;
                }

                MemberGroupStreamResultDto streamResultDto = new MemberGroupStreamResultDto();
                streamResultDto.setShopId(ctx.getCurrentKey().f0);

                for (OdsMemberGroup memberGroup : odsMemberGroupState.values()) {
                    dealGroupsOrTag(streamResultDto.getShopId(), MemberGroupStreamResultDto.SourceType.GROUP, memberGroup.getId(), memberGroup.getFilters(), streamResultDto, dealAll, now);
                    if (streamResultDto.isUpdate()) {
                        out.collect(streamResultDto);
                    }
                }

                for (OdsTagDefinition tagDefinition : odsTagDefinitionState.values()) {
                    dealGroupsOrTag(streamResultDto.getShopId(), MemberGroupStreamResultDto.SourceType.SMART_TAG, tagDefinition.getId(), tagDefinition.getFilters(), streamResultDto, dealAll, now);
                    if (streamResultDto.isUpdate()) {
                        out.collect(streamResultDto);
                    }
                }
            }

            timeState.update(false);

        }

        @Override
        public void processElement(MgFactMemberUnion value, Context ctx, Collector<MemberGroupStreamResultDto> out) throws Exception {
            Integer shopId = ctx.getCurrentKey().f0;
            //MEMBER_UNION  MEMBER_TOTAL ???shop_member_id ??????id
            //SHOP_MEMBER_GROUP   SHOP_MEMBER_TAG_DEFINITION  ?????????shop_memberid???
            //            System.out.println("?????????MgFactMemberUnion???" + JsonMapper.nonEmptyMapper().toJson(value));

            String analysisDate = value.getAnalysisDate();
            LocalDate now = LocalDate.now(TimeUtil.ZONE_ID);

            DataType dataType = value.getDataType();
            if (dataType != DataType.SHOP_MEMBER_GROUP && dataType != DataType.SHOP_MEMBER_TAG_DEFINITION) {
                Integer memberId = value.getId() == null ? value.getMemberId() : value.getId();
                //??????????????????
                if (dataType.equals(DataType.MEMBER_UNION) || dataType.equals(DataType.MEMBER_TOTAL)) {
                    memberId = value.getId();
                }

                if (todayMemberIds != null && todayMemberIds.contains(now.toString())) {
                    List<Integer> todayMs = todayMemberIds.get(now.toString());
                    if (memberId != null && !todayMs.contains(memberId)) {
                        todayMs.add(memberId);
                        todayMemberIds.put(now.toString(), todayMs);
                    }
                } else {
                    if (todayMemberIds != null) {
                        todayMemberIds.clear();
                    }

                    List<Integer> memberIds = new ArrayList<>();
                    if (memberId != null) {
                        memberIds.add(memberId);
                    }
                    todayMemberIds.put(now.toString(), memberIds);
                }

            }


            MemberGroupStreamResultDto streamResultDto = new MemberGroupStreamResultDto();
            streamResultDto.setShopId(ctx.getCurrentKey().f0);


            //?????????????????????????????? ??????
            boolean dealAll = false;


            boolean immediately = false;


            switch (value.getDataType()) {

                case MEMBER_WX_SUBSCRIBED:
                    FactSubscribedQuery factSubscribedQuery = MgFactMemberUnion.buildFactMemberBaseWxSubscribed(value);
                    FactMemberUnion factMemberUnion = factMemberBaseState.get(factSubscribedQuery.getId());
                    factMemberUnion = FactMemberUnion.mergeSubscribed(factMemberUnion, factSubscribedQuery);
                    factMemberBaseState.put(factMemberUnion.getId(), factMemberUnion);
                    break;

                case PRODUCT_LOG:
                    FactMemberProductLog factMemberProductLog = MgFactMemberUnion.buildFactMemberProductLog(value);
                    factMemberProductLog(factMemberProductLog.getMemberId(), analysisDate, factMemberProductLog);
                    break;

                case PRODUCT_ORDER:
                    FactMemberProductOrder factMemberProductOrder = MgFactMemberUnion.buildFactMemberProductOrder(value);
                    factMemberProductOrder(factMemberProductOrder.getMemberId(), analysisDate, factMemberProductOrder);
                    break;

                case PRODUCT_REFUND:
                    FactMemberProductRefund factMemberProductRefund = MgFactMemberUnion.buildFactMemberProductRefund(value);
                    factMemberProductRefund(factMemberProductRefund.getMemberId(), analysisDate, factMemberProductRefund);
                    break;


                case MEMBER_ORDER:
                    FactMemberOrder memberOrder = MgFactMemberUnion.buildFactMemberOrder(value);
                    factMemberOrder(memberOrder.getMemberId(), analysisDate, memberOrder);
                    break;

                case COUPON_LOG:
                    FactMemberCouponLog factMemberCouponLog = MgFactMemberUnion.buildFactMemberCouponLog(value);
                    factMemberCouponLog(factMemberCouponLog.getMemberId(), analysisDate, factMemberCouponLog);
                    break;

                case COUPON_RECEIVED:
                    FactMemberCoupon factMemberCoupon = MgFactMemberUnion.buildFactMemberCoupon(value);
                    factMemberCoupon(factMemberCoupon.getMemberId(), analysisDate, factMemberCoupon);
                    break;

                case SHOP_VIEW:
                    FactMemberShopVisits factMemberShopVisits = MgFactMemberUnion.buildFactMemberShopVisits(value);
                    if (factMemberShopVisits.getMemberId() != null) {
                        factMemberShopVisits(factMemberShopVisits.getMemberId(), factMemberShopVisits);
                    }
                    break;
                case MEMBER_UNION:
                    immediately = true;
                    //??????member_base????????????
                    OdsMemberUnion odsMemberUnion = MgFactMemberUnion.buildFactMemberBase(value);
                    FactMemberUnion fm = factMemberBaseState.get(odsMemberUnion.getId());
                    fm = FactMemberUnion.merge(fm, odsMemberUnion);
                    factMemberBaseState.put(fm.getId(), fm);
                    break;

                case MEMBER_TOTAL:
                    FactMemberTotal factMemberTotal = MgFactMemberUnion.buildFactMemberTotal(value);
                    factMemberTotal(factMemberTotal.getId(), factMemberTotal);
                    break;

                case MEMBER_TAG:
                    OdsMemberTag memberTag = MgFactMemberUnion.buildMemberTagFrom(value);
                    memberTag(memberTag);
                    break;

                case SHOP_MEMBER_TAG_DEFINITION:
                    dealAll = true;
                    OdsTagDefinition tagDefinition = MgFactMemberUnion.buildOdsTagDefinition(value);
                    if (odsTagDefinitionState.contains(tagDefinition.getId())) {
                        if (tagDefinition.is__deleted()) {
                            odsTagDefinitionState.remove(tagDefinition.getId());
                            odsTagDefinitionMemberIds.remove(tagDefinition.getId());
                            return;
                        }

                        OdsTagDefinition old = odsTagDefinitionState.get(tagDefinition.getId());
                        if (!tagDefinition.getVersion().equals(old.getVersion())) {//????????????????????????????????????????????????????????????
                            odsTagDefinitionState.put(tagDefinition.getId(), tagDefinition);
                            // ????????????????????????????????????????????????MemberID??????
                            dealGroupsOrTag(shopId, MemberGroupStreamResultDto.SourceType.SMART_TAG, tagDefinition.getId(), tagDefinition.getFilters(), streamResultDto, dealAll, now);
                        }
                    } else {
                        odsTagDefinitionState.put(tagDefinition.getId(), tagDefinition);
                        //T????????????????????????????????????????????????MemberID??????
                        dealGroupsOrTag(shopId, MemberGroupStreamResultDto.SourceType.SMART_TAG, tagDefinition.getId(), tagDefinition.getFilters(), streamResultDto, dealAll, now);
                    }
                    break;
                case SHOP_MEMBER_GROUP:
                    dealAll = true;
                    OdsMemberGroup memberGroup = MgFactMemberUnion.buildOdsMemberGroup(value);
                    if (odsMemberGroupState.contains(memberGroup.getId())) {
                        if (memberGroup.is__deleted()) {
                            odsMemberGroupState.remove(memberGroup.getId());
                            odsMemberGroupMemberIds.remove(memberGroup.getId());
                            return;
                        }

                        OdsMemberGroup old = odsMemberGroupState.get(memberGroup.getId());
                        if (!memberGroup.getVersion().equals(old.getVersion())) {//????????????????????????????????????????????????????????????
                            odsMemberGroupState.put(memberGroup.getId(), memberGroup);
                            //????????????????????????????????????????????????MemberID??????
                            dealGroupsOrTag(shopId, MemberGroupStreamResultDto.SourceType.GROUP, memberGroup.getId(), memberGroup.getFilters(), streamResultDto, dealAll, now);
                        }
                    } else {
                        odsMemberGroupState.put(memberGroup.getId(), memberGroup);
                        //????????????????????????????????????????????????MemberID??????
                        dealGroupsOrTag(shopId, MemberGroupStreamResultDto.SourceType.GROUP, memberGroup.getId(), memberGroup.getFilters(), streamResultDto, dealAll, now);
                    }
                    break;
            }


            //?????????????????????????????? ????????????
            if (!dealAll) {

                //               if (immediately) {
                //                    dealAll = true;
                //                    streamResultDto.setShopId(ctx.getCurrentKey().f0);
                //                    for (OdsMemberGroup memberGroup : odsMemberGroupState.values()) {
                //                        dealGroupsOrTag(streamResultDto.getShopId(), MemberGroupStreamResultDto.SourceType.GROUP, memberGroup.getId(), memberGroup.getFilters(), streamResultDto, dealAll, now);
                //                        if (streamResultDto.isUpdate()) {
                //                            out.collect(streamResultDto);
                //                        }
                //                    }

                //                    for (OdsTagDefinition tagDefinition : odsTagDefinitionState.values()) {
                //                        dealGroupsOrTag(streamResultDto.getShopId(), MemberGroupStreamResultDto.SourceType.SMART_TAG, tagDefinition.getId(), tagDefinition.getFilters(), streamResultDto, dealAll, now);
                //                        if (streamResultDto.isUpdate()) {
                //                            out.collect(streamResultDto);
                //                        }
                //                    }
                //                }else {
                if (timeState.value() == null || !timeState.value()) {
                    //???????????????
                    ctx.timerService().registerProcessingTimeTimer(System.currentTimeMillis() + 2 * 60 * 1000);
                    timeState.update(true);
                }
                //               }

            } else {
                if (streamResultDto.isUpdate()) {
                    out.collect(streamResultDto);
                }
            }


        }


        /**
         * ?????????????????????????????????????????????
         *
         * @param sourceType
         * @param targetId
         * @param filters
         * @param streamResultDto
         * @throws Exception
         */
        private void dealGroupsOrTag(Integer shopId, MemberGroupStreamResultDto.SourceType sourceType, Integer targetId, String filters,
                                     MemberGroupStreamResultDto streamResultDto, final boolean dealAll, LocalDate localDate) throws Exception {
            streamResultDto.setUpdate(false);
            List<Integer> stateMemberIds;
            List<Integer> dataMembers = new ArrayList<>();
            //??????????????????
            boolean update;
            if (sourceType.equals(MemberGroupStreamResultDto.SourceType.GROUP)) {
                stateMemberIds = odsMemberGroupMemberIds.get(targetId);
            } else {
                stateMemberIds = odsTagDefinitionMemberIds.get(targetId);
            }
            if (stateMemberIds == null) {
                stateMemberIds = new ArrayList<>();
            }
            List<Integer> calList = new ArrayList<>();
            String dateDay = localDate.toString();
            calList.addAll(stateMemberIds);


            //???????????????????????????
            String memberGroupOpenCalculate = serverConfig.getMemberGroupOpenCalculate();
            if (memberGroupOpenCalculate.equals("true")) {
                boolean contains = todayMemberIds.contains(dateDay);
                if (contains && !dealAll) {
                    /************????????????/?????????????????????????????????????????????????????????????????????????????????????????????=???????????????????????????+?????????????????????????????????????????? ??????????????????????????????????????????***********/
                    List<Integer> todayNews = todayMemberIds.get(dateDay);
                    //?????????
                    calList.addAll(todayNews);
                    //?????????
                    calList = calList.parallelStream().distinct().collect(Collectors.toList());
                }
                //??????????????????
                dataMembers = getMemberIdsByFilters(shopId, filters, localDate, calList, dealAll);
            }

            /*******************?????????????????? ??????????????? ????????????????????????????????? ???????????????*************************/
            //            List<Integer> adds = new ArrayList<>();
            //            List<Integer> deletes = new ArrayList<>();
            //            if (CollectionUtil.isNullOrEmpty(dataMembers)) {
            //                //??????????????????????????????
            //                deletes.addAll(stateMemberIds);
            //                //??????????????????
            //            } else {
            //                //??????????????? ?????????????????????????????????????????????
            //                List<Integer> finalDataMembers = dataMembers;
            //                deletes = stateMemberIds.stream().filter(a -> !finalDataMembers.contains(a)).collect(Collectors.toList());
            //                //?????????????????????????????????????????????????????????
            //                List<Integer> finalStateMemberIds = stateMemberIds;
            //                adds = dataMembers.stream().filter(a -> !finalStateMemberIds.contains(a)).collect(Collectors.toList());
            //            }
            //            streamResultDto.setAdds(adds);
            //            streamResultDto.setDeletes(deletes);
            /*****************???????????? ??????????????? ????????????????????????????????? ???????????????***************************/


            //????????????????????????
            update = checkDiffrent(stateMemberIds, dataMembers);
            if (dealAll) {//???????????????????????????????????????
                update = dealAll;
            }

            if (sourceType.equals(MemberGroupStreamResultDto.SourceType.GROUP)) {
                odsMemberGroupMemberIds.put(targetId, dataMembers);
            } else {
                odsTagDefinitionMemberIds.put(targetId, dataMembers);
            }

            streamResultDto.setAnalysis(dataMembers);
            streamResultDto.setSourceType(sourceType);
            streamResultDto.setTargetId(targetId);
            streamResultDto.setUpdate(update);
        }

        /**
         * ??????list?????????sort????????????????????????????????????toString?????????????????????????????????
         * ??????6
         */
        private static boolean checkDiffrent(List<Integer> one, List<Integer> two) {
            Collections.sort(one);
            Collections.sort(two);
            return !one.toString().equals(two.toString());
        }


        private void memberTag(OdsMemberTag memberTag) throws Exception {
            Integer memberId = memberTag.getMemberId();
            //????????????
            if (factMemberBaseState.contains(memberId)) {
                FactMemberUnion memberBase = factMemberBaseState.get(memberId);
                List<Integer> tagIds = memberBase.getTagIds();
                tagIds = (tagIds == null ? new ArrayList<>() : tagIds);
                if (memberTag.is__deleted() && tagIds.contains(tagIds)) {
                    tagIds.remove(memberTag.getTagId());
                } else if (!memberTag.is__deleted() && !tagIds.contains(memberTag.getTagId())) {
                    tagIds.add(memberTag.getTagId());
                }
                memberBase.setTagIds(tagIds);
                factMemberBaseState.put(memberId, memberBase);
            } else {
                if (!memberTag.is__deleted()) {
                    FactMemberUnion factMemberUnion = new FactMemberUnion();
                    factMemberUnion.setId(memberTag.getMemberId());
                    List<Integer> tagIds = new ArrayList<>();
                    tagIds.add(memberTag.getTagId());
                    factMemberUnion.setTagIds(tagIds);
                    factMemberBaseState.put(memberId, factMemberUnion);
                }
            }
        }

        private void factMemberTotal(Integer memberId, FactMemberTotal factMemberTotal) throws Exception {
            FactMemberUnion factMemberUnion = factMemberBaseState.get(memberId);
            if (factMemberUnion == null) {
                factMemberUnion = FactMemberUnion.buildFactMemberBase(factMemberTotal);
            } else {
                factMemberUnion.setShopId(factMemberTotal.getShopId());
                factMemberUnion.setId(factMemberTotal.getId());
                factMemberUnion.setOrderCount(factMemberTotal.getOrderCount());
                factMemberUnion.setTotalOrderAmount(factMemberTotal.getTotalOrderAmount());
                factMemberUnion.setRefundAmount(factMemberTotal.getRefundAmount());
                factMemberUnion.setRefundCount(factMemberTotal.getRefundCount());
                factMemberUnion.setLastOrderTime(factMemberTotal.getLastOrderTime());
                factMemberUnion.setFirstOrderTime(factMemberTotal.getFirstOrderTime());
                factMemberUnion.setAvgConsumeAmount(factMemberTotal.getAvgConsumeAmount());
                factMemberUnion.setPurchaseRate(factMemberTotal.getPurchaseRate());
            }
            //??????state
            factMemberBaseState.put(memberId, factMemberUnion);
        }

        private void factMemberShopVisits(Integer memberId, FactMemberShopVisits factMemberShopVisits) throws Exception {
            if (factMemberShopVisitsState.contains(memberId)) {
                List<FactMemberShopVisits> shopVisits = factMemberShopVisitsState.get(memberId);
                //??????????????????
                Optional<FactMemberShopVisits> optional = shopVisits.stream().filter(a -> a.getUuid().equals(factMemberShopVisits.getUuid())).findFirst();
                if (optional.isPresent()) {
                    FactMemberShopVisits memberShopVisits = optional.get();
                    memberShopVisits.setVisitsEnd(factMemberShopVisits.getVisitsEnd());
                    memberShopVisits.setVisitsDuration(factMemberShopVisits.getVisitsDuration());
                } else {
                    shopVisits.add(factMemberShopVisits);
                }
                factMemberShopVisitsState.put(memberId, shopVisits);
            } else {
                List<FactMemberShopVisits> shopVisits = new ArrayList<>();
                shopVisits.add(factMemberShopVisits);
                factMemberShopVisitsState.put(memberId, shopVisits);
            }

            OdsMemberUnion odsMemberUnion = MgFactMemberUnion.buildFactMemberBaseVistiTime(factMemberShopVisits);
            FactMemberUnion fm = factMemberBaseState.get(odsMemberUnion.getId());
            fm = FactMemberUnion.merge(fm, odsMemberUnion);
            factMemberBaseState.put(fm.getId(), fm);

        }


        private void factMemberOrder(Integer memberId, String analysisDate, FactMemberOrder memberOrder) throws Exception {
            if (factMemberOrderState.contains(memberId)) {
                Map<String, FactMemberOrder> dailyMap = factMemberOrderState.get(memberId);
                dailyMap.put(analysisDate, memberOrder);
                factMemberOrderState.put(memberId, dailyMap);
            } else {
                Map<String, FactMemberOrder> dailyMap = new HashMap<>();
                dailyMap.put(analysisDate, memberOrder);
                factMemberOrderState.put(memberId, dailyMap);
            }

        }


        private void factMemberCouponLog(Integer memberId, String analysisDate, FactMemberCouponLog factMemberCouponLog) throws Exception {
            if (factMemberCouponLogState.contains(memberId)) {
                Map<String, Map<Integer, FactMemberCouponLog>> dailyMap = factMemberCouponLogState.get(memberId);
                Map<Integer, FactMemberCouponLog> targetMap;
                //?????????????????????????????????
                if (dailyMap.containsKey(analysisDate)) {
                    targetMap = dailyMap.get(analysisDate);
                } else {
                    //?????????????????????????????????????????? value
                    targetMap = new HashMap<>();
                }
                //???????????? ???????????????????????????????????????????????????????????????
                targetMap.put(factMemberCouponLog.getCouponTemplateId(), factMemberCouponLog);

                //????????????????????????
                dailyMap.put(analysisDate, targetMap);
                //???????????????????????????
                factMemberCouponLogState.put(memberId, dailyMap);

            } else {
                Map<String, Map<Integer, FactMemberCouponLog>> dailyMap = new HashMap<>();
                Map<Integer, FactMemberCouponLog> targetMap = new HashMap<>();
                //???????????? ???????????????????????????????????????????????????????????????
                targetMap.put(factMemberCouponLog.getCouponTemplateId(), factMemberCouponLog);
                //????????????????????????
                dailyMap.put(analysisDate, targetMap);
                //???????????????????????????
                factMemberCouponLogState.put(memberId, dailyMap);
            }
        }

        private void factMemberCoupon(Integer memberId, String analysisDate, FactMemberCoupon factMemberCoupon) throws Exception {
            if (factMemberCouponState.contains(memberId)) {
                Map<String, Map<Integer, FactMemberCoupon>> dailyMap = factMemberCouponState.get(memberId);
                Map<Integer, FactMemberCoupon> targetMap;
                //?????????????????????????????????
                if (dailyMap.containsKey(analysisDate)) {
                    targetMap = dailyMap.get(analysisDate);
                } else {
                    //?????????????????????????????????????????? value
                    targetMap = new HashMap<>();
                }
                //???????????? ???????????????????????????????????????????????????????????????
                targetMap.put(factMemberCoupon.getCouponTemplateId(), factMemberCoupon);

                //????????????????????????
                dailyMap.put(analysisDate, targetMap);
                //???????????????????????????
                factMemberCouponState.put(memberId, dailyMap);

            } else {
                Map<String, Map<Integer, FactMemberCoupon>> dailyMap = new HashMap<>();
                Map<Integer, FactMemberCoupon> targetMap = new HashMap<>();
                //???????????? ???????????????????????????????????????????????????????????????
                targetMap.put(factMemberCoupon.getCouponTemplateId(), factMemberCoupon);
                //????????????????????????
                dailyMap.put(analysisDate, targetMap);
                //???????????????????????????
                factMemberCouponState.put(memberId, dailyMap);
            }
        }

        private void factMemberProductLog(Integer memberId, String analysisDate, FactMemberProductLog factMemberProductLog) throws Exception {
            if (factMemberProductLogState.contains(memberId)) {
                Map<String, Map<Integer, FactMemberProductLog>> dailyMap = factMemberProductLogState.get(memberId);
                Map<Integer, FactMemberProductLog> factMemberProductLogMap;
                //?????????????????????????????????
                if (dailyMap.containsKey(analysisDate)) {
                    factMemberProductLogMap = dailyMap.get(analysisDate);
                } else {
                    //?????????????????????????????????????????? value
                    factMemberProductLogMap = new HashMap<>();
                }
                //???????????? ???????????????????????????????????????????????????????????????
                factMemberProductLogMap.put(factMemberProductLog.getProductId(), factMemberProductLog);

                //????????????????????????
                dailyMap.put(analysisDate, factMemberProductLogMap);
                //???????????????????????????
                factMemberProductLogState.put(memberId, dailyMap);

            } else {
                Map<String, Map<Integer, FactMemberProductLog>> dailyMap = new HashMap<>();
                Map<Integer, FactMemberProductLog> factMemberProductLogMap = new HashMap<>();
                //???????????? ???????????????????????????????????????????????????????????????
                factMemberProductLogMap.put(factMemberProductLog.getProductId(), factMemberProductLog);
                //????????????????????????
                dailyMap.put(analysisDate, factMemberProductLogMap);
                //???????????????????????????
                factMemberProductLogState.put(memberId, dailyMap);
            }
        }

        private void factMemberProductOrder(Integer memberId, String analysisDate, FactMemberProductOrder factMemberProductOrder) throws Exception {
            if (factMemberProductOrderState.contains(memberId)) {
                Map<String, Map<Integer, FactMemberProductOrder>> dailyMap = factMemberProductOrderState.get(memberId);
                Map<Integer, FactMemberProductOrder> targetMap;
                //?????????????????????????????????
                if (dailyMap.containsKey(analysisDate)) {
                    targetMap = dailyMap.get(analysisDate);
                } else {
                    //?????????????????????????????????????????? value
                    targetMap = new HashMap<>();
                }
                //???????????? ???????????????????????????????????????????????????????????????
                targetMap.put(factMemberProductOrder.getProductId(), factMemberProductOrder);

                //????????????????????????
                dailyMap.put(analysisDate, targetMap);
                //???????????????????????????
                factMemberProductOrderState.put(memberId, dailyMap);

            } else {
                Map<String, Map<Integer, FactMemberProductOrder>> dailyMap = new HashMap<>();
                Map<Integer, FactMemberProductOrder> targetMap = new HashMap<>();
                //???????????? ???????????????????????????????????????????????????????????????
                targetMap.put(factMemberProductOrder.getProductId(), factMemberProductOrder);
                //????????????????????????
                dailyMap.put(analysisDate, targetMap);
                //???????????????????????????
                factMemberProductOrderState.put(memberId, dailyMap);
            }
        }

        private void factMemberProductRefund(Integer memberId, String analysisDate, FactMemberProductRefund factMemberProductRefund) throws Exception {
            if (factMemberProductRefundState.contains(memberId)) {
                Map<String, Map<Integer, FactMemberProductRefund>> dailyMap = factMemberProductRefundState.get(memberId);
                Map<Integer, FactMemberProductRefund> targetMap;
                //?????????????????????????????????
                if (dailyMap.containsKey(analysisDate)) {
                    targetMap = dailyMap.get(analysisDate);
                } else {
                    //?????????????????????????????????????????? value
                    targetMap = new HashMap<>();
                }
                //???????????? ???????????????????????????????????????????????????????????????
                targetMap.put(factMemberProductRefund.getProductId(), factMemberProductRefund);

                //????????????????????????
                dailyMap.put(analysisDate, targetMap);
                //???????????????????????????
                factMemberProductRefundState.put(memberId, dailyMap);

            } else {
                Map<String, Map<Integer, FactMemberProductRefund>> dailyMap = new HashMap<>();
                Map<Integer, FactMemberProductRefund> targetMap = new HashMap<>();
                //???????????? ???????????????????????????????????????????????????????????????
                targetMap.put(factMemberProductRefund.getProductId(), factMemberProductRefund);
                //????????????????????????
                dailyMap.put(analysisDate, targetMap);
                //???????????????????????????
                factMemberProductRefundState.put(memberId, dailyMap);
            }
        }


        /**
         * ???????????????????????????????????????memberIds
         *
         * @param shopId
         * @param filters
         * @return
         */
        private List<Integer> getMemberIdsByFilters(Integer shopId, String filters, LocalDate beganTime, List<Integer> calList, final boolean dealAll) {
            //??????id???state????????????

            FilterChain filterChain = FilterChain.fromCache(filters, cache);

            if (filterChain == null) {
                return Collections.emptyList();
            }


            Supplier<Stream<FactMemberUnion>> factMemberUnionSupplier = () -> {
                try {
                    //??????????????????????????????
                    return StreamSupport.stream(factMemberBaseState.values().spliterator(), true)
                        .filter(a -> a.getShopId() != null && a.getShopId().equals(shopId)).filter(a -> a.getDisabled() == null || a.getDisabled() == 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            };

            Supplier<Stream<FactMemberCoupon>> memberCouponStreamSupplier = () -> {
                try {

                    return StreamSupport.stream(factMemberCouponState.entries().spliterator(), true).filter(entry -> {
                            if (dealAll || calList.contains(entry.getKey())) {
                                return true;
                            }
                            return false;
                        }).map(Map.Entry::getValue)
                        .flatMap(one -> one.values().parallelStream())
                        .flatMap(two -> two.values().parallelStream()).filter(a -> a.getShopId() != null && a.getShopId().equals(shopId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            };

            Supplier<Stream<FactMemberCouponLog>> memberCouponLogStreamSupplier = () -> {
                try {
                    return StreamSupport.stream(factMemberCouponLogState.entries().spliterator(), true).filter(entry -> {
                            if (dealAll || calList.contains(entry.getKey())) {
                                return true;
                            }
                            return false;
                        }).map(Map.Entry::getValue)
                        .flatMap(one -> one.values().parallelStream())
                        .flatMap(two -> two.values().parallelStream()).filter(a -> a.getShopId() != null && a.getShopId().equals(shopId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            };

            Supplier<Stream<FactMemberProductOrder>> factMemberProductOrderSupplier = () -> {
                try {
                    return StreamSupport.stream(factMemberProductOrderState.entries().spliterator(), true).filter(entry -> {
                            if (dealAll || calList.contains(entry.getKey())) {
                                return true;
                            }
                            return false;
                        }).map(Map.Entry::getValue)
                        .flatMap(one -> one.values().parallelStream())
                        .flatMap(two -> two.values().parallelStream()).filter(a -> a.getShopId() != null && a.getShopId().equals(shopId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            };

            Supplier<Stream<FactMemberProductRefund>> factMemberProductRefundSupplier = () -> {
                try {
                    return StreamSupport.stream(factMemberProductRefundState.entries().spliterator(), true).filter(entry -> {
                            if (dealAll || calList.contains(entry.getKey())) {
                                return true;
                            }
                            return false;
                        }).map(Map.Entry::getValue)
                        .flatMap(one -> one.values().parallelStream())
                        .flatMap(two -> two.values().parallelStream()).filter(a -> a.getShopId() == shopId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            };


            Supplier<Stream<FactMemberProductLog>> factMemberProductLogSupplier = () -> {
                try {
                    return StreamSupport.stream(factMemberProductLogState.entries().spliterator(), true).filter(entry -> {
                            if (dealAll || calList.contains(entry.getKey())) {
                                return true;
                            }
                            return false;
                        }).map(Map.Entry::getValue)
                        .flatMap(one -> one.values().parallelStream())
                        .flatMap(two -> two.values().parallelStream()).filter(a -> a.getShopId() != null && a.getShopId().equals(shopId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            };


            Supplier<Stream<FactMemberOrder>> factMemberOrderSupplier = () -> {
                try {
                    return StreamSupport.stream(factMemberOrderState.entries().spliterator(), true).filter(entry -> {
                            if (dealAll || calList.contains(entry.getKey())) {
                                return true;
                            }
                            return false;
                        }).map(Map.Entry::getValue)
                        .flatMap(one -> one.values().parallelStream()).filter(a -> a.getShopId() != null && a.getShopId().equals(shopId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            };

            Supplier<Stream<FactMemberShopVisits>> factMemberShopVisitsSupplier = () -> {
                try {
                    return StreamSupport.stream(factMemberShopVisitsState.entries().spliterator(), true).filter(entry -> {
                        if (dealAll || calList.contains(entry.getKey())) {
                            return true;
                        }
                        return false;
                    }).flatMap(a -> a.getValue().parallelStream()).filter(a -> a.getShopId() != null && a.getShopId().equals(shopId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            };


            List<FilterResult> resultList = new ArrayList<>();
            List<Integer> memberIds = new ArrayList<>();


            FilterResult memberBases = filterChain.filterResult(FactMemberUnion.class, factMemberUnionSupplier, beganTime, null);
            if (memberBases.isUsed() && CollectionUtil.isNullOrEmpty(memberBases.getMemberIds())) {
                return memberIds;
            }

            FilterResult memberCoupons = filterChain.filterResult(FactMemberCoupon.class, memberCouponStreamSupplier, beganTime, null);
            if (memberCoupons.isUsed() && CollectionUtil.isNullOrEmpty(memberCoupons.getMemberIds())) {
                return memberIds;
            }

            FilterResult memberProductLogs = filterChain.filterResult(FactMemberProductLog.class, factMemberProductLogSupplier, beganTime, null);
            if (memberProductLogs.isUsed() && CollectionUtil.isNullOrEmpty(memberProductLogs.getMemberIds())) {
                return memberIds;
            }

            FilterResult memberCouponLogs = filterChain.filterResult(FactMemberCouponLog.class, memberCouponLogStreamSupplier, beganTime, null);
            if (memberCouponLogs.isUsed() && CollectionUtil.isNullOrEmpty(memberCouponLogs.getMemberIds())) {
                return memberIds;
            }

            FilterResult memberProductOrders = filterChain.filterResult(FactMemberProductOrder.class, factMemberProductOrderSupplier, beganTime, factMemberUnionSupplier);
            if (memberProductOrders.isUsed() && CollectionUtil.isNullOrEmpty(memberProductOrders.getMemberIds())) {
                return memberIds;
            }

            FilterResult memberOrders = filterChain.filterResult(FactMemberOrder.class, factMemberOrderSupplier, beganTime, factMemberUnionSupplier);
            if (memberOrders.isUsed() && CollectionUtil.isNullOrEmpty(memberOrders.getMemberIds())) {
                return memberIds;
            }

            FilterResult memberProductRefunds = filterChain.filterResult(FactMemberProductRefund.class, factMemberProductRefundSupplier, beganTime, null);
            if (memberProductRefunds.isUsed() && CollectionUtil.isNullOrEmpty(memberProductRefunds.getMemberIds())) {
                return memberIds;
            }

            FilterResult memberShopVisitss = filterChain.filterResult(FactMemberShopVisits.class, factMemberShopVisitsSupplier, beganTime, null);
            if (memberShopVisitss.isUsed() && CollectionUtil.isNullOrEmpty(memberShopVisitss.getMemberIds())) {
                return memberIds;
            }


            resultList.add(memberBases);
            resultList.add(memberCoupons);
            resultList.add(memberCouponLogs);
            resultList.add(memberProductOrders);
            resultList.add(memberProductRefunds);
            resultList.add(memberOrders);
            resultList.add(memberProductLogs);
            resultList.add(memberShopVisitss);
            //??????????????????????????????
            List<FilterResult> results = resultList.stream().filter(FilterResult::isUsed).collect(Collectors.toList());

            for (FilterResult result : results) {
                if (CollectionUtil.isNullOrEmpty(result.getMemberIds())) {
                    memberIds.clear();
                    return memberIds;
                }
                if (CollectionUtil.isNullOrEmpty(memberIds)) {
                    memberIds.addAll(result.getMemberIds());
                } else {
                    //?????????
                    memberIds = memberIds.parallelStream().filter(a -> result.getMemberIds().contains(a)).collect(Collectors.toList());
                }
            }


            if (memberIds.contains(0)) {
                memberIds.remove((Integer) 0);
            }

            List<Integer> finalMemberIds = memberIds;

            return factMemberUnionSupplier.get()
                .map(FactMemberUnion::getId)
                .filter(finalMemberIds::contains)
                .collect(Collectors.toList());
        }
    }
}
