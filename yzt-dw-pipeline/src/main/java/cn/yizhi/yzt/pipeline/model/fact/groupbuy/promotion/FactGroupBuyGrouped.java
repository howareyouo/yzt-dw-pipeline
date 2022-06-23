package cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion;

import lombok.Data;

/**
 * @Author: HuCheng
 * @Date: 2020/12/14 16:52
 */
@Data
public class FactGroupBuyGrouped {
    /**
     * 拼团活动id
     */
    private Integer promotionId;

    /**
     * 成团数
     */
    private Integer groupedCount;

    /**
     * 待成团数
     */
    private Integer groupingCount;

}
