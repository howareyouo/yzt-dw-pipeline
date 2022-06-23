package cn.yizhi.yzt.pipeline.model.fact.newfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactFlashSaleV1 {
    private String rowDate;
    private Integer activityId;
    private int pv;
    private int uv;
    /**
     * 当日下单次数
     */
    private int orderCount;
    /**
     * 当日优惠总额
     */
    private BigDecimal payCouponTotal;

    /**
     * 当日下单人数
     */
    private int orderNumber;

    /**
     * 当日付款总额
     */
    private BigDecimal payTotal;

    /**
     * 当日付款单数
     */
    private int payCount;

    /**
     * 剩余库存
     */
    private int inventory;

    /**
     * 分享人数
     */
    private int shareUserCount;

    /**
     * 分享次数
     */
    private int shareCount;

    /**
     * 预约人数
     */
    private int appointmentCount;
}
