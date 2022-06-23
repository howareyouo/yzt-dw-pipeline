package cn.yizhi.yzt.pipeline.model.ods;


import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    //商品编码
    @JsonProperty("serial")
    private String serial;
    //商品描述
    @JsonProperty("description")
    private String description;
    //分享描述
    @JsonProperty("share_desc")
    private String shareDesc;
     //商品详情
     @JsonProperty("detail")
     private String detail;
    //封面图片
    @JsonProperty("image")
    private String image;
    @JsonProperty("video")
    private String video;
    // 标识是否有sku,没有的话,创建默认的sku
    @JsonProperty("has_sku")
    private Boolean hasSku;
    // 商品类型 1-实物 2-虚拟 3-电子卡券
    @JsonProperty("product_type")
    private int productType;
    // 划线价
    @JsonProperty("marking_price")
    private BigDecimal markingPrice;
    @JsonProperty("weight")
    private BigDecimal weight;
    @JsonProperty("shop_id")
    private int shopId; // 门店id
    //上架状态 单个字段无法标识商品是否处于上架中
    @JsonProperty("status")
    private int status;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;

}
