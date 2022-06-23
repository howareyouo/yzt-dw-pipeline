package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/11/5
 */
@Getter
@Setter
public class FactFullReduceProductPv {

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
     * 主页商品访问量
     */
    private Integer productPv;

    /**
     * 主页加购次数
     */
    private Integer addcartCount;
}
