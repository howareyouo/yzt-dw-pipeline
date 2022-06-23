package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WxRelation {
    private Integer id;
    @JsonProperty("main_shop_id")
    private Integer mainShopId;
    @JsonProperty("shop_id")
    private Integer shopId;
    private String appid;
    private Integer apptype;
    private Integer relation;

    /**
     * 删除标志
     */
    @JsonProperty("__deleted")
    private Boolean __deleted;

}
