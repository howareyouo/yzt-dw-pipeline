package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author aorui created on 2020/11/6
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsFullReducePromotion {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("product_rule")
    private String productRule;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("update_at")
    private Timestamp updatedAt;

    @JsonProperty("start_time")
    private Timestamp startTime;

    @JsonProperty("end_time")
    private Timestamp endTime;

    @JsonProperty("state")
    private Integer state;

    @JsonProperty("is_long_term")
    private Integer isLongTerm;

}
