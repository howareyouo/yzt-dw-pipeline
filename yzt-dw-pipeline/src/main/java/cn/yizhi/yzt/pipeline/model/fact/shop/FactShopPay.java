package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/7/23 19:55
 */
@Setter
@Getter
public class FactShopPay {
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
     * 支付单数
     */
    private int payCount;

    /**
     * 支付人数
     */
    private int payUserCount;

    /**
     * 支付总额
     */
    private BigDecimal payAmount;
}
