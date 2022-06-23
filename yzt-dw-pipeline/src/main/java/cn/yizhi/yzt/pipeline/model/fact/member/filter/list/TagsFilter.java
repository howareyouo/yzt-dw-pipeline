package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 会员标签
 * <p>
 * 默认不限,则代表不对该选项进行筛选
 * 列表将门店标签管理中的所有标签显示出来供选项
 * 可多选，多选时则满足一个即可
 * 可选包含手动标签和智能标签
 */
@Getter
@Setter
public class TagsFilter extends Filter<FactMemberUnion> {

    List<Integer> tagIds;


    public TagsFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        String value = definition.getValue();
        if (StringUtils.isNotBlank(value)) {
            String[] strings = value.split(SPLIT_STR);
            tagIds = Arrays.stream(strings).map(Integer::parseInt).collect(Collectors.toList());
        }
    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }

        Set<Integer> memberIds = stream.filter(a -> adaptation(a.getTagIds())).map(FactMemberUnion::getId).collect(Collectors.toSet());

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }

    }

    public boolean adaptation(List<Integer> memberTags) {
        if (!CollectionUtil.isNullOrEmpty(memberTags) && tagIds != null) {
            for (Integer memberTag : memberTags) {
                if (tagIds.contains(memberTag)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }
}
