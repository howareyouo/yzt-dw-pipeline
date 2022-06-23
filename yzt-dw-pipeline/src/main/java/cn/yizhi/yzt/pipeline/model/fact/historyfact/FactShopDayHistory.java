package cn.yizhi.yzt.pipeline.model.fact.historyfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactShopDayHistory {

    /**
     * 日期
     */
    private String rowDate;

    /**
     * 渠道
     */
    private String channel;

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
     * 访客
     */
    private Integer uv;

    /**
     * 加购人数
     */
    private Integer addcartMember;

    /**
     * 订单数
     */
    private Integer orderCount;

    /**
     * 下单人数
     */
    private Integer orderMember;

    /**
     * 付款人数
     */
    private Integer payMember;

    /**
     * 注册用户数
     */
    private Integer registerCount;

    /**
     * 支付总金额
     */
    private BigDecimal payAmount;

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

    /**
     * 店铺下商品访问人数
     */
    private Integer uvProduct;
}
