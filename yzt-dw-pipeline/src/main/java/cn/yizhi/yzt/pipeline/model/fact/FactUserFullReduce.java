package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author aorui created on 2020/11/4
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactUserFullReduce {

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

    /**
     * 支付总金额
     */
    private BigDecimal payAmount;

    /**
     * 优惠总金额
     */
    private BigDecimal discountAmount;

    /**
     * 活动分享次数
     */
    private Integer shareCountFullReduce;

    /**
     * 商品分享次数
     */
    private Integer shareCountProduct;

    /**
     * 最后参与时间
     */
    private Timestamp lastJoinTime;

}
