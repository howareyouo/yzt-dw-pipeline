package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author aorui created on 2020/11/2
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactProductFullReduce {

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


}
