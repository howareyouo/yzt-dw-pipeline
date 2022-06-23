package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/6/20 17:55
 */
//事务型
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactOrder {
    @JsonProperty("fk_shop")
    private Integer fkShop;
    @JsonProperty("fk_date")
    private int fkDate;
    @JsonProperty("fk_member")
    private Integer fkMember;
    @JsonProperty("fk_channel")
    private int fkChannel;
    @JsonProperty("fk_payment")
    private int fkPayment;
    @JsonProperty("order_id")
    private Long orderId;
    @JsonProperty("union_no")
    private String unionNo;
    @JsonProperty("order_no")
    private String orderNo;

    @JsonProperty("status")
    private Integer status;
    @JsonProperty("order_type")
    private Integer orderType;

    @JsonProperty("transaction_no")
    private String transactionNo;

    @JsonProperty("product_amount")
    private BigDecimal productAmount;
    @JsonProperty("ship_cost_amount")
    private BigDecimal shipCostAmount;
    @JsonProperty("ship_cost_reduced")
    private BigDecimal shipCostReduced;
    //总优惠金额
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    @JsonProperty("actual_amount")
    private BigDecimal actualAmount;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("ship_method")
    private Integer shipMethod;
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


