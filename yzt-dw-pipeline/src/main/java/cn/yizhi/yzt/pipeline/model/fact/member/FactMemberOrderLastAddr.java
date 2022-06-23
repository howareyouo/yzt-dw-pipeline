package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/8/3 16:23
 */
@Getter
@Setter
@ToString(callSuper = true)

public class FactMemberOrderLastAddr{
    private Integer shopId;

    private Integer memberId;
    /**
     * 上次收货地址
     */
    private String lastConsigneeAddress;

    /**
     * 上次消费时间
     */
    private Timestamp lastOrderTime;

    /**
     * 上次订单id
     */
    private Long lastOrderId;


}
