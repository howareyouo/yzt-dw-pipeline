package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author aorui created on 2020/10/23
 */
@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coupon {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("coupon_template_id")
    private Integer couponTemplateId;

    @JsonProperty("member_id")
    private Integer memberId;

    @JsonProperty("member_name")
    private String memberName;

    @JsonProperty("member_phone")
    private String memberPhone;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("coupon_type")
    private Integer couponType;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    @JsonProperty("start_time")
    private Timestamp startTime;

    @JsonProperty("end_time")
    private Timestamp endTime;

    @JsonProperty("source")
    private String source;

    @JsonProperty("verification_type")
    private Integer verificationType;

    @JsonProperty("can_return")
    private Integer canReturn;

    @JsonProperty("state")
    private Integer state;

    @JsonProperty("used_at")
    private Timestamp usedAt;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updateAt;

}
