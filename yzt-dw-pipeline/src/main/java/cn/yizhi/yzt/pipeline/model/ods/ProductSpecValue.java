package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSpecValue {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("spec_id")
    private Integer specId;
    @JsonProperty("spec_value")
    private String specValue;
    @JsonProperty("seq")
    private Integer seq;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;
}
