package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/10/30 14:18
 */
@Data
public class FactPromotionLogByDay {
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

    @Override
    public String toString() {
        return "FactPromotionLog{" +
                "rowTime='" + rowTime + '\'' +
                ", channel='" + channel + '\'' +
                ", shopId=" + shopId +
                ", promotionId=" + promotionId +
                ", promotionType=" + promotionType +
                ", pv=" + pv +
                ", uv=" + uv +
                ", shareCount=" + shareCount +
                ", shareUserCount=" + shareUserCount +
                '}';
    }
}
