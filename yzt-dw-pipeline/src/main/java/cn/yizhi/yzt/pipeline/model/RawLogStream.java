package cn.yizhi.yzt.pipeline.model;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author hucheng
 * @date 2020/6/28 11:19
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawLogStream {
    @JsonProperty("shopId")
    private Integer shopId;
    @JsonProperty("mainShopId")
    private Integer mainShopId;
    @JsonProperty("userId")
    private Integer userId;
    @JsonProperty("goodsId")
    private Integer goodsId;
    @JsonProperty("taroEnv")
    private String taroEnv;

    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("openId")
    private String openId;
    @JsonProperty("deviceModel")
    private String deviceModel;
    @JsonProperty("deviceBrand")
    private String deviceBrand;
    @JsonProperty("systemName")
    private String systemName;
    @JsonProperty("systemVersion")
    private String systemVersion;
    @JsonProperty("appVersion")
    private String appVersion;
    @JsonProperty("eventName")
    private String eventName;
    @JsonProperty("eventTime")
    private Long eventTime;

    @JsonProperty("url")
    private String url;
    @JsonProperty("query")
    private String query;
    @JsonProperty("keyword")
    private String keyword;
    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("couponTemplateId")
    private Integer couponTemplateId;

    @JsonProperty("activityId")
    private Integer activityId;

    @JsonProperty("activityType")
    private String activityType;

    //直播新增
    /**
     * 进入直播来源 share 分享  click 点击直接进入
     */
    @JsonProperty("source")
    private String source;

    /**
     * 商品名称
     */
    @JsonProperty("goodsName")
    private String goodsName;

    /**
     * 会员名称
     */
    @JsonProperty("userName")
    private String userName;

    /**
     * 会员手机号
     */
    @JsonProperty("userPhone")
    private String userPhone;

    /**
     * 直播间id
     */
    @JsonProperty("liveRoomId")
    private Integer liveRoomId;

    //浏览商品新增字段
    /**
     * 浏览商品事件进入和离开的事件关联id
     */
    @JsonProperty("uuid")
    private String uuid;

    /**
     * 浏览商品开始时间
     */
    @JsonProperty("beginTime")
    private Long beginTime;


    /**
     * 浏览商品结束时间
     */
    @JsonProperty("endTime")
    private Long endTime;

    //统计优惠劵领取新增

    /**
     * 领取优惠劵类型
     */
    @JsonProperty("productType")
    private  String productType;

    /**
     * 分享优惠劵类型
     */
    @JsonProperty("couponType")
    private Integer couponType;

    //拼团新增

    /**
     * 头像
     */
    @JsonProperty("avatar")
    private String avatar;
}
