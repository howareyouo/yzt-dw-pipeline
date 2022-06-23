package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/11/3 19:16
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactOrderItem {
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


    @JsonProperty("product_id")
    private Integer productId;


    @JsonProperty("parent_id")
    private Integer parentId;

    @JsonProperty("sku_id")
    private Integer skuId;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("retail_price")
    private BigDecimal retailPrice;

    @JsonProperty("is_giveaway")
    private Boolean isGiveaway;

    /**
     * 商品优惠金额
     */
    @JsonProperty("product_discount_amount")
    private BigDecimal productDiscountAmount;

    @JsonProperty("name")
    private String name;

    /**
     * 是否需要展示电子凭证
     */
    @JsonProperty("show_voucher")
    private Boolean showVoucher;

    /**
     * 商品类型
     */
    @JsonProperty("product_type")
    private Integer productType;


    /**
     *商品实付
     */
    @JsonProperty("product_actual_amount")
    private  BigDecimal productActualAmount;
}
