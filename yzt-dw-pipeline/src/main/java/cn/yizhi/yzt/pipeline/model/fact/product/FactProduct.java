package cn.yizhi.yzt.pipeline.model.fact.product;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author hucheng
 * 累加型 从历史至今，修改和追加
 * @date 2020/6/26 11:28
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactProduct {
//    @JsonProperty("fk_shop")
//    private int fkShop;
//    @JsonProperty("fk_product")
//    private int fkProduct;
//    @JsonProperty("fk_sku")
//    private int fkSku;
//    @JsonProperty("fk_channel")
//    private int fkChannel;
    @JsonProperty("row_date")
    private String rowDate;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("sale_count")
    private Integer saleCount;

    //销售额
    @JsonProperty("sale_total")
    private BigDecimal saleTotal;
    //订单数
    @JsonProperty("order_count")
    private Integer orderCount;
    //下单人数
    @JsonProperty("order_number")
    private Integer orderNumber;

    //付款单数
    @JsonProperty("pay_count")
    private Integer payCount;
    //付款人数
    @JsonProperty("pay_Number")
    private Integer payNumber;
    //剩余库存
    @JsonProperty("inventory")
    private Integer inventory;
}
