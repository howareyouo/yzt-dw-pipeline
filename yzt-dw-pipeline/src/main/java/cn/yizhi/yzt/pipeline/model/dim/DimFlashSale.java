package cn.yizhi.yzt.pipeline.model.dim;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
public class DimFlashSale {

    @Column(name = "id", type = Column.FieldType.PK)
    private Integer id;

    @Column(name = "flash_sale_id", type = Column.FieldType.KEY)
    private Integer flashSaleId;

    private Integer shopId;

    /**
     * 活动主图
     */
    private String activityImg;

    /**
     * 活动编码：周期活动相同
     */
    private String activityCode;

    /**
     * 活动名称 活动名称
     */
    private String activityName;

    /**
     * 活动时间类型 0：固定时间活动，1:周期活动
     */
    private String activityRule;

    /**
     * 活动开始时间
     */
    private Timestamp startTime;

    /**
     * 活动结束时间, 最大时间时表示
     */
    private Timestamp endTime;

    /**
     * 周期活动时需要,当前周期
     */
    private Integer currentPeriod;

    /**
     * 周期活动时需要,周期数
     */
    private Integer periodSum;

    /**
     * 活动状态：0：未开始,1:预热中,2:进行中,3已结束
     */
    private String activityState;

    /**
     * 任务调度规则
     */
    private String cronRule;

    /**
     * 活动标签id
     */
    private Integer tagId;

    /**
     * 活动标签名称
     */
    private String tagName;

    /**
     * 预设人数
     */
    private Integer presetNum;

    /**
     * 参与门槛：0 否，1是
     */
    private Boolean onlyNew;

    /**
     * 活动限购：0 不限购，其他数：限购数
     */
    private Integer buyLimit;

    /**
     * 预热时间(小时)
     */
    private Integer preheatHour;

    /**
     * 提前提醒时间， 0：不提醒，其他则提醒
     */
    private Integer remindAheadMinute;

    /**
     * 自动取消订单，释放库存时间
     */
    private Integer lockInventorySetting;

    /**
     * 弹幕配置 0：不开启，1：预热中就开启,2：进行中才开启
     */
    private Integer barrageSetting;

    /**
     * 参与人数显示：0否，1显示
     */
    private String joinedShow;

    /**
     * 购买人数显示：0否，1显示
     */
    private String buyedShow;

    /**
     * 活动人气块显示：0否，1显示
     */
    private String popularShow;

    /**
     * 活动主页模版ID
     */
    private Integer indexTemplateId;

    /**
     * 抢购模版ID
     */
    private Integer rushTemplateId;

    /**
     * 需要给会员打的标签
     */
    private String memberTags;



    private Timestamp createdAt;


    /**
     * 是否删除：0否，1是
     */
    private Boolean disabled;

    @Column(name = "start_date", type = Column.FieldType.KEY)
    private Date startDate;
    private Date endDate;
}
