package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/7/23 19:52
 */
@Getter
@Setter
public class FactShopOrder {
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
     * 下单次数
     */
    private  int orderCount;

    /**
     * 下单人数
     */
    private int orderUserCount;

    /**
     * 下单金额
     */
    private BigDecimal orderAmount;
}
