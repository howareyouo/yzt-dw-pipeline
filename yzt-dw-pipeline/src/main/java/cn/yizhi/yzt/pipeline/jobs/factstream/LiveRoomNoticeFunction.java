package cn.yizhi.yzt.pipeline.jobs.factstream;

import cn.yizhi.yzt.pipeline.model.fact.liveroom.FactLiveRoomOrder;
import cn.yizhi.yzt.pipeline.model.fact.liveroom.LiveRoomNotice;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LiveRoomNoticeFunction extends RichMapFunction<FactLiveRoomOrder, LiveRoomNotice> {
    private static Logger logger = LoggerFactory.getLogger(LiveRoomNoticeFunction.class);

    private static  List<BigDecimal> amountPool = init();

    private transient ValueState<Tuple2<Integer, BigDecimal>> liveRoomOrderAmount;

    @Override
    public LiveRoomNotice map(FactLiveRoomOrder order) throws Exception {
        Tuple2<Integer, BigDecimal> lastStateValue = liveRoomOrderAmount.value();

        if (lastStateValue == null) {
            lastStateValue = Tuple2.of(0, BigDecimal.ZERO);
        }

        logger.info("lastStateValue: {}", lastStateValue);

        Integer lastSearchIndex = lastStateValue.f0;
        BigDecimal lastAmount = lastStateValue.f1;

        BigDecimal currentAmount = order.getOrderAmount();

        BigDecimal toBePushed = BigDecimal.ZERO;

        for (int i=lastSearchIndex; i< amountPool.size() - 1; i++) {

            if (currentAmount.compareTo(amountPool.get(i)) >= 0 && currentAmount.compareTo(amountPool.get(i+1)) < 0) {
                toBePushed = amountPool.get(i);
                lastSearchIndex = i;
                break;
            }
        }

        if (toBePushed.compareTo(BigDecimal.ZERO) > 0 && lastAmount.compareTo(amountPool.get(lastSearchIndex)) != 0){
            LiveRoomNotice liveRoomNotice = new LiveRoomNotice();
            liveRoomNotice.setNoticeType(2);
            liveRoomNotice.setShopId(order.getShopId());
            liveRoomNotice.setLiveRoomId(order.getLiveRoomId());
            liveRoomNotice.setOrderAmount(toBePushed);
            liveRoomNotice.setPushTime(Timestamp.from(Instant.now()));

            lastStateValue.f0 = lastSearchIndex;
            lastStateValue.f1 = toBePushed;

            liveRoomOrderAmount.update(lastStateValue);

            return liveRoomNotice;
        }

        return null;
    }


    @Override
    public void open(Configuration config) {
        // Tuple2记录了查找起始点和上次的订单总金额
        ValueStateDescriptor<Tuple2<Integer, BigDecimal>> descriptor =
                new ValueStateDescriptor<>(
                        // the state name
                        "total-order-amount",
                        // type information
                        TypeInformation.of(new TypeHint<Tuple2<Integer, BigDecimal>>() {}));

        StateTtlConfig ttlConfig = StateTtlConfig
                // 保留一年
                .newBuilder(Time.days(90))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();

        descriptor.enableTimeToLive(ttlConfig);


        liveRoomOrderAmount = getRuntimeContext().getState(descriptor);

        try {
            if (liveRoomOrderAmount.value() == null) {
                    liveRoomOrderAmount.update(
                            Tuple2.of(0, BigDecimal.ZERO)
                    );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 初始化推送阶数 最大值已到 9*(10^12)=千亿
     * 100、200、300、1000、2000 ....
     *
     * @return
     */
    private static List<BigDecimal> init() {
        List<BigDecimal> list = new ArrayList<>();
        Integer[] baseNumber = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] indexNumber = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        for (Integer integer : indexNumber) {
            double pow = Math.pow(10, integer);
            for (Integer integer1 : baseNumber) {
                double v = pow * integer1;
                list.add(BigDecimal.valueOf(v));
            }
        }
        return list;
    }

}
