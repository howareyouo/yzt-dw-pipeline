package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 客户来源
 * <p>
 * 默认为不限，可多选
 */
public class SourceFilter extends Filter<FactMemberUnion> {

    List<String> sources;


    public SourceFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {

        String value = definition.getValue();
        if (StringUtils.isNotBlank(value)) {
            String[] strings = value.split(SPLIT_STR);
            sources = Arrays.asList(strings);
        }


    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }

        Set<Integer> memberIds = stream.filter(a -> adaptation(a.getSource())).map(FactMemberUnion::getId).collect(Collectors.toSet());

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }
    }

    public boolean adaptation(String source) {
        if (!CollectionUtil.isNullOrEmpty(sources) && source != null) {
            return sources.contains(source);
        }
        return false;
    }


    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }
}
