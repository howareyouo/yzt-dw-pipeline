package cn.yizhi.yzt.pipeline.model.ods;



import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shop {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("main_shop_id")
    private Integer mainShopId;
    @JsonProperty("group_id")
    private Integer groupId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("abbr")
    private String abbr;

    @JsonProperty("business_scope")
    private Integer businessScope;

    @JsonProperty("management_mode")
    private Integer managementMode;

    @JsonProperty("logo")
    private String logo;
    @JsonProperty("wx_qr_code")
    private String wxQrCode;
    @JsonProperty("country")
    private String country;
    @JsonProperty("province")
    private String province;
    @JsonProperty("city")
    private String city;
    @JsonProperty("district")
    private String district;
    @JsonProperty("address")
    private String address;
    @JsonProperty("longitude")
    private Double longitude;
    @JsonProperty("latitude")
    private Double latitude;
    @JsonProperty("phone1")
    private String phone1;
    @JsonProperty("phone2")
    private String phone2;
    @JsonProperty("open_time")
    private String openTime;
    @JsonProperty("close_time")
    private String closeTime;
    @JsonProperty("status")
    private Integer status;
    @JsonProperty("access_status")
    private Integer accessStatus;
    @JsonProperty("profile")
    private String profile;
    @JsonProperty("story")
    private String story;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;

}
