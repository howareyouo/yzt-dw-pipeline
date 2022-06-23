package cn.yizhi.yzt.pipeline.model.fact.historyfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactShopHourHistory {


    /**
     *
     */
    private String rowTime;

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
     * 加购次数
     */
    private Integer addcartCount;

    /**
     * 加购人数
     */
    private Integer addcartNumber;

    /**
     * 分享次数
     */
    private Integer shareCount;

    /**
     * 分享人数
     */
    private Integer shareNumber;

    /**
     * 下单次数
     */
    private Integer orderCount;

    /**
     * 下单人数
     */
    private Integer orderNumber;

    /**
     * 付款次数
     */
    private Integer payCount;

    /**
     * 付款人数
     */
    private Integer payNumber;

    /**
     * 商品销量-不含退款
     */
    private Integer saleCount;

    /**
     * 付款总额
     */
    private BigDecimal payAmount;

    /**
     * 下单金额
     */
    private BigDecimal orderAmount;

    /**
     * 退款单数
     */
    private Integer refundCount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
}