package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author aorui created on 2020/11/3
 */
@Getter
@Setter
public class FactFullReduceItemJoinSteam {

    private Integer promotionId;

    private Long orderId;

    private BigDecimal totalPrice;

    private BigDecimal discountAmount;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Integer productId;

    private Integer quantity;

}
