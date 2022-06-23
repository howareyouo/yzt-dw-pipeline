package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/6/22 17:46
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactLogStream {
    //维度外键，区别于业务外键
    @JsonProperty("fk_shop")
    private Integer fkShop;//店铺主键
    @JsonProperty("fk_member")
    private Integer fkMember;//会员主键
    @JsonProperty("fk_date")
    private Integer fkDate;//日期外键
    @JsonProperty("fk_product")
    private Integer fkProduct;//商品主键 sku
    @JsonProperty("fk_promotion")
    private Integer fkPromotion;//优惠劵主键
    @JsonProperty("fk_channel")
    private Integer fkChannel;//渠道主键

    @JsonProperty("device_id")
    private String deviceId;
    @JsonProperty("open_id")
    private String openId;
    @JsonProperty("device_model")
    private String deviceModel;
    @JsonProperty("device_brand")
    private String deviceBrand;
    @JsonProperty("system_name")
    private String systemName;
    @JsonProperty("system_version")
    private String systemVersion;
    @JsonProperty("app_version")
    private String appVersion;
    @JsonProperty("event_name")
    private String eventName;
    @JsonProperty("url")
    private String url;
    @JsonProperty("query")
    private String query;
    @JsonProperty("keyword")
    private String keyword;
    @JsonProperty("quantity")
    private Integer quantity;
    @JsonProperty("event_time")
    private Timestamp eventTime;
}
