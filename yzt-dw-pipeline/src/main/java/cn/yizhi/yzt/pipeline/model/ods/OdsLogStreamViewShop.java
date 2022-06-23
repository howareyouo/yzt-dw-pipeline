package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author hucheng
 * @date 2020/6/22 18:01
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsLogStreamViewShop {
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("member_id")
    private Integer memberId;
    @JsonProperty("device_id")
    private String deviceId;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("event_name")
    private String eventName;
    @JsonProperty("heart_beat")
    private Long heartBeat;
    @JsonProperty("event_time")
    private Long eventTime;
}
