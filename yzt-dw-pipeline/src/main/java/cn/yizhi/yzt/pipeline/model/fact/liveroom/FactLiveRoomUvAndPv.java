package cn.yizhi.yzt.pipeline.model.fact.liveroom;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author hucheng
 * @date 2020/8/10 15:16
 */
@Data
public class FactLiveRoomUvAndPv {
    /**
     * 店铺id
     */
    @JsonProperty("shop_id")
    private Integer shopId;
    /**
     * 直播间id
     */
    @JsonProperty("live_room_id")
    private Integer liveRoomId;

    /**
     * pv
     */
    @JsonProperty("pv")
    private Long pv;

    /**
     * uv
     */
    @JsonProperty("uv")
    private Long uv;
}
