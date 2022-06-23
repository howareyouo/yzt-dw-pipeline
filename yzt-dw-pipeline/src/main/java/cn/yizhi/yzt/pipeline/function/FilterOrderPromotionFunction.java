package cn.yizhi.yzt.pipeline.function;

import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotion;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;

/**
 * @author hucheng
 * @date 2020/11/4 15:33
 */
public class FilterOrderPromotionFunction extends RichFilterFunction<FactOrderPromotion> {

    // 只在KeyedStream上使用时才可以访问
    // All state collection types support per-entry TTLs. This means that list elements and map entries expire independently.
    private transient MapState<Long, Boolean> orderState;

    @Override
    public boolean filter(FactOrderPromotion value) throws Exception {

        if (orderState.contains(value.getOrderId())) {
            return false;
        }

        orderState.put(value.getOrderId(), true);
        return true;
    }

    @Override
    public void open(Configuration conf) {
        // Tuple2记录了查找起始点和上次的订单总金额
        MapStateDescriptor<Long, Boolean> descriptor =
                new MapStateDescriptor<>(
                        // the state name
                        "order-map",
                        // type information
                        Types.LONG,
                        Types.BOOLEAN);

        StateTtlConfig ttlConfig = StateTtlConfig
                // 保留90天, 取决于订单生命周期的时间跨度
                .newBuilder(org.apache.flink.api.common.time.Time.days(90))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();

        descriptor.enableTimeToLive(ttlConfig);

        orderState = getRuntimeContext().getMapState(descriptor);

    }
}
