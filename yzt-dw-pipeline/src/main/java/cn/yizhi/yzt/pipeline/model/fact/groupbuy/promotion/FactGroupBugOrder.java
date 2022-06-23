package cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: HuCheng
 * @Date: 2020/12/14 16:49
 */
@Data
public class FactGroupBugOrder {
    /**
     * 拼团groupId
     */
    private Integer groupPromotionId;

    /**
     * 交易总额
     */
    private BigDecimal orderAmount;

    /**
     * 订单数
     */
    private Integer orderCount;

    /**
     * 下单人数
     */
    private Integer orderUserCount;

}
