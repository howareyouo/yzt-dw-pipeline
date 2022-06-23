package cn.yizhi.yzt.pipeline.model.fact.liveroom;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/8/10 15:56
 */
@Data
public class LiveRoomNotice {
    /**
     * 通知类型
     * 0-进入直播间 1-观看人数提醒 2-订单金额提醒 3-点击商品 4-购买商品 5-点击优惠劵 6-领取优惠劵 7-直播开始 8-直播结束
     */
    @JsonProperty("notice_type")
    private Integer noticeType;

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
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;

    /**
     * 商品名称
     */
    @JsonProperty("product_name")
    private String productName;

    /**
     * 购买商品数量
     */
    @JsonProperty("product_count")
    private Integer productCount;

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

    /**
     * 支付订单金额
     */
    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    /**
     * 观看人数
     */
    @JsonProperty("uv")
    private Long uv;

    /**
     * 推送时间
     */
    @JsonProperty("push_time")
    private Timestamp pushTime;
}
