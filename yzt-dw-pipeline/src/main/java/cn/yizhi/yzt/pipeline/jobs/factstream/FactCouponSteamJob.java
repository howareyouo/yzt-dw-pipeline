package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.function.FilterPaidOrderFunction;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactCoupon;
import cn.yizhi.yzt.pipeline.model.fact.FactCouponJoinStream;
import cn.yizhi.yzt.pipeline.model.ods.*;
import cn.yizhi.yzt.pipeline.model.ods.CouponUseRecord;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple1;
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
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.apache.flink.api.common.eventtime.WatermarkStrategy.forBoundedOutOfOrderness;

/**
 * @author aorui created on 2020/10/22
 */
public class FactCouponSteamJob extends StreamJob {

    private static StateTtlConfig ttlConfig = StateTtlConfig
            //设置状态保留3天
            .newBuilder(org.apache.flink.api.common.time.Time.days(3))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {

        WatermarkStrategy<CouponUseRecord> strategy = WatermarkStrategy.forMonotonousTimestamps();

        //odsCouponUseRecord流数据
        DataStream<CouponUseRecord> useDs = this.createStreamFromKafka(SourceTopics.TOPIC_COUPON_USE_RECORD, CouponUseRecord.class)
            .assignTimestampsAndWatermarks(strategy);

        //OdsmallOrder流数据
        DataStream<OdsEmallOrder> orderDs = this.createStreamFromKafka(SourceTopics.TOPIC_EMALL_ORDER, OdsEmallOrder.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<OdsEmallOrder>() {

                    @Override
                    public long extractAscendingTimestamp(OdsEmallOrder element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                })
                .keyBy(new KeySelector<OdsEmallOrder, Tuple1<Integer>>() {
                    @Override
                    public Tuple1<Integer> getKey(OdsEmallOrder value) throws Exception {
                        return new Tuple1<>(value.getShopId());
                    }
                }).filter(new FilterPaidOrderFunction());

        //source为空的默认设置为web
        DataStream<OdsEmallOrder> orderEmallDs = orderDs.map(new MapFunction<OdsEmallOrder, OdsEmallOrder>() {
            @Override
            public OdsEmallOrder map(OdsEmallOrder value) throws Exception {
                if (value.getSource() == null) {
                    value.setSource("web");
                }
                return value;
            }
        });

        //分渠道计算factCoupon
        DataStream<FactCoupon> couponDs = useDs
                .keyBy(new KeySelector<CouponUseRecord, Long>() {

                    @Override
                    public Long getKey(CouponUseRecord value) throws Exception {
                        return value.getOrderId();
                    }
                })
                .intervalJoin(orderEmallDs.keyBy(new KeySelector<OdsEmallOrder, Long>() {
                    @Override
                    public Long getKey(OdsEmallOrder value) throws Exception {
                        return value.getId();
                    }
                }))
                .between(Time.minutes(-1), Time.minutes(1))
                .process(new ProcessJoinFunction<CouponUseRecord, OdsEmallOrder, FactCouponJoinStream>() {
                    @Override
                    public void processElement(CouponUseRecord left, OdsEmallOrder right, Context ctx, Collector<FactCouponJoinStream> out) throws Exception {
                        FactCouponJoinStream factCouponJoinStream = new FactCouponJoinStream();
                        factCouponJoinStream.setCouponUseRecord(left);
                        factCouponJoinStream.setOdsEmallOrder(right);
                        out.collect(factCouponJoinStream);
                    }
                })
                .keyBy(new KeySelector<FactCouponJoinStream, Tuple4<String, Integer, Integer, String>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, String> getKey(FactCouponJoinStream value) throws Exception {
                        return new Tuple4(convertTimeStamp(value.getOdsEmallOrder().getUpdatedAt()), value.getCouponUseRecord().getShopId(), value.getCouponUseRecord().getCouponTemplateId(), value.getOdsEmallOrder().getSource());
                    }
                })
                .process(new FactCouponResult())
                .uid("CouponUseRecord-OdsEmallOrder-process")
                .name("CouponUseRecord-OdsEmallOrder-process");

        //全渠道计算factCoupon
        DataStream<FactCoupon> couponAllDs = useDs
                .keyBy(new KeySelector<CouponUseRecord, Long>() {
                    @Override
                    public Long getKey(CouponUseRecord value) throws Exception {
                        return value.getOrderId();
                    }
                })
                .intervalJoin(orderEmallDs.map(new MapFunction<OdsEmallOrder, OdsEmallOrder>() {
                    @Override
                    public OdsEmallOrder map(OdsEmallOrder value) throws Exception {
                        value.setSource("all");
                        return value;
                    }
                }).keyBy(new KeySelector<OdsEmallOrder, Long>() {
                    @Override
                    public Long getKey(OdsEmallOrder value) throws Exception {
                        return value.getId();
                    }
                }))
                .between(Time.minutes(-1), Time.minutes(1))
                .process(new ProcessJoinFunction<CouponUseRecord, OdsEmallOrder, FactCouponJoinStream>() {
                    @Override
                    public void processElement(CouponUseRecord left, OdsEmallOrder right, Context ctx, Collector<FactCouponJoinStream> out) throws Exception {
                        FactCouponJoinStream factCouponJoinStream = new FactCouponJoinStream();
                        factCouponJoinStream.setCouponUseRecord(left);
                        factCouponJoinStream.setOdsEmallOrder(right);
                        out.collect(factCouponJoinStream);
                    }
                })
                .keyBy(new KeySelector<FactCouponJoinStream, Tuple4<String, Integer, Integer, String>>() {
                    @Override
                    public Tuple4<String, Integer, Integer, String> getKey(FactCouponJoinStream value) throws Exception {
                        return new Tuple4(convertTimeStamp(value.getOdsEmallOrder().getUpdatedAt()), value.getCouponUseRecord().getShopId(), value.getCouponUseRecord().getCouponTemplateId(), value.getOdsEmallOrder().getSource());
                    }
                })
                .process(new FactCouponResult())
                .uid("CouponUseRecord-OdsEmallOrder-all-process")
                .name("CouponUseRecord-OdsEmallOrder-all-process");

        //输出到数据库
        toJdbcUpsertSink(couponDs, Tables.SINK_TABLE_COUPON, FactCoupon.class);
        toJdbcUpsertSink(couponAllDs, Tables.SINK_TABLE_COUPON, FactCoupon.class);
    }

