package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * 导入或者新增用户
 * @date 2020/7/31 11:39
 */
@Getter
@Setter
@ToString
public class FactMemberExport{
    private Integer shopId;

    private Integer memberId;
    /**
     * 储值余额
     */
    private BigDecimal balanceAmount;

    /**
     * 赠金余额
     */
    private BigDecimal giftAmount;

    /**
     * 积分
     */
    private BigDecimal points;

    /**
     * 欠款
     */
    private BigDecimal debtAmount;

    /**
     * 介绍人
     */
    private String referrer;

    /**
     * 跟踪员工
     */
    private String follower;

    /**
     * 建档时间
     */
    private Timestamp createdAt;

    /**
     * 导入产生订单次数
     */
    private Integer orderCountExport;

    /**
     * 导入产生消费总额
     */
    private BigDecimal totalOrderAmountExport;

    /**
     * 上次浏览店铺时间（导入）
     */
    private Timestamp lastViewShopTime;

    /**
     * 上次消费服务（导入）
     */
    private String lastConsumeServices;

    /**
     * 上次收货地址(导入)
     */
    private String lastConsigneeAddress;

    /**
     * 上次订单时间（导入）
     */
    private Timestamp lastOrderTime;
}
