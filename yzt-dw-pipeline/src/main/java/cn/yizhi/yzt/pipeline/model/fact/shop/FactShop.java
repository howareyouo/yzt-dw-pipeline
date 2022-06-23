package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/11/11 下午3:45
 */
@Data
public class FactShop {
    private String rowTime;

    private int shopId;
    private String channel;

    /**
     * 累计退款单数
     */
    private int refundCount;

    /**
     * 累计退款金额
     */
    private BigDecimal refundAmount;


    /**
     * 累计付款单数
     */
    private int orderCount;

    /**
     * 累计付款金额
     */
    private BigDecimal orderAmount;

    /**
     * 累计付款人数
     */
    private int orderUserCount = 0;


    /**
     * 累计未付款人数
     */
    private int notOrderUserCount;

    /**
     * 累计客户数
     */
    private int memberCount;
}
