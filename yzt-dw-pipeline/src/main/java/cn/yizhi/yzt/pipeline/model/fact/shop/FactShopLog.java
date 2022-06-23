package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hucheng
 * 店铺当天实时pv、uv
 * @date 2020/7/23 19:33
 */
@Getter
@Setter
public class FactShopLog {
    /**
     * 时间
     */
    private String rowTime;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 店铺id
     */
    private Integer shopId;

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
     * 店铺下商品浏览次数
     */
    private int pvProduct;

    /**
     * 店铺下商品浏览人数
     */
    private int uvProduct;

    /**
     * 店铺下商品加购次数
     */
    private int addCartCount;

    /**
     * 店铺下商品加购人数
     */
    private int addCartUserCount;
}
