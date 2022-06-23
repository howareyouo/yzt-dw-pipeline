package cn.yizhi.yzt.pipeline.model.fact.newfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactShopHourV1 {
    private String rowTime;
    private String channel;
    private Integer shopId;
    private int pv;
    private int uv;
    /**
     * 商品pv
     */
    private int pvProduct;
    /**
     * 商品uv
     */
    private int uvProduct;
    /**
     * 加购数
     */
    private int addCartCount;
    /**
     * 加购人数
     */
    private int addCartUserCount;
    /**
     * 分享数
     */
    private int shareCount;
    /**
     * 分享人数
     */
    private int shareUserCount;
    /**
     * 下单数
     */
    private int orderCount;
    /**
     * 下单人数
     */
    private int orderUserCount;
    /**
     * 订单金额
     */
    private BigDecimal orderAmount;
    /**
     * 支付单数
     */
    private int payCount;
    /**
     * 支付人数
     */
    private int payUserCount;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
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
    /**
     * 销量
     */
    private int saleCount;
    /**
     * 活动订单金额
     */
    private BigDecimal promotionOrderAmount;

    /**
     * 活动订单优惠金额
     */
    private BigDecimal promotionDiscountAmount;

    /**
     * 注册用户数
     */
    private int registerCount;

    /**
     * 有效活动数
     */
    private int promotionCount;
}
