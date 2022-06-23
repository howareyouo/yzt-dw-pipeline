package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/11/2 16:16
 */
@Data
public class FactOrderPromotion {
    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("main_shop_id")
    private Integer mainShopId;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("union_no")
    private String unionNo;

    @JsonProperty("order_no")
    private String orderNo;

    @JsonProperty("transaction_no")
    private String transactionNo;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("source")
    private String source;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("order_type")
    private Integer orderType;

    @JsonProperty("product_amount")
    private BigDecimal productAmount;

    @JsonProperty("ship_cost_amount")
    private BigDecimal shipCostAmount;

    @JsonProperty("ship_cost_reduced")
    private BigDecimal shipCostReduced;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("actual_amount")
    private BigDecimal actualAmount;

    @JsonProperty("member_id")
    private Integer memberId;

    @JsonProperty("ship_method")
    private Integer shipMethod;

    @JsonProperty("closed_at")
    private Timestamp closedAt;

    @JsonProperty("received_at")
    private Timestamp receivedAt;

    @JsonProperty("paid_at")
    private Timestamp paidAt;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    /**
     * 活动类型， 在product-service GRPC接口中定义：
     *
     *     活动类型不要使用0
     *     NOACTIVITY = 0;
     *     优惠券
     *     COUPON = 1;
     *     满减满赠
     *     MANJIAN = 2;
     *     限时抢购
     *     FLASHSALE = 3;
     *     直播
     *     WX_LIVE = 4;
     *
     *  最新的定义请参考源头文件
     */
    @JsonProperty("promotion_type")
    private Integer promotionType;

    @JsonProperty("promotion_id")
    private Integer promotionId;
    /**
     * 活动优惠金额
     */
    @JsonProperty("promotion_discount_amount")
    private BigDecimal promotionDiscountAmount;
}
