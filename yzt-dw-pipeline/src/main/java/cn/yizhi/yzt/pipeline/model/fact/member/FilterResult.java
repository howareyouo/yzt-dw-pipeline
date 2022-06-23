package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class FilterResult {

    private boolean used;


    private Set<Integer> memberIds;

}
