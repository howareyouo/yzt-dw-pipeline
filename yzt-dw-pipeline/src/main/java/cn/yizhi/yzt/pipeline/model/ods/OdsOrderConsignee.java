package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;


/**
 * @author hucheng
 * @date 2020/7/2 15:06
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsOrderConsignee {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("order_id")
    private Long orderId;
    //国家
    @JsonProperty("country")
    private String country;
    //省
    @JsonProperty("province")
    private String province;
    //市
    @JsonProperty("city")
    private String city;
    //区县
    @JsonProperty("district")
    private String district;
    //收货人地址
    @JsonProperty("address")
    private String address;

    //自提id
    @JsonProperty("pickup_address_id")
    private Integer pickupAddressId;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;
}
