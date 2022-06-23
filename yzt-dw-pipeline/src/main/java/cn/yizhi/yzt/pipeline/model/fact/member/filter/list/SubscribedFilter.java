package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 绑定微信
 * <p>
 * 默认为不限，此处按是否绑定业务推送公众号为准
 * 可选项「不限、是、否」
 */
@Getter
@Setter
public class SubscribedFilter extends Filter<FactMemberUnion> {
    //    //是否关注公众号. 1是, 0否
    int subscribed;

    public SubscribedFilter(FilterDefinition definition) {
        super(definition);
    }

    public void init(FilterDefinition definition) {
        String value = definition.getValue();
        if (StringUtils.isNotBlank(value)) {
            subscribed = Integer.parseInt(value);
        }

    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }

        Set<Integer> memberIds = stream.filter(a -> adaptation(a.getSubscribed())).map(FactMemberUnion::getId).collect(Collectors.toSet());

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }
    }


    public boolean adaptation(Integer msubscribed) {
        if (msubscribed == null) {
            msubscribed = 0;
        }
        return subscribed == msubscribed;

    }


    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }
}
