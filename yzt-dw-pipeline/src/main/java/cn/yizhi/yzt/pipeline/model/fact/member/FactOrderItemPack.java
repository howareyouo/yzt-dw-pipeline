package cn.yizhi.yzt.pipeline.model.fact.member;

import cn.yizhi.yzt.pipeline.common.PackType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author aorui created on 2021/1/11
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactOrderItemPack {
    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("main_shop_id")
    private Integer mainShopId;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("union_no")
    private String unionNo;

    @JsonProperty("order_no")
    private String orderNo;

    @JsonProperty("transaction_no")
    private String transactionNo;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("source")
    private String source;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("order_type")
    private Integer orderType;

    @JsonProperty("product_amount")
    private BigDecimal productAmount;

    @JsonProperty("ship_cost_amount")
    private BigDecimal shipCostAmount;

    @JsonProperty("ship_cost_reduced")
    private BigDecimal shipCostReduced;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("actual_amount")
    private BigDecimal actualAmount;

    @JsonProperty("member_id")
    private Integer memberId;

    @JsonProperty("ship_method")
    private Integer shipMethod;

    @JsonProperty("closed_at")
    private Timestamp closedAt;

    @JsonProperty("received_at")
    private Timestamp receivedAt;

    @JsonProperty("paid_at")
    private Timestamp paidAt;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;


    @JsonProperty("parent_id")
    private Integer parentId;

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("sku_id")
    private Integer skuId;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("retail_price")
    private BigDecimal retailPrice;

    @JsonProperty("is_giveaway")
    private Boolean isGiveaway;

    /**
     * ??????????????????
     */
    @JsonProperty("product_discount_amount")
    private BigDecimal productDiscountAmount;

    @JsonProperty("name")
    private String name;

    /**
     * ??????????????????????????????
     */
    @JsonProperty("show_voucher")
    private Boolean showVoucher;

    /**
     * ????????????
     */
    @JsonProperty("product_type")
    private Integer productType;


    /**
     * ????????????
     */
    @JsonProperty("product_actual_amount")
    private BigDecimal productActualAmount;
    /**
     * ????????????
     */
    @JsonProperty("pack_type")
    private PackType packType;

    /**
     * ??????id
     */
    @JsonProperty("group_id")
    private Integer groupId;

    /**
     * ??????ids
     */
    @JsonProperty("group_ids")
    private List<Integer> groupIds;
}
