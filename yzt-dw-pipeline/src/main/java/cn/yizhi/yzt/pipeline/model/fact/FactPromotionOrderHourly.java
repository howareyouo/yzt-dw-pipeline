package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Time;

@Getter
@Setter
class FactPromotionOrderHourly {
    @JsonProperty("fk_shop")
    private int fkShop;
    @JsonProperty("fk_date")
    private int fkDate;
    @JsonProperty("fk_promotion")
    private int fkPromotion;
    @JsonProperty("fk_channel")
    private int fkChannel;
    @JsonProperty("fk_product")
    private int fkProduct;
    @JsonProperty("fk_sku")
    private int fkSku;

    //订单内参与活动的商品的金额
    private BigDecimal productAmount;
    // 一个活动在订单里的总优惠金额
    private BigDecimal discountAmount;
    // 参与活动的商品的数量
    private int productQuantity;

    private Time rowTime;

}