package cn.yizhi.yzt.pipeline.model.ods;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import cn.yizhi.yzt.pipeline.kafka.KafkaPojoDeserializationSchema;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Date;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsMemberUnion {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("member_id")
    private Integer memberId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("nickname")
    private String nickname;
    @JsonProperty("avatar")
    private String avatar;
    @JsonProperty("gender")
    private Integer gender;
    @JsonProperty("wechat")
    private String wechat;
    // kafka connect debezium解析date为int类型，需要特殊处理
    @JsonDeserialize(using = KafkaPojoDeserializationSchema.DateDeserializer.class)
    @JsonSerialize(using = KafkaPojoDeserializationSchema.Dateserialize.class)
    @JsonProperty("birthday")
    private Date birthday;
    @JsonProperty("province")
    private String province;
    @JsonProperty("city")
    private String city;
    @JsonProperty("main_shop_id")
    private Integer mainShopId;
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("staff_id")
    private Integer staffId;
    @JsonProperty("payment_password")
    private String paymentPassword;
    @JsonProperty("source")
    private String source;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("disabled")
    private Integer disabled;
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
    @JsonProperty("last_service_staff")
    private String lastServiceStaff;
    @JsonProperty("last_view_shop_time")
    private Timestamp lastViewShopTime;
    @JsonProperty("last_consignee_address")
    private String lastConsigneeAddress;
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

    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;

}
