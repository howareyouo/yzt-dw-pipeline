package cn.yizhi.yzt.pipeline.model.fact.flashsale;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/10/23 15:14
 */
@Data
public class FactFlashSaleOrder {
    /**
     * 秒杀活动id
     */
    private  Integer activityId;

    /**
     * 下单次数
     */
    private Integer orderCount;

    /**
     * 下单人数
     */
    private Integer orderNumber;

}
