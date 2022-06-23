package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/10/27 18:48
 */
@Data
public class FactProductLog {
    /**
     * 时间天
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

    @Override
    public String toString() {
        return "FactProductLog{" +
                "rowTime='" + rowTime + '\'' +
                ", channel='" + channel + '\'' +
                ", shopId=" + shopId +
                ", promotionId=" + promotionId +
                ", promotionType=" + promotionType +
                ", productId=" + productId +
                ", pv=" + pv +
                ", uv=" + uv +
                ", shareCount=" + shareCount +
                ", shareUserCount=" + shareUserCount +
                ", addShoppingCartCount=" + addShoppingCartCount +
                ", addShoppingCartUserCount=" + addShoppingCartUserCount +
                '}';
    }
}
