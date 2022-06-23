package cn.yizhi.yzt.pipeline.model.fact.liveroom;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @program: yzt-coupon-mgmt
 * @package cn.yizhi.yzt.coupon.mgmt.wxlive.vo
 * @description: 微信直播间用户领取优惠券DTO
 * @author: wangdongming
 * @date: 2020-08-31
 */
@Data
public class LiveRoomUserReceiveCoupon {

    @JsonProperty("product_ype")
    private String productType;

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
     * 会员id
     */
    @JsonProperty("member_id")
    private Integer memberId;

    /**
     * 会员姓名
     */
    @JsonProperty("member_name")
    private String memberName;

    /**
     * 会员手机号
     */
    @JsonProperty("member_phone")
    private String memberPhone;

    /**
     * 购买商品数量
     *
     * 默认是0
     */
    @JsonProperty("coupon_count")
    private Integer couponCount;

    /**
     * 优惠劵id
     */
    @JsonProperty("coupon_id")
    private Integer couponId;

    /**
     * 优惠劵名称
     */
    @JsonProperty("coupon_name")
    private String couponName;
}
