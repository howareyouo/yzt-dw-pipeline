package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/10/30 14:59
 */
@Data
public class FactMemberLog {
    /**
     * 时间天
     */
    private String rowTime;


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
     * 会员id 不用deviceId
     */
    private Integer memberId;


    /**
     * 分享活动次数
     */
    private int shareActivityCount;

    /**
     * 分享活动商品次数
     */
    private int shareActivityProductCount;

    /**
     * 分享店铺次数
     */
    private int shareShopCount;
}
