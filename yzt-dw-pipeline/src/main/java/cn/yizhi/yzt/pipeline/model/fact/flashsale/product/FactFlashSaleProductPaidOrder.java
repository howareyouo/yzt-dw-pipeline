package cn.yizhi.yzt.pipeline.model.fact.flashsale.product;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/11/2 14:37
 */
@Data
public class FactFlashSaleProductPaidOrder {
    /**
     * 秒杀活动id
     */
    private Integer activityId;

    /**
     * 秒杀活动下商品id
     */
    private Integer productId;

    /**
     * 付款总额
     */
    private BigDecimal payTotal;

    /**
     * 付款单数
     */
    private Integer payCount;

    /**
     * 付款人数
     */
    private Integer payNumber;


    /**
     * 优惠总金额
     */
    private BigDecimal payCouponTotal;

    /**
     * 销量
     */
    private Integer saleCount;
}
