package cn.yizhi.yzt.pipeline.model.fact.liveroom;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author hucheng
 * @date 2020/8/11 16:14
 */
@Data
public class FactLiveRoomReceivedCoupon {
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
     * 领取优惠劵的数量
     */
    @JsonProperty("revived_coupon_count")
    private  Long revivedCouponCount;
}
