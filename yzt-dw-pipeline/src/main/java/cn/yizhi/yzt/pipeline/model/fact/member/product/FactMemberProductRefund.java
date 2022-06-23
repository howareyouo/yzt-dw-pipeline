package cn.yizhi.yzt.pipeline.model.fact.member.product;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @Author: HuCheng
 * @Date: 2021/1/7 16:21
 */
@Getter
@Setter
@ToString
public class FactMemberProductRefund {
    /**
     * 处理时间
     */
    @JsonProperty("analysis_date")
    private String analysisDate;

    /**
     * 店铺ID
     */
    @JsonProperty("shop_id")
    private int shopId;

    /**
     * 会员id
     */
    @JsonProperty("member_id")
    private int memberId;

    /**
     * 商品id
     */
    @JsonProperty("product_id")
    private int productId;

    /**
     * 退款次数
     */
    @JsonProperty("refund_times")
    private int refundTimes;

    /**
     * 退款数量
     */
    @JsonProperty("refund_quantity")
    private int refundQuantity;

    /**
     * 退款金额
     */
    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;

    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;
}
