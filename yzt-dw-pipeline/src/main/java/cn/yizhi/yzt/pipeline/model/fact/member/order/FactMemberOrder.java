package cn.yizhi.yzt.pipeline.model.fact.member.order;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;


@Getter
@Setter
@ToString
public class FactMemberOrder {

    /**
     * 处理时间
     */
    @JsonProperty("analysis_date")
    private String analysisDate;

    /**
     * 店铺ID
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 会员id
     */
    @JsonProperty("member_id")
    private Integer memberId;


    /**
     * 下单次数
     */
    @JsonProperty("order_count")
    private int orderCount;

    /**
     * 下单金额
     */
    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    /**
     * 付款订单数
     */
    @JsonProperty("paid_count")
    private int paidCount;

    /**
     * 付款金额
     */
    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;

    /**
     * 退款数
     */
    @JsonProperty("refund_count")
    private int refundCount;

    /**
     * 退款总额
     */
    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;


    /**
     * 订单id
     */
    @JsonProperty("order_ids")
    private String orderIds;


    /**
     * 退款订单id
     */
    @JsonProperty("refund_ids")
    private String refundIds;


    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;
}
