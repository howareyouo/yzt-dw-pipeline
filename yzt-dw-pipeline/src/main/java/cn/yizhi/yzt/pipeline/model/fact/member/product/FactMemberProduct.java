package cn.yizhi.yzt.pipeline.model.fact.member.product;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @Author: HuCheng
 * @Date: 2021/1/5 16:23
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactMemberProduct {
    /**
     * 处理时间
     */
    @JsonProperty("analysis_date")
    private String analysisDate;

    /**
     * 店铺ID
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 会员id
     */
    @JsonProperty("member_id")
    private Integer memberId;

    /**
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;

    /**
     * 商品浏览数
     */
    @JsonProperty("viewed_times")
    private Integer viewedTimes;

    /**
     * 分享次数
     */
    @JsonProperty("share_times")
    private Integer shareTimes;

    /**
     * 下单次数
     */
    @JsonProperty("order_times")
    private Integer orderTimes;

    /**
     * 下单金额
     */
    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    /**
     * 购买件数
     */
    @JsonProperty("order_quantity")
    private Integer orderQuantity;

    /**
     * 付款订单数
     */
    @JsonProperty("paid_times")
    private Integer paidTimes;

    /**
     * 付款金额
     */
    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;

    /**
     * 退款次数
     */
    @JsonProperty("refund_times")
    private Integer refundTimes;

    /**
     * 退款金额
     */
    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;

    /**
     * 加入购物车时间
     */
    @JsonProperty("cart_add_date")
    private String cartAddDate;

    /**
     * 加入购物车次数
     */
    @JsonProperty("cart_add_times")
    private Integer cartAddTimes;
}
