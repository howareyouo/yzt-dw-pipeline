package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

/**
 * @author hucheng
 * @date 2020/7/1 19:40
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsDeliveryPackage {
    @JsonProperty("id")
    private BigInteger id;

    @JsonProperty("main_shop_id")
    private Integer mainShopId;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("order_id")
    private Integer orderId;
    //收货人
    @JsonProperty("consignee")
    private String consignee;
    //收货人电话
    @JsonProperty("consignee_phone")
    private String consigneePhone;
    //收货人地址
    @JsonProperty("consignee_address")
    private String consigneeAddress;
}
