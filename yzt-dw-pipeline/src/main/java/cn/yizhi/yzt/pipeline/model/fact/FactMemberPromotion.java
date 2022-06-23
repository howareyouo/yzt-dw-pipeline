package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/6/28 20:36
 */
@Data
public class FactMemberPromotion {
    @JsonProperty("member_id")
    private Integer memberId;
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("promotion_type")
    private Integer promotionType;
    @JsonProperty("promotion_id")
    private Integer promotionId;

    //总订单数
    @JsonProperty("order_count")
    private Long orderCount;
    //活动订单数
    @JsonProperty("promotion_count")
    private Long promotionCount;
    @JsonProperty("original_order_time")
    private Timestamp originalOrderTime;
    @JsonProperty("original_order_promotion_time")
    private Timestamp originalOrderPromotionTime;
    //总订单金额
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

}
