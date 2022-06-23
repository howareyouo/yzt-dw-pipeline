package cn.yizhi.yzt.pipeline.model.dim;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
public class DimFullReduce {

    @Column(name = "id", type = Column.FieldType.PK)
    private Long id;

    @Column(name = "full_reduce_id", type = Column.FieldType.KEY)
    private Long fullReduceId;

    private Integer shopId;

    private String name;
    /**
     * 商品规则
     */
    private String productRule;

    /**
     *优惠规则
     */
    private String discountRule;

    /**
     * 参与限制
     */
    private String participateRule;

    /**
     * 仅限新客参与:0-否 1-是
     */
    private String forFirstMember;

    /**
     *开始时间
     */
    private Timestamp startTime;

    /**
     * 结束时间
     */
    private Timestamp endTime;

    /**
     * 优惠券状态 0:未开始，1:进行中，2:已结束
     */
    private String state;

    /**
     * 逻辑删除标志 0:未删除，1:已删除
     */
    private String delFlag;

    /**
     * 优惠券规则修改时间
     */
    private Timestamp discountRuleUpdatedAt;

    /**
     * 是否长期活动
     */
    private Boolean isLongTerm;

    /**
     * 是否包含全部商品
     */
    private Boolean isIncludeAllProduct;

    /**
     * 创建时间
     */
    private Timestamp createdAt;

    @Column(name = "start_date", type = Column.FieldType.KEY)
    private Date startDate;
    private Date endDate;
}
