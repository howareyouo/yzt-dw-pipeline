package cn.yizhi.yzt.pipeline.model.fact.historyfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactProductHistory {

    /**
     *
     */
    private String rowDate;

    /**
     * 商品id
     */
    private Integer productId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     *
     */
    private String channel;

    /**
     *
     */
    private Integer pv;

    /**
     *
     */
    private Integer uv;

    /**
     * 销售额
     */
    private BigDecimal saleTotal;

    /**
     * 销量
     */
    private Integer saleCount;

    /**
     * 当日总订单数
     */
    private Integer orderCount;

    /**
     * 当日下单人数
     */
    private Integer orderNumber;

    /**
     * 当日付款单数
     */
    private Integer payCount;

    /**
     * 当日付款人数
     */
    private Integer payNumber;

    /**
     * 剩余库存
     */
    private Integer inventory;

    /**
     * 分享次数
     */
    private Integer shareCount;

    /**
     * 分享人数
     */
    private Integer shareNumber;

    /**
     * 加购次数
     */
    private Integer addcartCount;

    /**
     * 加购人数
     */
    private Integer addcartNumber;

}