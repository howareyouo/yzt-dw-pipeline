package cn.yizhi.yzt.pipeline.model.fact.flashsale.product;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/11/2 14:56
 */
@Data
public class FactFlashSaleProductInventory {
    /**
     * 秒杀活动id
     */
    private  Integer activityId;

    /**
     * 秒杀活动下商品id
     */
    private Integer productId;
    
    /**
     * 剩余库存
     */
    private Integer inventory;
}
