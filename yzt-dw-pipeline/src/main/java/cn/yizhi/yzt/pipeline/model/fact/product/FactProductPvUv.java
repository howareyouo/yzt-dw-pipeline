package cn.yizhi.yzt.pipeline.model.fact.product;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author aorui created on 2020/11/2
 */
@Getter
@Setter
public class FactProductPvUv {
    @JsonProperty("row_date")
    private String rowDate;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("channel")
    private String channel;


    @JsonProperty("pv")
    private Integer pv;
    @JsonProperty("uv")
    private Integer uv;

    @JsonProperty("share_count")
    private Integer shareCount;
    //加入购物车的数量
    @JsonProperty("addcart_count")
    private Integer addcartCount;
    // 加入购物车的人数
    @JsonProperty("addcart_number")
    private Integer addcartNumber;
    //分享人数
    @JsonProperty("share_number")
    private Integer shareNumber;

}
