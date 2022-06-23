package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSkuExtend {

    @JsonProperty("id")
    private int id;

    /**
     * 活动库存
     */
    @JsonProperty("activity_inventory")
    private int activityInventory;

    /**
     * 库存
     */
    @JsonProperty("inventory")
    private int inventory;

    /**
     * 线下库存
     */
    @JsonProperty("warehouse_inventory")
    private int warehouseInventory;

    /**
     * 销售量
     */
    @JsonProperty("sold_count")
    private int soldCount;

    /**
     * 限购数
     */
    @JsonProperty("buy_limit")
    private int buyLimit;

    @JsonProperty("purchase_price")
    private BigDecimal purchasePrice;

    @JsonProperty("retail_price")
    private BigDecimal retailPrice;

    @JsonProperty("product_id")
    private int productId;

    /**
     * 商品SKUID
     */
    @JsonProperty("product_sku_id")
    private int productSkuId;

    /**
     * 商品活动编号
     */
    @JsonProperty("promotion_id")
    private int promotionId;

    /**
     * 活动类型
     */
    @JsonProperty("promotion_type")
    private int promotionType;

    @JsonProperty("version")
    private int version;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;

}
