package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/10/24 14:59
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsFlashSaleAppointment {

    /**
     * id
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * shop_id
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 被提醒用户id
     */
    @JsonProperty("user_id")
    private Integer userId;

    /**
     * 用户名称
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * 活动id
     */
    @JsonProperty("activity_id")
    private Integer activityId;

    /**
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;

    /**
     * 提醒内容
     */
    @JsonProperty("remind_content")
    private String remindContent;

    /**
     * 提醒类别:activity活动、product商品
     */
    @JsonProperty("remind_type")
    private String remindType;

    /**
     * 提醒优先级
     */
    @JsonProperty("priority")
    private Integer priority;

    /**
     * 提醒次数
     */
    @JsonProperty("number_of_reminders")
    private Integer numberOfReminders;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    /**
     * 删除标志
     */
    @JsonProperty("__deleted")
    private Boolean __deleted;

}
