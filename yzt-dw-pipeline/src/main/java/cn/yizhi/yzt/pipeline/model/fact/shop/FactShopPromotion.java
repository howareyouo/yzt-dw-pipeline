package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/11/4 15:41
 */
@Data
public class FactShopPromotion {
    /**
     * 时间
     */
    private String rowTime;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 活动订单金额
     */
    private BigDecimal promotionOrderAmount;


    /**
     * 活动优惠金额
     */
    private BigDecimal promotionDiscountAmount;


    /**
     * 有效活动数
     */
    private Integer promotionCount;
}
