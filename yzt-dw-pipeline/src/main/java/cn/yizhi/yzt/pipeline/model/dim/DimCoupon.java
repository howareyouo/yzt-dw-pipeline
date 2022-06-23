package cn.yizhi.yzt.pipeline.model.dim;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
public class DimCoupon {
    @Column(name = "id", type = Column.FieldType.PK)
    private Integer id;

    @Column(name = "coupon_id", type = Column.FieldType.KEY)
    private Integer couponId;

    private Integer shopId;

    /**
     * 优惠劵名称
     */
    private String couponName;

    /**
     * 描述
     */
    private String description;

    /**
     * 优惠劵类型 0 满减劵    1 折扣劵      2 随机金额优惠劵       3 包邮劵
     */
    private String couponType;

    /**
     * 发放总量
     */
    private Integer issuedQuantity;


    /**
     * 发放金额总量
     */
    private BigDecimal issuedAmount;

    /**
     * 劵开始时间
     */
    private Timestamp startTime;

    /**
     * 劵结束时间
     */
    private Timestamp endTime;

    /**
     * 优惠券状态 0:未开始，1:进行中，2:已结束，3:停止发券
     */
    private String couponState;

    /**
     * 是否可见 0:不可见，1:可见
     */
    private String isVisible;

    /**
     * 0:不恢复投放，1：恢复投放
     */
    private String isRecoverIssued;

    /**
     * 优惠券优先级
     */
    private Integer priority;

    /**
     * json value
     */
    private String useRule;

    /**
     * 逻辑删除标志 0:未删除，1:已删除
     */
    private String delFlag;

    private Timestamp createdAt;

    @Column(name = "start_date", type = Column.FieldType.KEY)
    private Date startDate;
    private Date endDate;
}
