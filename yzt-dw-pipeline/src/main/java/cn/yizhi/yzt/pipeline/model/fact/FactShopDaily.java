package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * 店铺当天实时数据 和小时级参数相同
 * @date 2020/6/26 11:32
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactShopDaily {
    @JsonProperty("shopId")
    private int shopId;
    @JsonProperty("row_date")
    private Timestamp rowDate;
    @JsonProperty("channel")
    private String  channel;

    @JsonProperty("pv")
    private Integer pv;
    @JsonProperty("uv")
    private Integer uv;
    //加购次数
    @JsonProperty("addcart_count")
    private  Integer addcartCount;
    //加购人数
    @JsonProperty("addcart_number")
    private  Integer addcartNumber;
    @JsonProperty("share_count")
    private  Integer shareCount;
    @JsonProperty("share_number")
    private  Integer shareNumber;
    @JsonProperty("order_count")
    private  Integer orderCount;
    @JsonProperty("order_number")
    private Integer orderNumber;
    @JsonProperty("pay_count")
    private Integer payCount;
    @JsonProperty("pay_number")
    private Integer payNumber;

    //商品销量，不含退款
    @JsonProperty("sale_count")
    private Integer saleCount;
    //支付总额
    @JsonProperty("pay_amount")
    private BigDecimal payAmount;
    @JsonProperty("order_amount")
    private BigDecimal orderAmount;
    //退款单数
    @JsonProperty("refund_count")
    private BigDecimal refundCount;
    //退款金额
    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;
}
