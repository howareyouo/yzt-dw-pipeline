package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.util.CollectionUtil;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 建档时间
 * <p>
 * 可选项为：本月、上月、今年、去年、可自定义近**天、也可选择时间区段，使用右边时间控件
 * 自定义时间段时开始时间不可大于结束时间
 * 前后均为闭区间
 */
@Getter
@Setter
public class CreatedAtFilter extends Filter<FactMemberUnion> {


    public CreatedAtFilter(FilterDefinition definition) {
        super(definition);
    }


    @Override
    public void init(FilterDefinition definition) {

    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }

        Set<Integer> memberIds = stream.filter(a -> adaptation(a)).map(FactMemberUnion::getId).collect(Collectors.toSet());

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }
    }

    /**
     * 条件过滤
     *
     * @param data
     * @return
     */
    public boolean adaptation(FactMemberUnion data) {

        if (data.getCreatedAt() != null && isDatelegal(data.getCreatedAt().toLocalDateTime().toLocalDate())) {
            return true;
        }
        return false;
    }


    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }
}
