package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author aorui created on 2020/11/2
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsFullReducePromotionOrder {

    /**
     * id
     */
    @JsonProperty("id")
    private Long id;

    /**
     * 门店id
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 会员id
     */
    @JsonProperty("member_id")
    private Integer memberId;

    /**
     * 活动id
     */
    @JsonProperty("promotion_id")
    private Integer promotionId;

    /**
     * 订单id
     */
    @JsonProperty("order_id")
    private Long orderId;

    /**
     * 订单号
     */
    @JsonProperty("order_no")
    private String orderNo;

    /**
     * 订单总金额
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;

    /**
     * 是否支付
     */
    @JsonProperty("is_paid")
    private Integer isPaid;

    /**
     * 是否退款
     */
    @JsonProperty("is_refund")
    private Integer isRefund;

    /**
     * 是否关闭
     */
    @JsonProperty("is_closed")
    private Integer isClosed;

    /**
     * 商品总价格
     */
    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    /**
     * 优惠金额
     */
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Timestamp createdAt;

    /**
     * 修改时间
     */
    @JsonProperty("updated_at")
    private Timestamp updatedAt;

}
