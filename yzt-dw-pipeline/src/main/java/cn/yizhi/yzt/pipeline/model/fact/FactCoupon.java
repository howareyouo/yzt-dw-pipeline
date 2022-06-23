package cn.yizhi.yzt.pipeline.model.fact;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author hucheng
 * 优惠劵指标当天实时数据
 * @date 2020/7/18 14:38
 */
@Data
public class FactCoupon {

    @Column(name = "row_date",type = Column.FieldType.KEY)
    @JsonProperty("row_date")
    private String rowDate;

    /**
     * 商品id
     */
    @Column(name = "shop_id",type = Column.FieldType.KEY)
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 优惠券id
     */
    @Column(name = "coupon_id",type = Column.FieldType.KEY)
    @JsonProperty("coupon_id")
    private Integer couponId;

    /**
     * 渠道
     */
    @Column(name = "channel",type = Column.FieldType.KEY)
    @JsonProperty("channel")
    private String channel;

    /**
     * 当日支付总金额
     */
    @JsonProperty("pay_amount")
    private BigDecimal payAmount;

    /**
     * 优惠总金额
     */
    @JsonProperty("preferential_amount")
    private BigDecimal preferentialAmount;

    /**
     *核销人数
     */
    @JsonProperty("write_off_number")
    private Integer writeOffNumber;

    /**
     *核销张数
     */
    @JsonProperty("write_off_count")
    private Integer writeOffCount;

    /**
     * 商品销量
     */
    @JsonProperty("sale_count")
    private Integer saleCount;

    /**
     * 当日总订单数
     */
    @JsonProperty("order_count")
    private Integer orderCount;

    /**
     * 当日订单人数
     */
    @JsonProperty("order_number")
    private Integer orderNumber;

    /**
     * 用劵新客
     */
    @JsonProperty("new_member_count")
    private Integer newMemberCount;

    /**
     * 用券老客
     */
    @JsonProperty("old_member_count")
    private Integer oldMemberCount;
}
