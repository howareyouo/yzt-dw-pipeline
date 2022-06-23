package cn.yizhi.yzt.pipeline.model.fact.flashsale;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/10/23 18:51
 */
@Data
public class FactFlashSaleInventory {
    /**
     * 秒杀活动id
     */
    private  Integer activityId;

    /**
     * 剩余库存
     */
    private Integer inventory;

}
