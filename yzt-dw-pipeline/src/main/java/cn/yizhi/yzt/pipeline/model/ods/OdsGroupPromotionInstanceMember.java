package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 拼团活动参与人
 *
 * @author aorui created on 2020/12/16
 */
@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsGroupPromotionInstanceMember {

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
    @JsonProperty("group_promotion_instance_id")
    private Integer groupPromotionInstanceId;
    /**
     * ''会员id(参与人id) 机器人统一为0''
     */
    @JsonProperty("member_id")
    private Integer memberId;
    /**
     * ''参与人头像''
     */
    @JsonProperty("member_photo")
    private String memberPhoto;
    /**
     * ''参与人名字/手机号''
     */
    @JsonProperty("member_name")
    private String memberName;
    /**
     * ''是否预约 0-否 1-是''
     */
    @JsonProperty("is_reserved")
    private Integer isReserved;
    /**
     * ''是否为团长 0-否 1-是''
     */
    @JsonProperty("is_leader")
    private Integer isLeader;
    /**
     * ''是否已付款 0-否 1-是 机器人统一为1''
     */
    @JsonProperty("is_paid")
    private Integer isPaid;
    /**
     * ''是否退款失败 0-否 1-是''
     */
    @JsonProperty("is_refund_failure")
    private Integer isRefundFailure;
    /**
     * ''是否参团失败 0-否 1-是''
     */
    @JsonProperty("is_joind_failure")
    private Integer isJoindFailure;
    /**
     * ''分享人id(会员id)''
     */
    @JsonProperty("share_member_id")
    private Integer shareMemberId;
    /**
     * ''原始分享人id(员工id)''
     */
    @JsonProperty("share_employee_id")
    private Integer shareEmployeeId;
    /**
     * ''订单id 机器人统一为0''
     */
    @JsonProperty("order_id")
    private Integer orderId;
    /**
     * ''订单号 机器人统一为0''
     */
    @JsonProperty("order_no")
    private String OrderNo;

    /**
     * 订单状态
     */
    @JsonProperty("order_status")
    private Integer orderStatus;

    @JsonProperty("refund_status")
    private Integer refundStatus;

    /**
     * 拼团活动id
     */
    @JsonProperty("group_promotion_id")
    private Integer groupPromotionId;

    /**
     * 默认 0 是否是新客户
     */
    @JsonProperty("is_new_member")
    private Integer isNewMember;

    /**
     * 订单实付金额
     */
    @JsonProperty("actual_order_amount")
    private BigDecimal actualOrderAmount;

    /**
     * 受益人（员工)的昵称
     */
    @JsonProperty("staff_nickname")
    private String staffNickname;

    /**
     * 受益人（员工)的昵称
     */
    @JsonProperty("staff_phone")
    private String staffPhone;

    /**
     * 受益人（员工)的ID
     */
    @JsonProperty("staff_id")
    private Integer staffId;
}
