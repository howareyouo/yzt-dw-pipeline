package cn.yizhi.yzt.pipeline.model.fact.flashsale;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: HuCheng
 * @Date: 2020/11/30 15:40
 */
@Data
public class FactFlashSaleEveryDay {
    private String rowDate;
    private Integer shopId;
    /**
     * 活动类型 3
     */
    private Integer promotionType;
    /**
     * 每天活动订单数
     */
    private int orderCount;
    /**
     * 每天活动订单额
     */
    private BigDecimal orderAmount;
    /**
     * 每天活动优惠金额
     */
    private BigDecimal discountAmount;
}
