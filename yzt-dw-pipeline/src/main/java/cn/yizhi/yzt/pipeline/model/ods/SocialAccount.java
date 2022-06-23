package cn.yizhi.yzt.pipeline.model.ods;


import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialAccount {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("shop_member_id")
    private Integer shopMemberId;
    @JsonProperty("open_id")
    private String openId;
    @JsonProperty("union_id")
    private String unionId;
    @JsonProperty("member_base_id")
    private String memberBaseId;
    @JsonProperty("user_id")
    private String userId;
    private String appid;
//    @JsonProperty("created_at")
//    private Timestamp createdAt;
//    @JsonProperty("updated_at")
//    private Timestamp updatedAt;
}
