package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/8/18 15:55
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsLiveRoomOrderItem {
    /**
     * id
     */
    @JsonProperty("id")
    private Long id;

    /**
     * 会员id
     */
    @JsonProperty("member_id")
    private Integer memberId;

    /**
     * 会员姓名
     */
    @JsonProperty("member_name")
    private String memberName;

    /**
     * 会员手机号
     */
    @JsonProperty("member_phone")
    private String memberPhone;

    /**
     * 订单id
     */
    @JsonProperty("order_id")
    private Long orderId;

    /**
     * 订单号
     */
    @JsonProperty("order_no")
    private String orderNo;

    /**
     * 直播间id
     */
    @JsonProperty("live_room_id")
    private Integer liveRoomId;

    /**
     * 店铺id
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;

    /**
     * 商品名称
     */
    @JsonProperty("product_name")
    private String productName;

    /**
     * 商品类型 1-实物商品  2-虚拟商品  3-电子卡劵
     */
    @JsonProperty("product_type")
    private Integer productType;

    /**
     * sku_id
     */
    @JsonProperty("sku_id")
    private Integer skuId;

    /**
     * 数量
     */
    @JsonProperty("quantity")
    private Integer quantity;

    /**
     * 售价
     */
    @JsonProperty("retail_price")
    private BigDecimal retailPrice;

    /**
     * 总价
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    /**
     * 优惠后的总价
     */
    @JsonProperty("actual_amount")
    private BigDecimal actualAmount;

    /**
     * 优惠金额
     */
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 订单状态  0-代付款 1-已付款
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Timestamp createdAt;

    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private Timestamp updatedAt;
}
