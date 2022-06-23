package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author aorui created on 2021/1/12
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactPackEmallOrder {
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

    /**
     * 1:包装 2：原数据
     */
    @JsonProperty("pack_type")
    private Integer packType;

    /**
     * 分组id
     */
    @JsonProperty("group_id")
    private Integer groupId;

    /**
     * 分组ids
     */
    @JsonProperty("group_ids")
    private List<Integer> groupIds;
}
