package cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion;

import lombok.Data;

/**
 * @Author: HuCheng
 * 拼团一期数据指标-日志
 * @Date: 2020/12/14 16:47
 */
@Data
public class FactGroupBugLog {
    /**
     * 拼团活动id
     */
    private Integer promotionId;

    /**
     * pv
     */
    private Integer pv;

    /**
     * uv
     */
    private Integer uv;

    /**
     * 分享次数
     */
    private Integer shareCount;

    /**
     * 分享人数
     */
    private Integer shareUserCount;
}
