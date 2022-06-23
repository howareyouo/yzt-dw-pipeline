package cn.yizhi.yzt.pipeline.model.ods;


import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FullReducePromotion {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("name")
    private String name;
    /**
     * 商品规则
     */
    @JsonProperty("product_rule")
    private String productRule;

    /**
     *优惠规则
     */
    @JsonProperty("discount_rule")
    private String discountRule;

    /**
     * 参与限制
     */
    @JsonProperty("participate_rule")
    private String participateRule;

    /**
     * 仅限新客参与:0-否 1-是
     */
    @JsonProperty("for_first_member")
    private Integer forFirstMember;

    /**
     *开始时间
     */
    @JsonProperty("start_time")
    private Timestamp startTime;

    /**
     * 结束时间
     */
    @JsonProperty("end_time")
    private Timestamp endTime;

    /**
     * 优惠券状态 0:未开始，1:进行中，2:已结束
     */
    @JsonProperty("state")
    private Integer state;

    /**
     * 逻辑删除标志 0:未删除，1:已删除
     */
    @JsonProperty("del_flag")
    private Integer delFlag;

    /**
     * 优惠券规则修改时间
     */
    @JsonProperty("discount_rule_updated_at")
    private Timestamp discountRuleUpdatedAt;

    /**
     * 是否长期活动
     */
    @JsonProperty("is_long_term")
    private Boolean isLongTerm;

    /**
     * 是否包含全部商品
     */
    @JsonProperty("is_include_all_product")
    private Boolean isIncludeAllProduct;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Timestamp createdAt;

    /**
     * 修改时间
     */
    @JsonProperty("updated_at")
    private Timestamp updatedAt;

}
