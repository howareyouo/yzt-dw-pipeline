package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/6/20 18:32
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsEmallOrder {
    @JsonProperty("id")
    private Long id;
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
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("member_id")
    private Integer memberId;
    @JsonProperty("openid")
    private String openid;
    @JsonProperty("ship_method")
    private Integer shipMethod;
    @JsonProperty("remind_ship_count")
    private Integer remindShipCount;
    @JsonProperty("closed_at")
    private Timestamp closedAt;
    @JsonProperty("received_at")
    private Timestamp receivedAt;
    @JsonProperty("paid_at")
    private Timestamp paidAt;
    @JsonProperty("cancel_reason")
    private String cancelReason;
    @JsonProperty("deleted")
    private Integer deleted;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;
}
