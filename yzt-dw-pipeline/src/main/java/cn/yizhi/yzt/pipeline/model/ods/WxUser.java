package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WxUser {
    @JsonProperty("id")
    private Integer id;

    //是否关注. 1是, 0否
    @JsonProperty("subscribed")
    private Integer subscribed;

    //是否被加入黑名单. 1是, 0否
    @JsonProperty("blacked")
    private Integer blacked;

    @JsonProperty("appid")
    private String appid;

    @JsonProperty("openid")
    private String openid;

//    @JsonProperty("subscribe_time")
//    private Timestamp subscribeTime;
}
