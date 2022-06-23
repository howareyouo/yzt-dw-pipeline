package cn.yizhi.yzt.pipeline.model.fact.liveroom;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/8/10 15:24
 */
@Data
public class FactLiveRoomOrder {
    /**
     * 店铺id
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 直播间id
     */
    @JsonProperty("live_room_id")
    private Integer liveRoomId;

    /**
     * 订单数
     */
    @JsonProperty("order_count")
    private Long orderCount;

    /**
     * 总订单额
     */
    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    /**
     * 支付人数
     */
    @JsonProperty("pay_number")
    private Long payNumber;
}
