package cn.yizhi.yzt.pipeline.model.ods;


import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopMember {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("member_id")
    private Integer memberId;
    @JsonProperty("shop_id")
    private Integer shopId;
    @JsonProperty("main_shop_id")
    private Integer mainShopId;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("source")
    private String source;
    @JsonProperty("payment_password")
    private String paymentPassword;
    @JsonProperty("disabled")
    private int disabled;
    @JsonProperty("created_at")
    private Timestamp createdAt;
    @JsonProperty("updated_at")
    private Timestamp updatedAt;
}
