package cn.yizhi.yzt.pipeline.model.fact.member.coupon;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactMemberCouponLog {

    /**
     * 处理时间
     */
    @JsonProperty("analysis_date")
    private String analysisDate;

    /**
     * 店铺ID
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 会员id
     */
    @JsonProperty("member_id")
    private Integer memberId;

    /**
     * 优惠券模板id
     */
    @JsonProperty("coupon_template_id")
    private Integer couponTemplateId;


    /**
     * 分享次数
     */
    @JsonProperty("share_times")
    private Integer shareTimes;

    /**
     * * 优惠劵类型 0 满减劵    1 折扣劵      2 随机金额优惠劵       3 包邮劵
     */
    @JsonProperty("coupon_type")
    private int couponType;



    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;
}
