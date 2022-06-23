package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponTemplate {
	@JsonProperty("id")
	private Integer id;
	@JsonProperty("shop_id")
	private Integer shopId;
	/**
	 * 优惠劵名称
	 */
	@JsonProperty("coupon_name")
	private String couponName;

	/**
	 * 描述
	 */
	@JsonProperty("description")
	private String description;

	/**
	 * 优惠劵类型
	 */
	@JsonProperty("coupon_type")
	private int couponType;

	/**
	 * 发放总量
	 */
	@JsonProperty("issued_quantity")
	private Integer issuedQuantity;

	/**
	 * 剩余库存
	 */
	@JsonProperty("inventory")
	private Integer inventory;

	/**
	 * 已核销数
	 */
	@JsonProperty("verified_quantity")
	private Integer verifiedQuantity;

	/**
	 * 发放金额总量
	 */
	@JsonProperty("issued_amount")
	private BigDecimal issuedAmount;

	/**
	 * 劵开始时间
	 */
	@JsonProperty("start_time")
	private Timestamp startTime;

	/**
	 * 劵结束时间
	 */
	@JsonProperty("end_time")
	private Timestamp endTime;

	/**
	 * 优惠券状态 0:未开始，1:进行中，2:已结束，3:停止发券
	 */
	@JsonProperty("coupon_state")
	private int couponState;

	/**
	 * 是否可见 0:不可见，1:可见
	 */
	@JsonProperty("is_visible")
	private int isVisible;

	/**
	 * 0:不恢复投放，1：恢复投放
	 */
	@JsonProperty("is_recover_issued")
	private int isRecoverIssued;

	/**
	 * 优惠券优先级
	 */
	@JsonProperty("priority")
	private Integer priority;

	/**
	 * json value
	 */
	@JsonProperty("use_rule")
	private String useRule;

	/**
	 * 逻辑删除标志 0:未删除，1:已删除
	 */
	@JsonProperty("del_flag")
	private int delFlag;

	@JsonProperty("created_at")
	private Timestamp createdAt;

	@JsonProperty("updated_at")
	private Timestamp updatedAt;
}
