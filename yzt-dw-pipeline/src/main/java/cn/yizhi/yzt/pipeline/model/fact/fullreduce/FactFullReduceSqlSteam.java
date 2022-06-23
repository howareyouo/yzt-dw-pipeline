package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * @author aorui created on 2020/11/9
 */
@Getter
@Setter
public class FactFullReduceSqlSteam {
    @JsonProperty("promotion_id")
    private Long promotionId;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("product_rule")
    private String productRule;

    @JsonProperty("start_time")
    private Timestamp startTime;

    @JsonProperty("end_time")
    private Timestamp endTime;

    @JsonProperty("state")
    private Integer state;

    @JsonProperty("longTerm")
    private Integer longTerm;
}
