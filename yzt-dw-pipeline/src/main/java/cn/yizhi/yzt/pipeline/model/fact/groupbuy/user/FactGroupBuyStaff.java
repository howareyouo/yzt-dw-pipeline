package cn.yizhi.yzt.pipeline.model.fact.groupbuy.user;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: HuCheng
 * 拼团员工统计表
 * @Date: 2020/12/14 16:54
 */
@Data
public class FactGroupBuyStaff {
    /**
     * 活动id
     */
    private int promotionId;

    /**
     * 员工id
     */
    private int staffId;

    /**
     * 受益人姓名
     */
    private String name;

    /**
     * 受益人手机号
     */
    private String phone;

    /**
     * 订单总金额
     */
    private BigDecimal payAmount;

    /**
     * 订单数
     */
    private Integer orderNumber;

    /**
     * 退款订单数
     */
    private Integer refundNumber;

    /**
     * 退款订单金额
     */
    private BigDecimal refundAmount;

    /**
     * 拓新客数
     */
    private Integer newUserCount;
}
