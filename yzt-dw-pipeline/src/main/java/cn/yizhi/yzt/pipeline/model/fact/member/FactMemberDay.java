package cn.yizhi.yzt.pipeline.model.fact.member;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hucheng
 * 用户按天的实时数据
 * @date 2020/8/4 16:54
 */
@Data
public class FactMemberDay {
    /**
     * 当天日期
     */
    @Column(name = "row_date", type = Column.FieldType.KEY)
    private String rowDate;
    @Column(name = "shop_id", type = Column.FieldType.KEY)
    private Integer shopId;
    @Column(name = "member_id", type = Column.FieldType.KEY)
    private Integer memberId;

    /**
     * 当天累计订单数
     */
    private Long orderCount;

    /**
     * 当天累计消费金额
     */
    private BigDecimal totalOrderAmount;

}
