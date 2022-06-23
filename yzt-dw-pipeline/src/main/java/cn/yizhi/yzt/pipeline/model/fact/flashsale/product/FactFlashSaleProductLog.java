package cn.yizhi.yzt.pipeline.model.fact.flashsale.product;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/10/30 17:19
 */
@Data
public class FactFlashSaleProductLog {

    /**
     * 秒杀活动id
     */
    private  Integer activityId;

    /**
     * 秒杀活动下商品id
     */
    private Integer productId;


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
