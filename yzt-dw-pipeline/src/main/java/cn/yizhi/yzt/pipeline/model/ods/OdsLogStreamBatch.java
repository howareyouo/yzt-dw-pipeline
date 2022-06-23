package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * 2 * @Author: HuCheng
 * 3 * @Date: 2020/11/18 20:46
 * 4
 */
@Data
public class OdsLogStreamBatch {
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("goods_id")
    private Integer goodsId;
    @JsonProperty("taro_env")
    private String taroEnv;
    @JsonProperty("device_id")
    private String deviceId;
    @JsonProperty("event_name")
    private String eventName;
    @JsonProperty("event_time")
    private Timestamp eventTime;


    @JsonProperty("promotion_type")
    private Integer promotionType;
    @JsonProperty("promotion_id")
    private Integer promotionId;

}
