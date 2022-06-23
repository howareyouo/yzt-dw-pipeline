package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopGroup {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("main_shop_id")
    private Integer mainShopId;
    @JsonProperty("group_name")
    private String groupName;
    @JsonProperty("group_mode")
    private Integer groupMode;
    @JsonProperty("support_shop_price")
    private Integer supportShopPrice;
    @JsonProperty("support_cross_consume")
    private Integer supportCrossConsume;
    @JsonProperty("consumer_pickup_product")
    private Integer consumePickupProduct;
    @JsonProperty("consumer_virtual_product")
    private Integer consumeVirtualProduct;
    @JsonProperty("consumer_electronic_card")
    private Integer consumeElectronicCard;
    @JsonProperty("consume_coupon")
    private Integer consumeCoupon;
}
