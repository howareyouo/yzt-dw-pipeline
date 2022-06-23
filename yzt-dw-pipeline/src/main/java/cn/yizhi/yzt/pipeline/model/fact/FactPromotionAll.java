package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: HuCheng
 * 秒杀活动累计数据指标
 * @Date: 2020/11/23 16:27
 */
@Data
public class FactPromotionAll {
    /**
     * 店铺 id
     */
    private Integer shopId;

    /**
     * 日期
     */
    private String rowDate;

    /**
     * 活动类型
     */
    private Integer promotionType;

    /**
     * 订单数
     */
    private int orderCount;

    /**
     * 退款数
     */
    private int refundCount;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 活动优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 新客数
     */
    private int newOrderMember;
}
