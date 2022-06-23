package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/7/23 19:37
 */
@Getter
@Setter
public class FactShopHour{

    private Long pv;

    private Long uv;
    //加购次数

    private  Long addcartCount;
    //加购人数

    private  Long addcartNumber;

    private  Long shareCount;

    private  Long shareNumber;

    private  Long orderCount;

    private Long orderNumber;

    private Long payCount;

    private Long payNumber;

    //商品销量，不含退款
    private Integer saleCount;
    //支付总额

    private BigDecimal payAmount;

    private BigDecimal orderAmount;

    //退款单数
    private Long refundCount;

    //退款人数

    private Long refundNumber;
    //退款金额
    private BigDecimal refundAmount;
}
