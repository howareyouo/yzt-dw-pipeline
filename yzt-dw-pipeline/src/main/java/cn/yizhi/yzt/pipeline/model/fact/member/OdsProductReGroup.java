package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 商品分组规则
 * @author aorui created on 2021/1/11
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsProductReGroup {

    private Integer id;

    /**
     * 店铺id
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 分组id
     */
    @JsonProperty("group_id")
    private Integer groupId;

    /**
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("__deleted")
    private boolean __deleted;
}
