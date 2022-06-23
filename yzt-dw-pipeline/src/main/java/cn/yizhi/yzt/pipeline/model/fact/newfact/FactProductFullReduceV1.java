package cn.yizhi.yzt.pipeline.model.fact.newfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactProductFullReduceV1 {
    /**
     *
     */
    private String rowDate;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 商品id
     */
    private Integer productId;

    /**
     * 满减id
     */
    private Integer fullReduceId;

    /**
     * 通过满减活动的pv
     */
    private Integer pv;

    /**
     * 通过满减活动的uv
     */
    private Integer uv;

    /**
     * 销售额_通过满减活动
     */
    private BigDecimal saleTotal;

    /**
     * 销量_通过满减活动
     */
    private Integer saleCount;

    /**
     * 当日付款单数_通过满减活动
     */
    private Integer payCount;

    /**
     * 当日付款人数_通过满减活动
     */
    private Integer payNumber;

    /**
     * 活动优惠总金额
     */
    private BigDecimal discountAmount;

    /**
     * 当日分享次数
     */
    private Integer shareCount;
}
