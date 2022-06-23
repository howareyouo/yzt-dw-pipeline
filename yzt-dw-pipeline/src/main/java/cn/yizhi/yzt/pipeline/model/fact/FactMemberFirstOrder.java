package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/6/28 20:36
 */
@Data
public class FactMemberFirstOrder {
    @JsonProperty("id")
    private int id;
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

    @JsonProperty("promotion_type")
    private Integer promotionType;
    @JsonProperty("promotion_id")
    private Integer promotionId;

    @JsonProperty("member_id")
    private Integer memberId;

    @JsonProperty("paid_at")
    private Timestamp paidAt;

    @JsonProperty("created_at")
    private Timestamp createdAt;
}
