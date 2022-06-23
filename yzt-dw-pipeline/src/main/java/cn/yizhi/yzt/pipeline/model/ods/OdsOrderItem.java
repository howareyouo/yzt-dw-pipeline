package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * @author hucheng
 * @date 2020/6/20 20:48
 */
@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsOrderItem {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("order_no")
    private String orderNo;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("sku_id")
    private Integer skuId;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("weight")
    private BigDecimal weight;

    @JsonProperty("retail_price")
    private BigDecimal retailPrice;

    @JsonProperty("is_giveaway")
    private Boolean isGiveaway;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    /**
     * 是否需要展示电子凭证
     */
    @JsonProperty("show_voucher")
    private Boolean showVoucher;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    /**
     * 商品类型
     */
    @JsonProperty("product_type")
    private Integer productType;

    /**
     * 商品组合分类id
     */
    @JsonProperty("parent_id")
    private Integer parentId;

    /**
     * 商品实付
     */
    @JsonProperty("actual_amount")
    private BigDecimal actualAmount;
}