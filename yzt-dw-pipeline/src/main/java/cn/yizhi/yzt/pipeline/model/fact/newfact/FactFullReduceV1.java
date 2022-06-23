package cn.yizhi.yzt.pipeline.model.fact.newfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactFullReduceV1 {

    /**
     *
     */
    private String rowDate;

    /**
     * 店铺Id
     */
    private Integer shopId;

    /**
     * 满减活动id
     */
    private Integer promotionId;

    /**
     *
     */
    private Integer pv;

    /**
     *
     */
    private Integer uv;

    /**
     * 主页商品访问量
     */
    private Integer productPv;

    /**
     * 当日总订单数
     */
    private Integer orderCount;

    /**
     * 付款总额
     */
    private BigDecimal payTotal;

    /**
     * 当日付款人数
     */
    private Integer payNumber;

    /**
     * 商品销量
     */
    private Integer saleCount;

    /**
     * 活动商品销售额
     */
    private BigDecimal productAmount;

    /**
     * 优惠额
     */
    private BigDecimal preferentialAmount;

    /**
     * 主页加购次数
     */
    private Integer addcartCount;

    /**
     * 主页分享次数
     */
    private Integer shareCount;
}
