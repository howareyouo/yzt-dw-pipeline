package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hucheng
 * @date 2020/7/23 19:40
 */
@Getter
@Setter
public class FactShopAddCart {
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
     * 加购次数
     */
    private int addcartCount;

    /**
     * 加购人数
     */
    private int addcartNumber;

}
