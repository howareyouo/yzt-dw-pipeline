package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author aorui created on 2020/11/2
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsFullReducePromotionOrderItem {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("full_reduce_promotion_order_id")
    private Long fullReducePromotionOrderId;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("sku_id")
    private Integer skuId;

    @JsonProperty("retail_price")
    private BigDecimal retailPrice;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

}
