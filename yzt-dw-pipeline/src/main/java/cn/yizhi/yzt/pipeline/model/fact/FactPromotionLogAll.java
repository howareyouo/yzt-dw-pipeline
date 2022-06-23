package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;

/**
 * @Author: HuCheng
 * 活动累计埋点数据
 * @Date: 2020/11/28 01:39
 */
@Data
public class FactPromotionLogAll {
    /**
     * 渠道
     */
    private String channel;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 活动id
     */
    private Integer promotionId;

    /**
     * 活动类型
     */
    private Integer promotionType;

    /**
     * pv
     */
    private int pv;

    /**
     * uv
     */
    private int uv;

    /**
     * 分享次数
     */
    private int shareCount;

    /**
     * 分享人数
     */
    private int shareUserCount;
}
