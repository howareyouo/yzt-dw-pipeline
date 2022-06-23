package cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion;

import lombok.Data;

/**
 * @Author: HuCheng
 * @Date: 2021/1/23 16:21
 */
@Data
public class FactGroupBuyGroupV2 {
    /**
     * 拼团活动id
     */
    private Integer promotionId;

    /**
     * 成团数
     */
    private Integer groupedCount;

    /**
     * 满团数
     */
    private Integer fullGroupedCount;

    /**
     * 成团失败数
     */
    private Integer groupedFailedCount;
}
