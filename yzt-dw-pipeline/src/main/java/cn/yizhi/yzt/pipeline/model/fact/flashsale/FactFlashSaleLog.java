package cn.yizhi.yzt.pipeline.model.fact.flashsale;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/10/20 15:03
 */
@Data
public class FactFlashSaleLog {
    /**
     * 秒杀活动id
     */
    private  Integer activityId;

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
