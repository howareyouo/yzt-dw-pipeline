package cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author aorui created on 2021/1/20
 */
@Getter
@Setter
@Data
public class FactGroupBuyOrder {

    /**
     * 活动id
     */
    @JsonProperty("promotion_id")
    private Integer promotionId;

    /**
     * '订单总金额'
     */
    @JsonProperty("pay_amount")
    private BigDecimal payAmount;

    /**
     * '订单数'
     */
    @JsonProperty("order_number")
    private Integer orderNumber;

    /**
     * '退款订单数'
     */
    @JsonProperty("refund_number")
    private Integer refundNumber;

    /**
     * '退款订单金额'
     */
    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;

    /**
     * '拓新客数'
     */
    @JsonProperty("new_user_count")
    private Integer newUserCount;
}
