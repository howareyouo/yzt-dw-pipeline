package cn.yizhi.yzt.pipeline.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 聚合JOB
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberGroupStreamResultDto {

    private Integer shopId;

    private SourceType sourceType;

    private Integer targetId;

    //此次分析需要添加的id集合
    private List<Integer> adds;

    //此次分析需要删除的id集合
    private List<Integer> deletes;

    private boolean update;

    //此次分析的会员全部符合的id集合
    private List<Integer> analysis;

    public enum SourceType {
        GROUP,
        SMART_TAG
    }

}
