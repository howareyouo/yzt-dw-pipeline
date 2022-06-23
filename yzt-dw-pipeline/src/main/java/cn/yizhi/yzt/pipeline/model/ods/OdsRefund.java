package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/7/1 19:50
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsRefund {
    @JsonProperty("id")
    private Long id;


    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("order_id")
    private Long orderId;
    //退款金额
    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;
    //退款状态
    @JsonProperty("status")
    private Integer status;
    //退款时间
    @JsonProperty("refunded_at")
    private Timestamp refundedAt;
}
