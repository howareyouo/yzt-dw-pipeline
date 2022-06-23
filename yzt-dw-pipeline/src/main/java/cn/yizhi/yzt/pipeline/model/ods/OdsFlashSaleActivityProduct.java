package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author hucheng
 * @date 2020/11/2 11:11
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsFlashSaleActivityProduct {
    /**
     * id
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;

    /**
     * 商品名
     */
    @JsonProperty("product_name")
    private String productName;

    /**
     * 活动id
     */
    @JsonProperty("activity_id")
    private Integer activityId;
}
