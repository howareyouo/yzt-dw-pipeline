package cn.yizhi.yzt.pipeline.model.ods;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsMemberTag {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("member_id")
    private Integer memberId;

    @JsonProperty("tag_id")
    private Integer tagId;

    @JsonProperty("shop_id")
    private Integer shopId;
    /**
     * 删除标志
     */
    @JsonProperty("__deleted")
    private boolean __deleted;


    @Ignore
    @JsonProperty("data_type")
    private DataType dataType = DataType.MEMBER_TAG;
}
