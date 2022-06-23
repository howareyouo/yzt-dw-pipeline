package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/7/31 11:46
 */
@Getter
@Setter
@ToString
public class FactMemberRefund{
    private Integer shopId;

    private Integer memberId;
    /**
     * 累计退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 累计退款次数
     */
    private Integer refundCount;
}
