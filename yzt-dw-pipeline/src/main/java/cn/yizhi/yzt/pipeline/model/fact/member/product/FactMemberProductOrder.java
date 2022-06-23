package cn.yizhi.yzt.pipeline.model.fact.member.product;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: HuCheng
 * @Date: 2021/1/5 17:36
 */
@Getter
@Setter
@ToString
public class FactMemberProductOrder {
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
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;

    /**
     * 下单次数
     */
    @JsonProperty("order_times")
    private int orderTimes;

    /**
     * 下单金额
     */
    @JsonProperty("order_amount")
    private BigDecimal orderAmount;

    /**
     * 购买件数
     */
    @JsonProperty("order_quantity")
    private int orderQuantity;

    /**
     * 付款订单数
     */
    @JsonProperty("paid_times")
    private int paidTimes;

    /**
     * 付款金额
     */
    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;


    /**
     * 分组id，数组
     */
    @JsonProperty("group_ids")
    private String groupIds = "[]";

    /**
     * 分组ids（不入库）
     */
    @Ignore
    private List<Integer> groups;



    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;
}
