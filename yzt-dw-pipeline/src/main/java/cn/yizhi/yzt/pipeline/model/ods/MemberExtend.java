package cn.yizhi.yzt.pipeline.model.ods;


import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberExtend {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("shop_member_id")
    private Integer shopMemberId;
    @JsonProperty("balance_amount")
    private BigDecimal balanceAmount;
    @JsonProperty("gift_amount")
    private BigDecimal giftAmount;
    @JsonProperty("points")
    private BigDecimal points;
    @JsonProperty("debt_amount")
    private BigDecimal debtAmount;
    @JsonProperty("consume_times")
    private Integer consumeTimes;
    @JsonProperty("consume_amount")
    private BigDecimal consumeAmount;
    @JsonProperty("last_consume_date")
    private Timestamp lastConsumeDate;
    @JsonProperty("last_consume_services")
    private String lastConsumeServices;
    @JsonProperty("last_services_staff")
    private String lastServiceStaff;
    @JsonProperty("referrer")
    private String referrer;
    @JsonProperty("follower")
    private String follower;
    @JsonProperty("custom1")
    private String custom1;
    @JsonProperty("custom2")
    private String custom2;
    @JsonProperty("custom3")
    private String custom3;
    @JsonProperty("custom4")
    private String custom4;
    @JsonProperty("custom5")
    private String custom5;
    @JsonProperty("custom6")
    private String custom6;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    //最后一次浏览时间
    @JsonProperty("last_view_shop_time")
    private Timestamp lastViewShopTime;

    //最后一次收货地址
    @JsonProperty("last_consignee_address")
    private String  lastConsigneeAddress;
}
