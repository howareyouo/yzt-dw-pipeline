package cn.yizhi.yzt.pipeline.model.ods;


import cn.yizhi.yzt.pipeline.kafka.KafkaPojoDeserializationSchema;
import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberBase {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("main_shop_id")
    private Integer mainShopId;
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
    @JsonProperty("province")
    private String province;
    @JsonProperty("city")
    private String city;

    // kafka connect debezium解析date为int类型，需要特殊处理
    @JsonDeserialize(using = KafkaPojoDeserializationSchema.DateDeserializer.class)
    @JsonProperty("birthday")
    private Date birthday;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;

  
}
