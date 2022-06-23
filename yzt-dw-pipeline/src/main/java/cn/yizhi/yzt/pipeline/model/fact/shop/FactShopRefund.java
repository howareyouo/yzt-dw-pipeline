package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/7/23 19:59
 */
@Getter
@Setter
public class FactShopRefund {
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
     * 退款单数
     */
    private int refundCount;


    /**
     * 退款人数
     */
    private int refundUserCount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
}
