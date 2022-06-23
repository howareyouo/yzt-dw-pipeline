package cn.yizhi.yzt.pipeline.model.ods;


import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductReGroup {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("product_id")
    private Integer productId;
    @JsonProperty("group_id")
    private Integer groupId;


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProductReGroup) {
            ProductReGroup reGroup = (ProductReGroup) obj;
            if (reGroup.getGroupId() == null || reGroup.getProductId() == null) {
                return false;
            }
            return reGroup.getProductId().equals(productId)
                    && reGroup.getGroupId().equals(groupId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, groupId);
    }
}