    public static class FactCouponResult extends KeyedProcessFunction<Tuple4<String, Integer, Integer, String>, FactCouponJoinStream, FactCoupon> {

        //支付总金额
        private transient ValueState<BigDecimal> paidAmountState;

        //优惠总金额
        private transient ValueState<BigDecimal> preferentialAmountState;

        //核销人数
        private transient ListState<Integer> writeOffNumberState;

        //商品销量
        private transient ValueState<Integer> saleCountState;

        //核销张数
        private transient ListState<Long> writeOffCountState;

        //总订单数
        private transient ListState<String> orderCountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<BigDecimal> paidAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "paidAmountState",
                    // type information
                    Types.BIG_DEC);
            paidAmountStateDescriptor.enableTimeToLive(ttlConfig);
            paidAmountState = getRuntimeContext().getState(paidAmountStateDescriptor);

            ValueStateDescriptor<BigDecimal> preferentialAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "preferentialAmountState",
                    // type information
                    Types.BIG_DEC);
            preferentialAmountStateDescriptor.enableTimeToLive(ttlConfig);
            preferentialAmountState = getRuntimeContext().getState(preferentialAmountStateDescriptor);

            ListStateDescriptor<Integer> writeOffNumberStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "writeOffNumberState",
                    // type information
                    Types.INT);
            writeOffNumberStateDescriptor.enableTimeToLive(ttlConfig);
            writeOffNumberState = getRuntimeContext().getListState(writeOffNumberStateDescriptor);

            ValueStateDescriptor<Integer> saleCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "saleCountState",
                    // type information
                    Types.INT);
            saleCountStateDescriptor.enableTimeToLive(ttlConfig);
            saleCountState = getRuntimeContext().getState(saleCountStateDescriptor);

            ListStateDescriptor<Long> writeOffCountStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "writeOffCountState",
                    // type information
                    Types.LONG);
            writeOffCountStateDescriptor.enableTimeToLive(ttlConfig);
            writeOffCountState = getRuntimeContext().getListState(writeOffCountStateDescriptor);

            ListStateDescriptor<String> orderCountStateDescriptor = new ListStateDescriptor<>(
                    // the state name
                    "orderCountState",
                    // type information
                    Types.STRING);
            orderCountStateDescriptor.enableTimeToLive(ttlConfig);
            orderCountState = getRuntimeContext().getListState(orderCountStateDescriptor);

        }

        @Override
        public void processElement(FactCouponJoinStream value, Context ctx, Collector<FactCoupon> out) throws Exception {
            //数据时间,乘以1000就是毫秒的。
            long oneTime = value.getOdsEmallOrder().getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算
            long judge = oneTime + 172800000;

            //未超时进行计算
            if (judge > watermark) {
                BigDecimal paidAmount = paidAmountState.value();
                BigDecimal preferentialAmount = preferentialAmountState.value();
                Integer productQuantity = saleCountState.value();
                if (paidAmount == null) {
                    paidAmountState.update(value.getOdsEmallOrder().getActualAmount());
                } else {
                    paidAmountState.update(paidAmount.add(value.getOdsEmallOrder().getActualAmount()));
                }

                if (preferentialAmount == null) {
                    preferentialAmountState.update(value.getCouponUseRecord().getDiscountAmount());
                } else {
                    preferentialAmountState.update(preferentialAmount.add(value.getCouponUseRecord().getDiscountAmount()));
                }

                if (productQuantity == null) {
                    saleCountState.update(value.getCouponUseRecord().getProductQuantity());
                } else {
                    saleCountState.update(productQuantity + value.getCouponUseRecord().getProductQuantity());
                }

                writeOffNumberState.add(value.getCouponUseRecord().getMemberId());
                writeOffCountState.add(value.getCouponUseRecord().getId());
                orderCountState.add(value.getCouponUseRecord().getOrderNo());
            }

            //去重统计
            Set<Integer> orderNumberCount = new HashSet<>();
            Set<Long> urIdCount = new HashSet<>();
            Set<String> orderNoCount = new HashSet<>();
            writeOffNumberState.get().forEach(e -> {
                orderNumberCount.add(e);
            });
            writeOffCountState.get().forEach(e -> {
                urIdCount.add(e);
            });
            orderCountState.get().forEach(e -> {
                orderNoCount.add(e);
            });

            //数据封装
            FactCoupon factCoupon = new FactCoupon();
            Tuple4<String, Integer, Integer, String> currentKey = ctx.getCurrentKey();
            factCoupon.setRowDate(currentKey.f0);
            factCoupon.setShopId(currentKey.f1);
            factCoupon.setCouponId(currentKey.f2);
            factCoupon.setChannel(currentKey.f3);
            factCoupon.setPayAmount(paidAmountState.value());
            factCoupon.setPreferentialAmount(preferentialAmountState.value());
            factCoupon.setWriteOffNumber(orderNumberCount.size());
            factCoupon.setWriteOffCount(urIdCount.size());
            factCoupon.setSaleCount(saleCountState.value());
            factCoupon.setOrderCount(orderNoCount.size());
            factCoupon.setOrderNumber(orderNumberCount.size());
            factCoupon.setNewMemberCount(0);
            factCoupon.setOldMemberCount(0);
            out.collect(factCoupon);
        }
    }


    private static String convertTimeStamp(Timestamp timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd").format(timestamp);
    }

}
