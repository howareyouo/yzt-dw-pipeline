package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @Author: HuCheng
 * @Date: 2020/12/15 20:10
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderPromotionTable {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("order_id")
    private Long orderId;

    /**
     * 活动类型， 在product-service GRPC接口中定义：
     *
     *     活动类型不要使用0
     *     NOACTIVITY = 0;
     *     优惠券
     *     COUPON = 1;
     *     满减满赠
     *     MANJIAN = 2;
     *     限时抢购
     *     FLASHSALE = 3;
     *     直播
     *     WX_LIVE = 4;
     *
     *  最新的定义请参考源头文件
     */
    @JsonProperty("promotion_type")
    private Integer promotionType;
    @JsonProperty("promotion_id")
    private Integer promotionId;
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;//优惠金额

    //创建时间
    @JsonProperty("created_at")
    private Timestamp createdAt;
}
