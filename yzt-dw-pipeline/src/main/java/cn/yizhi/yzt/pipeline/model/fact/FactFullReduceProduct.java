package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * @author aorui created on 2020/11/4
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactFullReduceProduct {


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


}
