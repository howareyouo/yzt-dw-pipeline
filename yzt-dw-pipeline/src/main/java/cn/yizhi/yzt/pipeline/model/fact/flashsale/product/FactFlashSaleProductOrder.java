package cn.yizhi.yzt.pipeline.model.fact.flashsale.product;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/11/2 14:23
 */
@Data
public class FactFlashSaleProductOrder {
    /**
     * 秒杀活动id
     */
    private  Integer activityId;


    /**
     * 秒杀活动下商品id
     */
    private Integer productId;

    /**
     * 下单次数
     */
    private Integer orderCount;

    /**
     * 下单人数
     */
    private Integer orderNumber;
}
