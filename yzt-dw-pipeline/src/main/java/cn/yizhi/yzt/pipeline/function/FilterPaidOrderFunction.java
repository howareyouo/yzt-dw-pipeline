package cn.yizhi.yzt.pipeline.function;

import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;

/**
 * @author hucheng
 * 过滤订单的重复数据，防止支付订单重复计算
 * @date 2020/10/20 14:07
 */
public class FilterPaidOrderFunction extends RichFilterFunction<OdsEmallOrder> {

    // 只在KeyedStream上使用时才可以访问
    private transient MapState<Long, Boolean> paidOrderState;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        // Tuple2记录了查找起始点和上次的订单总金额
        MapStateDescriptor<Long, Boolean> descriptor =
                new MapStateDescriptor<>(
                        // the state name
                        "paid-order-map",
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


        paidOrderState = getRuntimeContext().getMapState(descriptor);
    }

    @Override
    public boolean filter(OdsEmallOrder value) throws Exception {
        if (paidOrderState.contains(value.getId())) {
            return false;
        }

        boolean foundNewPaidOrder = false;


        //只要支付过就算支付订单数据
        if (value.getTransactionNo() != null) {
            foundNewPaidOrder = true;
        }

        if (foundNewPaidOrder) {
            paidOrderState.put(value.getId(), true);
        }

        return foundNewPaidOrder;
    }
}
