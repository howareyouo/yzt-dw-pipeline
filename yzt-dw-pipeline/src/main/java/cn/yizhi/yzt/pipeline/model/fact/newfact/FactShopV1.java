package cn.yizhi.yzt.pipeline.model.fact.newfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactShopV1 {

    private String rowTime;
    private String channel;
    private Integer shopId;
    /**
     * 累计客户数
     */
    private int memberCount;

    /**
     * 累计订单额
     */
    private BigDecimal orderAmount;

    /**
     * 累计订单数
     */
    private int orderCount;

    /**
     * 已消费客户数
     */
    private int orderUserCount;

    /**
     * 累计退款数
     */
    private int refundCount;

    /**
     * 累计退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 未消费客户数
     */
    private int notOrderUserCount;
}
