package cn.yizhi.yzt.pipeline.model.fact.historyfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactShopMonthHistory {

    /**
     * 月
     */
    private String month;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 总店id
     */
    private Integer mainShopId;

    /**
     * 浏览量
     */
    private Integer pv;

    /**
     * 订单数
     */
    private Integer orderCount;

    /**
     * 注册用户数
     */
    private Integer registerCount;

    /**
     * 支付总金额
     */
    private BigDecimal payAmount;

    /**
     *
     */
    private String channel;

    /**
     * 付款订单数
     */
    private Integer payCount;

    /**
     * 退款订单数
     */
    private Integer refundCount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
}
