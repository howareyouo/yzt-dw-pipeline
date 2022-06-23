package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * @author aorui created on 2020/12/15
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactGroupBuyPushMessage {

    /**
     * 商铺id
     */
    private Integer shopId;

    /**
     * 活动id
     */
    private Integer promotionId;

    /**
     * 拼团活动记录id
     */
    private Integer groupPromotionInstanceId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userPhoto;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 痕迹描述(内容、价格)
     */
    private String desc;

    /**
     * 记录时间
     */
    private Timestamp recordTime;

    /**
     * 记录类型
     */
    private Integer recordType;
}
