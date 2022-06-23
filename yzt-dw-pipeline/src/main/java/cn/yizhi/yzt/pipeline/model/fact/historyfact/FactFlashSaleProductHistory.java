package cn.yizhi.yzt.pipeline.model.fact.historyfact;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactFlashSaleProductHistory {
    //日期
    private String rowDate;
    //活动id
    private Integer activityId;
    //商店
    private Integer shopId;
    //商品
    private Integer productId;

    //商品浏览次数
    private Integer pv;
    //用户浏览人数
    private Integer uv;
    //当日下单次数
    private Integer orderCount;
    //当日下单人数
    private Integer orderNumber;
    //当日付款总额
    private BigDecimal payTotal;
    //当日优惠总额
    private BigDecimal payCouponTotal;
    //当日付款总额
    private Integer payCount;
    //当日付款人数
    private Integer payNumber;
    //商品销量
    private Integer saleCount;
    //剩余库存
    private Integer inventory;
    //分享次数
    private Integer shareCount;
    //分享人数
    private Integer shareUserCount;
    //预约人数
    private String appointmentCount;
}
