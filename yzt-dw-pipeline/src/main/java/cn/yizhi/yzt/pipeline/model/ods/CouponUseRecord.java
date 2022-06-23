package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Setter
@Getter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponUseRecord {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("shop_id")
	private Integer shopId;

	@JsonProperty("member_id")
	private Integer memberId;

	@JsonProperty("coupon_template_id")
	private Integer couponTemplateId;

	@JsonProperty("coupon_id")
	private Long couponId;

	@JsonProperty("promotion_type")
	private int promotionType;

	@JsonProperty("product_id")
	private Integer productId;

	@JsonProperty("product_name")
	private String productName;

	@JsonProperty("product_image")
	private String productImage;

	@JsonProperty("sku_id")
	private Integer skuId;

	@JsonProperty("sku_price")
	private BigDecimal skuPrice;

	@JsonProperty("coupon_code")
	private String couponCode;

	@JsonProperty("order_id")
	private Long orderId;

	@JsonProperty("order_no")
	private String orderNo;

	@JsonProperty("product_quantity")
	private Integer productQuantity;

	@JsonProperty("total_amount")
	private BigDecimal totalAmount;

	@JsonProperty("paid_amount")
	private BigDecimal paidAmount;

	@JsonProperty("discount_amount")
	private BigDecimal discountAmount;

	@JsonProperty("is_refund")
	private boolean isRefund;

	@JsonProperty("created_at")
	private Timestamp createdAt;

	@JsonProperty("updated_at")
	private Timestamp updatedAt;
}
