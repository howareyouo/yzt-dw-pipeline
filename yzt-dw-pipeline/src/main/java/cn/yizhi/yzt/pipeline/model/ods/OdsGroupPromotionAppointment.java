package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * 用户预约表
 * @author aorui created on 2020/12/16
 */
@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsGroupPromotionAppointment {
    /**
     * id
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * '创建时间'
     */
    @JsonProperty("created_at")
    private Timestamp createdAt;
    /**
     * ''更新时间''
     */
    @JsonProperty("updated_at")
    private Timestamp updatedAt;
    /**
     * ''是否删除 0-否 1-是''
     */
    @JsonProperty("deleted")
    private Integer deleted;
    /**
     * ''店铺id''
     */
    @JsonProperty("shop_id")
    private Integer shopId;
    /**
     * ''拼团活动记录id''
     */
    @JsonProperty("group_promotion_id")
    private Integer groupPromotionId;
    /**
     * ''会员id(参与人id)''
     */
    @JsonProperty("member_id")
    private Integer memberId;
    /**
     * ''参与人名字''
     */
    @JsonProperty("member_name")
    private String memberName;
    /**
     * ''手机号''
     */
    @JsonProperty("phone")
    private String phone;
    /**
     * ''参与人头像''
     */
    @JsonProperty("member_avatar")
    private String memberAvatar;
    /**
     * ''预约时间''
     */
    @JsonProperty("appoint_at")
    private Timestamp appointAt;
    /**
     * ''是否提醒1-已提醒''
     */
    @JsonProperty("is_remind")
    private Integer isRemind;

}
