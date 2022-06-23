package cn.yizhi.yzt.pipeline.model.ods;


import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSku {
    @JsonProperty("id")
    private Integer id;

    /**
     * 是否为默认sku 单规格商品则新增默认sku
     */
    @JsonProperty("default_sku")
    private Boolean defaultSku;

    /**
     * sku封面
     */
    @JsonProperty("image")
    private String image;

    /**
     * 库存
     */
    @JsonProperty("inventory")
    private Integer inventory = 0;

    /**
     * 线下库存
     */
    @JsonProperty("warehouse_inventory")
    private Integer warehouseInventory = 0;

    /**
     * 销售量
     */
    @JsonProperty("sold_count")
    private Integer soldCount = 0;

    /**
     * 采购价
     */
    @JsonProperty("purchase_price")
    private BigDecimal purchasePrice;

    /**
     * 售价
     */
    @JsonProperty("retail_price")
    private BigDecimal retailPrice;

    /**
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;
    @JsonProperty("version")
    private Integer version;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;

}
