package cn.yizhi.yzt.pipeline.model.ods;

import cn.yizhi.yzt.pipeline.kafka.KafkaPojoDeserializationSchema;
import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/6/28 11:19
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsLogStream {
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("main_shop_id")
    private Integer mainShopId;
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("goods_id")
    private Integer goodsId;
    @JsonProperty("taro_env")
    private String taroEnv;
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
    @JsonProperty("event_time")
    private Timestamp eventTime;

    @JsonProperty("url")
    private String url;
    @JsonProperty("query")
    private String query;
    @JsonProperty("keyword")
    private String keyword;
    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("promotion_type")
    private Integer promotionType;
    @JsonProperty("promotion_id")
    private Integer promotionId;

    //直播新增
    /**
     * 直播间id
     */
    @JsonProperty("live_room_id")
    private Integer liveRoomId;

    /**
     * 进入直播间的渠道
     */
    @JsonProperty("source")
    private String source;

    /**
     * 会员姓名
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * 手机号
     */
    @JsonProperty("user_phone")
    private String userPhone;

    /**
     * 商品名称
     */
    @JsonProperty("goods_name")
    private String goodsName;

    /**
     * 优惠劵id
     */
    @JsonProperty("coupon_template_id")
    private Integer couponTemplateId;

    /**
     * 优惠劵名称
     */
    @JsonProperty("coupon_name")
    private String couponName;

    //浏览商品新增字段
    /**
     * 浏览商品事件进入和离开的事件关联id
     */
    @JsonProperty("uuid")
    private String uuid;


    /**
     * 浏览商品开始时间
     */
    @JsonProperty("begin_time")
    private Long beginTime;


    /**
     * 浏览商品结束时间
     */
    @JsonProperty("end_time")
    private Long endTime;


    //领取优惠劵新增
    /**
     * 领取优惠劵类型
     */
    @JsonProperty("product_type")
    private String productType;


    //拼团新增
    /**
     * 头像
     */
    @JsonProperty("avatar")
    private String avatar;

    /**
     * 分享优惠劵类型
     */
    @JsonDeserialize(using = KafkaPojoDeserializationSchema.CouponTypeDeserializer.class)
    @JsonProperty("coupon_type")
    private Integer couponType;

}
