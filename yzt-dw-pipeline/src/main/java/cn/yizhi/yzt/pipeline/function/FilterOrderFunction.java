package cn.yizhi.yzt.pipeline.function;

import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;

/**
 * @author hucheng
 * 过滤下单数据，只要有订单产生就算下单
 * @date 2020/10/20 14:07
 */
public class FilterOrderFunction extends RichFilterFunction<OdsEmallOrder> {

    // 只在KeyedStream上使用时才可以访问
    private transient MapState<Long, Boolean> orderState;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        // Tuple2记录了查找起始点和上次的订单总金额
        MapStateDescriptor<Long, Boolean> descriptor = new MapStateDescriptor<>("order-map", Types.LONG, Types.BOOLEAN);

        StateTtlConfig ttlConfig = StateTtlConfig
            // 保留90天, 取决于订单生命周期的时间跨度
            .newBuilder(Time.days(90))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();

        descriptor.enableTimeToLive(ttlConfig);

        orderState = getRuntimeContext().getMapState(descriptor);
    }

    @Override
    public boolean filter(OdsEmallOrder value) throws Exception {
        if (orderState.contains(value.getId())) {
            return false;
        } else {
            orderState.put(value.getId(), true);
            return true;
        }
    }
}
