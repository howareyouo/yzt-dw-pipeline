package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @Author: HuCheng
 * 拼团活动开团记录
 * @Date: 2020/12/14 15:59
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsGroupPromotionInstance {
    @JsonProperty("id")
    private Integer id;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Timestamp createdAt;

    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    /**
     * 是否删除 0-否 1-是
     */
    @JsonProperty("deleted")
    private Integer deleted;

    /**
     * 店铺id
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 拼团活动id
     */
    @JsonProperty("group_promotion_id")
    private Integer groupPromotionId;

    /**
     * 拼团状态 1-待成团 2-拼团成功 3-拼团失败
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * 是否机器人成团 0-否 1-是
     */
    @JsonProperty("has_robot")
    private Integer hasRobot;

    /**
     * 拼团价
     */
    @JsonProperty("group_price")
    private BigDecimal groupPrice;

    /**
     * 成团人数
     */
    @JsonProperty("threshold")
    private Integer threshold;

    /**
     * 开团有效期(小时)
     */
    @JsonProperty("expires_in")
    private Integer expiresIn;

    /**
     * 模拟成团时间(小时)(为NULL表示没有开启)
     */
    @JsonProperty("autoCreatedIn")
    private Integer auto_created_in;

    /**
     * 机器人开团数(为NULL表示没有开启)
     */
    @JsonProperty("group_robot")
    private Integer groupRobot;

    /**
     * 是否允许成团前退款 0-否 1-是
     */
    @JsonProperty("enabled_refund")
    private Integer enabledRefund;

    /**
     * 至少包含的新客数(为NULL表示没有开启)
     */
    @JsonProperty("customer_limit")
    private Integer customerLimit;

    /**
     * 订单超时时间(分钟)
     */
    @JsonProperty("order_expires_in")
    private Integer orderExpiresIn;
}
