package cn.yizhi.yzt.pipeline.model.ods;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * 会员群组定义
 *
 * @author aorui created on 2021/1/8
 */
@Getter
@Setter
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsMemberGroup {

    /**
     * 唯一标志
     */
    @JsonProperty("id")
    private Integer Id;

    /**
     * 门店id
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 群组类型. 0:自定义, 1:重要客户, 2:意向客户
     */
    @JsonProperty("category")
    private Integer category;

    /**
     * 分组名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * '分组描述'
     */
    @JsonProperty("description")
    private String description;

    /**
     * 群组规则定义 JSONArray
     */
    @JsonProperty("filters")
    private String filters;

    /**
     * 群组版本
     */
    @JsonProperty("version")
    private Integer version;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    /**
     * 删除标志
     */
    @JsonProperty("__deleted")
    private boolean __deleted;

    @Ignore
    @JsonProperty("data_type")
    private DataType dataType = DataType.SHOP_MEMBER_GROUP;

}
