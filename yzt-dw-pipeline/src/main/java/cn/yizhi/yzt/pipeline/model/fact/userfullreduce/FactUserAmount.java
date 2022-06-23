package cn.yizhi.yzt.pipeline.model.fact.userfullreduce;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author aorui created on 2020/11/5
 */
@Setter
@Getter
public class FactUserAmount {
    /**
     *
     */
    private String rowDate;

    /**
     * 店铺Id
     */
    private Integer shopId;

    /**
     * 用户id
     */
    private Integer memberId;

    /**
     * 满减id
     */
    private Integer fullReduceId;


    /**
     * 支付总金额
     */
    private BigDecimal payAmount;

    /**
     * 优惠总金额
     */
    private BigDecimal discountAmount;

    /**
     * 最后参与时间
     */
    private Timestamp lastJoinTime;


}
