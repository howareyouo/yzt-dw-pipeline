package cn.yizhi.yzt.pipeline.model.fact.historyfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactShopHistory {

    /**
     *
     */
    private String rowDate;

    /**
     *
     */
    private String channel;

    /**
     *
     */
    private Integer shopId;

    /**
     *
     */
    private Integer mainShopId;

    /**
     *
     */
    private Integer pv;

    /**
     *
     */
    private Integer uv;

    /**
     * 分享次数
     */
    private Integer shareCount;

    /**
     * 累计订单额
     */
    private BigDecimal allAmount;

    /**
     * 截止当前累计客户数
     */
    private Integer allMember;

    /**
     * 已消费客户数-派生指标
     */
    private Integer paiedMember;

    /**
     * 未消费客户数-派生指标
     */
    private Integer notPaiedMember;

    /**
     * 首购客户数-派生指标
     */
    private Integer firstPaiedMember;

    /**
     * 非首购客户数-派生指标
     */
    private Integer notFirstPaiedMember;

    /**
     * 当日支付订单总额
     */
    private BigDecimal payAmount;

    /**
     * 有效活动数
     */
    private Integer promotionCount;

    /**
     * 活动订单总额
     */
    private BigDecimal promotionOrderAmount;

    /**
     * 优惠总金额
     */
    private BigDecimal promotionDiscountAmount;

    /**
     * 累计退款金额
     */
    private BigDecimal allRefundAmount;
}
