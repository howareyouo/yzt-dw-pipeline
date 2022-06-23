package cn.yizhi.yzt.pipeline.model.fact.member;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import cn.yizhi.yzt.pipeline.util.BigDecimalUtil;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @Author: HuCheng
 * @Date: 2021/1/12 15:33
 */
@Getter
@Setter
public class FactMemberTotal {
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * shop_member的id 作为主键id
     */
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("order_count")
    private Integer orderCount;

    @JsonProperty("total_order_amount")
    private BigDecimal totalOrderAmount;

    //累计退款金额
    @JsonProperty("refund_amount")
    private BigDecimal refundAmount;

    //累计退款单数
    @JsonProperty("refund_count")
    private Integer refundCount;


    @JsonProperty("last_order_time")
    private Timestamp lastOrderTime;

    /**
     * 首次消费时间
     */
    @JsonProperty("first_order_time")
    private Timestamp firstOrderTime;

    //*************   需单独计算  *************
    //客单价
    @JsonProperty("avg_consume_amount")
    BigDecimal avgConsumeAmount;


    //消费频率
    @JsonProperty("purchase_rate")
    BigDecimal purchaseRate;

    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;

    /**
     * 计算消费频率
     * <p>
     * 消费频率公式确定调整为：（消费次数）÷ （最后消费时间-首次消费时间）
     * 前后均为闭区间
     * 可支持2位小数
     * 不受时间影响
     */
    public void calPurchaseRate(FactMemberTotal fmt) {
        BigDecimal value = BigDecimal.ZERO;
        int orderCount = fmt.getOrderCount();
        if (fmt.getLastOrderTime() != null && fmt.getFirstOrderTime() != null && orderCount > 0) {
            long fromLogical = TimeUtil.toDays(fmt.getFirstOrderTime());
            long toLogical = TimeUtil.toDays(fmt.getLastOrderTime());
            if (toLogical - fromLogical > 0) {
                double div = BigDecimalUtil.div(orderCount, toLogical - fromLogical, 2);
                value = BigDecimal.valueOf(div);
            }
        }
        fmt.setPurchaseRate(value);
    }


}
