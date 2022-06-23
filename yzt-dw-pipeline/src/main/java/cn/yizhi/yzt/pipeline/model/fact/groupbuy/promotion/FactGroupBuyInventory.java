package cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion;

import lombok.Data;

/**
 * @Author: HuCheng
 * @Date: 2020/12/14 16:56
 */
@Data
public class FactGroupBuyInventory {
    /**
     * 拼团活动id
     */
    private Integer promotionId;

    /**
     * 剩余库存数
     */
    private Integer inventory;
}
