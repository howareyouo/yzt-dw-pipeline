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
 * @date 2020/11/3 17:51
 */
public class FilterRefundFunction extends RichFilterFunction<OdsEmallOrder> {
    // 只在KeyedStream上使用时才可以访问
    private transient MapState<Long, Boolean> refundState;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        // Tuple2记录了查找起始点和上次的订单总金额
        MapStateDescriptor<Long, Boolean> descriptor =
                new MapStateDescriptor<>(
                        // the state name
                        "refund-order-map",
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


        refundState = getRuntimeContext().getMapState(descriptor);
    }

    @Override
    public boolean filter(OdsEmallOrder value) throws Exception {
        if (refundState.contains(value.getId())) {
            return false;
        }

        boolean foundNewRefundOrder = false;


        //9 表示退款成功
        if (value.getStatus() != null && value.getStatus() == 9) {
            foundNewRefundOrder = true;
        }

        if (foundNewRefundOrder) {
            refundState.put(value.getId(), true);
        }

        return foundNewRefundOrder;
    }
}
