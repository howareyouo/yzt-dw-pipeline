package cn.yizhi.yzt.pipeline.model.ods;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author aorui created on 2021/1/8
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OdsTagDefinition {

    public static final int NORMAL_TAG = 1;
    public static final int SMART_TAG = 2;


    @JsonProperty("id")
    private Integer id;

    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("pid")
    private Integer pid;
    //标签类型，1手动标签，2智能标签
    @JsonProperty("type")
    private Integer type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private Integer category;

    @JsonProperty("filters")
    private String filters;

    @JsonProperty("version")
    private Integer version;


    /**
     * 删除标志
     */
    @JsonProperty("__deleted")
    private boolean __deleted;

    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;

}
