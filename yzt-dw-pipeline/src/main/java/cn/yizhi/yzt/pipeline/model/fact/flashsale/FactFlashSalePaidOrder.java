package cn.yizhi.yzt.pipeline.model.fact.flashsale;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/10/23 16:37
 */
@Data
public class FactFlashSalePaidOrder {
    /**
     * 秒杀活动id
     */
    private  Integer activityId;

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
