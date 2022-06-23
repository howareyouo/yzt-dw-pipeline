package cn.yizhi.yzt.pipeline.model.fact.userfullreduce;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * @author aorui created on 2020/11/5
 */
@Setter
@Getter
public class FactUserCount {
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
     * 通过满减活动的订单数
     */
    private Integer orderCount;

    /**
     * 通过满减活动商品购买数
     */
    private Integer saleCount;

}
