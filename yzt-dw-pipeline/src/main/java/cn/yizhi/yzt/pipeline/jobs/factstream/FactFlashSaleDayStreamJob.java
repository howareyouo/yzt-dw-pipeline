package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderItem;
import cn.yizhi.yzt.pipeline.model.fact.flashsale.FactFlashSaleEveryDay;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;

/**
 * @Author: HuCheng
 * 秒杀每天的计算指标
 * @Date: 2020/11/30 15:36
 */
public class FactFlashSaleDayStreamJob extends StreamJob {
    //3天清除
    private static final StateTtlConfig ttlConfig = StateTtlConfig
            .newBuilder(org.apache.flink.api.common.time.Time.days(3))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

    @Override
    public void defineJob() throws Exception {
        DataStream<FactOrderItem> factOrderItemDs = this.createStreamFromKafka(SourceTopics.TOPIC_FACT_ORDER_ITEM, FactOrderItem.class);

        //秒杀order_type = 3
        DataStream<FactFlashSaleEveryDay> factFlashSaleEveryDayDs = factOrderItemDs
                .filter(new FilterFunction<FactOrderItem>() {
                    @Override
                    public boolean filter(FactOrderItem value) throws Exception {
                        return value.getStatus() == 1 && value.getOrderType() != null && value.getOrderType() == 3;
                    }
                })
                .keyBy(new KeySelector<FactOrderItem, Tuple3<String, Integer, Integer>>() {
                    @Override
                    public Tuple3<String, Integer, Integer> getKey(FactOrderItem value) throws Exception {
                        return new Tuple3<>(TimeUtil.convertTimeStampToDay(value.getUpdatedAt()), value.getShopId(), value.getOrderType());
                    }
                })
                .process(new FactFlashSaleProcessFunction())
                .uid("flash-sale-day-process")
                .name("flash-sale-day-process");

        //写入mysql
        toJdbcUpsertSink(factFlashSaleEveryDayDs, Tables.SINK_TABLE_FLASH_ACTIVITY_DAY, FactFlashSaleEveryDay.class);
    }

    public static class FactFlashSaleProcessFunction extends KeyedProcessFunction<Tuple3<String, Integer, Integer>, FactOrderItem, FactFlashSaleEveryDay> {
        private transient ValueState<Integer> orderCountState;
        private transient ValueState<BigDecimal> orderAmountState;
        private transient ValueState<BigDecimal> discountAmountState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            ValueStateDescriptor<Integer> orderCountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderCountState",
                    // type information
                    Types.INT);
            orderCountStateDescriptor.enableTimeToLive(ttlConfig);
            orderCountState = getRuntimeContext().getState(orderCountStateDescriptor);

            ValueStateDescriptor<BigDecimal> orderAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "orderAmountState",
                    // type information
                    Types.BIG_DEC);
            orderAmountStateDescriptor.enableTimeToLive(ttlConfig);
            orderAmountState = getRuntimeContext().getState(orderAmountStateDescriptor);

            ValueStateDescriptor<BigDecimal> discountAmountStateDescriptor = new ValueStateDescriptor<>(
                    // the state name
                    "discountAmountState",
                    // type information
                    Types.BIG_DEC);
            discountAmountStateDescriptor.enableTimeToLive(ttlConfig);
            discountAmountState = getRuntimeContext().getState(discountAmountStateDescriptor);
        }

        @Override
        public void processElement(FactOrderItem value, Context ctx, Collector<FactFlashSaleEveryDay> out) throws Exception {
            long oneTime = value.getUpdatedAt().getTime();
            long watermark = ctx.timerService().currentWatermark() + 1L;

            //如果数据时间迟到24小时，那么直接忽略不计算，比如3号的数据，在5号以后才到，那么直接忽略该数据
            long judge = oneTime + 86400000;
            if (judge > watermark) {
                Integer orderCount = orderCountState.value();
                BigDecimal orderAmount = orderAmountState.value();
                BigDecimal discountAmount = discountAmountState.value();

                if (orderCount == null || orderCount == 0) {
                    orderCountState.update(1);
                } else {
                    orderCountState.update(orderCount + 1);
                }

                if (orderAmount == null) {
                    orderAmountState.update(value.getActualAmount());
                } else {
                    orderAmountState.update(orderAmount.add(value.getActualAmount()));
                }

                if (discountAmount == null) {
                    discountAmountState.update(value.getProductDiscountAmount());
                } else {
                    discountAmountState.update(discountAmount.add(value.getProductDiscountAmount()));
                }

                Tuple3<String, Integer, Integer> currentKey = ctx.getCurrentKey();
                FactFlashSaleEveryDay factFlashSaleEveryDay = new FactFlashSaleEveryDay();
                factFlashSaleEveryDay.setRowDate(currentKey.f0);
                factFlashSaleEveryDay.setShopId(currentKey.f1);
                factFlashSaleEveryDay.setPromotionType(currentKey.f2);
                factFlashSaleEveryDay.setOrderCount(orderCountState.value());
                factFlashSaleEveryDay.setOrderAmount(orderAmountState.value());
                factFlashSaleEveryDay.setDiscountAmount(discountAmountState.value());

                out.collect(factFlashSaleEveryDay);
            }
        }
    }
}
