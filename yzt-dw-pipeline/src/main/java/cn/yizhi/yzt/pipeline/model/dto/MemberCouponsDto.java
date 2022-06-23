package cn.yizhi.yzt.pipeline.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberCouponsDto {

    //模板ID
    private List<Integer> couponTemplates;

    private List<Integer> couponTypes;


}
