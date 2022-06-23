package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/7/23 19:50
 */
@Data
public class FactShopProductSold {
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
     * 商品销量
     */
    private int saleCount;
}
