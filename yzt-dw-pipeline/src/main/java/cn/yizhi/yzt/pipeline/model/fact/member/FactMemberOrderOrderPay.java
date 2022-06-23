package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/7/30 02:57
 */
@Getter
@Setter
@ToString
public class FactMemberOrderOrderPay{
    private Integer shopId;

    private Integer memberId;
    /**
     * 总订单金额
     */
    private Long orderCount;

    /**
     * 总消费金额
     */
    private BigDecimal totalOrderAmount;
}
