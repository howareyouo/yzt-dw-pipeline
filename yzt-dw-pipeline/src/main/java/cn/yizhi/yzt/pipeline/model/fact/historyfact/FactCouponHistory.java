package cn.yizhi.yzt.pipeline.model.fact.historyfact;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class FactCouponHistory {

    /**
     *
     */
    private String rowDate;

    /**
     * 实质为优惠券模版Id（coupon_template_id）
     */
    private Integer couponId;

    /**
     *
     */
    private Integer shopId;

    /**
     *
     */
    private String channel;

    /**
     * 当日支付总金额
     */
    private BigDecimal payAmount;

    /**
     * 优惠总金额
     */
    private BigDecimal preferentialAmount;

    /**
     * 核销人数
     */
    private Integer writeOffNumber;

    /**
     * 核销张数
     */
    private Integer writeOffCount;

    /**
     * 商品销量
     */
    private Integer saleCount;

    /**
     * 当日总订单数
     */
    private Integer orderCount;

    /**
     * 当日下单人数
     */
    private Integer orderNumber;

    /**
     * 用劵新客
     */
    private Integer newMemberCount;

    /**
     * 用劵老客
     */
    private Integer oldMemberCount;

}
