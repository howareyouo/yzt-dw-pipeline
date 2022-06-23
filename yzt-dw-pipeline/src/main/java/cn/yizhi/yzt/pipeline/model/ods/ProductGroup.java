package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductGroup {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("detail")
    private String detail;
    @JsonProperty("is_default")
    private Boolean isDefault;
    @JsonProperty("sort_first")
    private String sortFirst;
    @JsonProperty("sort_second")
    private String sortSecond;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;
}
