package cn.yizhi.yzt.pipeline.model.fact.groupbuy.promotion;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: HuCheng
 * @Date: 2020/12/14 16:50
 */
@Data
public class FactGroupBugRefund {
    /**
     * 拼团groupId
     */
    private Integer groupPromotionId;

    /**
     * 退款总额
     */
    private BigDecimal refundAmount;

    /**
     * 退款单数
     */
    private Integer refundCount;

    /**
     * 退款人数
     */
    private Integer refundUserCount;

}
