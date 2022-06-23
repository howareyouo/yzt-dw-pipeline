package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlashSaleActivity {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 活动主图
     */
    @JsonProperty("activity_img")
    private String activityImg;

    /**
     * 活动编码：周期活动相同
     */
    @JsonProperty("activity_code")
    private String activityCode;

    /**
     * 活动名称 活动名称
     */
    @JsonProperty("activity_name")
    private String activityName;

    /**
     * 活动时间类型 0：固定时间活动，1:周期活动
     */
    @JsonProperty("activity_rule")
    private int activityRule;

    /**
     * 活动开始时间
     */
    @JsonProperty("start_time")
    private Timestamp startTime;

    /**
     * 活动结束时间, 最大时间时表示
     */
    @JsonProperty("end_time")
    private Timestamp endTime;

    /**
     * 周期活动时需要,当前周期
     */
    @JsonProperty("current_period")
    private Integer currentPeriod;

    /**
     * 周期活动时需要,周期数
     */
    @JsonProperty("period_sum")
    private Integer periodSum;

    /**
     * 活动状态：0：未开始,1:预热中,2:进行中,3已结束
     */
    @JsonProperty("activity_state")
    private int activityState;

    /**
     * 任务调度规则
     */
    @JsonProperty("cron_rule")
    private String cronRule;

    /**
     * 活动标签id
     */
    @JsonProperty("tag_id")
    private Integer tagId;

    /**
     * 活动标签名称
     */
    @JsonProperty("tag_name")
    private String tagName;

    /**
     * 预设人数
     */
    @JsonProperty("preset_num")
    private Integer presetNum;

    /**
     * 参与门槛：0 否，1是
     */
    @JsonProperty("only_new")
    private Boolean onlyNew;

    /**
     * 活动限购：0 不限购，其他数：限购数
     */
    @JsonProperty("buy_limit")
    private Integer buyLimit;

    /**
     * 预热时间(小时)
     */
    @JsonProperty("preheat_hour")
    private Integer preheatHour;

    /**
     * 提前提醒时间， 0：不提醒，其他则提醒
     */
    @JsonProperty("remind_ahead_minute")
    private Integer remindAheadMinute;

    /**
     * 自动取消订单，释放库存时间
     */
    @JsonProperty("lock_inventory_setting")
    private Integer lockInventorySetting;

    /**
     * 弹幕配置 0：不开启，1：预热中就开启,2：进行中才开启
     */
    @JsonProperty("barrage_setting")
    private Integer barrageSetting;

    /**
     * 参与人数显示：0否，1显示
     */
    @JsonProperty("joined_show")
    private Integer joinedShow;

    /**
     * 购买人数显示：0否，1显示
     */
    @JsonProperty("buyed_show")
    private Integer buyedShow;

    /**
     * 活动人气块显示：0否，1显示
     */
    @JsonProperty("popular_show")
    private Integer popularShow;

    /**
     * 活动主页模版ID
     */
    @JsonProperty("index_template_id")
    private Integer indexTemplateId;

    /**
     * 抢购模版ID
     */
    @JsonProperty("rush_template_id")
    private Integer rushTemplateId;

    /**
     * 需要给会员打的标签
     */
    @JsonProperty("member_tags")
    private String memberTags;


    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    /**
     * 库存数据版本
     */
    @JsonProperty("version")
    private int version;

    /**
     * 是否删除：0否，1是
     */
    @JsonProperty("disabled")
    private Boolean disabled;

}
