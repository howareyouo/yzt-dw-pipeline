package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author hucheng
 * 自提点地址
 * @date 2020/7/27 14:44
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsPickupAddress {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("province")
    private String province;

    @JsonProperty("city")
    private String city;

    @JsonProperty("district")
    private String district;

    @JsonProperty("address")
    private String address;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("contact_phone")
    private String contactPhone;
}
