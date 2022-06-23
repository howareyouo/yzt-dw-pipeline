package cn.yizhi.yzt.pipeline.model.fact.liveroom;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author hucheng
 * @date 2020/8/11 16:16
 */
@Data
public class FactLiveRoomProductOrder {
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
     * 直播商品销售额
     */
    @JsonProperty("product_amount")
    private BigDecimal productAmount;

    /**
     * 直播商品销量
     */
    @JsonProperty("product_count")
    private Integer productCount;
}
