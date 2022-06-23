package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author hucheng
 * 优惠劵
 * @date 2020/6/26 11:32
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactCouponDaily {
    @JsonProperty("fk_shop")
    private int fkShop;
    @JsonProperty("fk_date")
    private int fkDate;
    @JsonProperty("fk_product")
    private int fkProduct;
    @JsonProperty("fk_sku")
    private int fkSku;
    @JsonProperty("fk_channel")
    private int fkChannel;
    @JsonProperty("fk_promotion")
    private int fkPromotion;

    //总优惠金额
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    @JsonProperty("actual_amount")
    private BigDecimal actualAmount;
    @JsonProperty("order_count")
    private int orderCount;
}
