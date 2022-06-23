package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author hucheng
 * 订单周期表 天
 * @date 2020/6/26 11:25
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactOrderDaily {
    @JsonProperty("fk_shop")
    private int fkShop;
    @JsonProperty("fk_date")
    private int fkDate;
    @JsonProperty("fk_member")
    private int fkMember;
    @JsonProperty("fk_product")
    private int fkProduct;
    @JsonProperty("fk_sku")
    private int fkSku;
    @JsonProperty("fk_channel")
    private int fkChannel;
    @JsonProperty("fk_payment")
    private int fkPayment;

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
    //订单数
    @JsonProperty("order_count")
    private int orderCount;
    //活动订单数
    @JsonProperty("promotion_order_count")
    private int promotionOrderCount;
    //活动订单金额
    @JsonProperty("get_promotion_order_amount")
    private int getPromotionOrderAmount;
}
