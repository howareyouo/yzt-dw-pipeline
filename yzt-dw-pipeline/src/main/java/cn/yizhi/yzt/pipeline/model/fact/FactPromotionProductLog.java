package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;

/**
 * @Author: HuCheng
 * 活动商品访问日志，不去重
 * @Date: 2020/11/28 01:51
 */
@Data
public class FactPromotionProductLog {

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
     * 商品id
     */
    private Integer productId;


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

    /**
     * 加入购物车次数
     */
    private int addShoppingCartCount;

    /**
     * 加入购物车人数
     */
    private int addShoppingCartUserCount;
}
