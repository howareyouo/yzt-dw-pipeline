package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.FactMemberShopVisits;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 访问次数
 * <p>
 * 受行为行为发生时间影响
 * 客户访问商城的次数，进入-退出算一个周期 算1次
 * 前后均为闭区间
 * 只可为大于0整数
 */
@Getter
@Setter
public class VisitTimesFilter extends Filter<FactMemberShopVisits> {


    String[] valueRange;


    public VisitTimesFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        valueRange = definition.getValueRange();
    }

    @Override
    public void filter(FilterContext<FactMemberShopVisits> context) {

        if (valueRange == null) {
            return;
        }

        List<FactMemberShopVisits> collect = context.getStreamSupplier().get().collect(Collectors.toList());


        Stream<FactMemberShopVisits> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getMemberId()));
        }

        //符合实际的
        stream = stream.filter(a -> isDatelegal(a.getVisitsStart().toLocalDateTime().toLocalDate()));

        Map<Integer, List<FactMemberShopVisits>> map = stream.collect(Collectors.groupingBy(FactMemberShopVisits::getMemberId));
        int from = Integer.MIN_VALUE;
        int to = Integer.MAX_VALUE;


        if (isNotBlank(valueRange[0])) {
            from = Integer.parseInt(valueRange[0]);
        }

        if (valueRange.length == 2 && isNotBlank(valueRange[1])) {
            to = Integer.parseInt(valueRange[1]);
        }

        int finalFrom = from;
        int finalTo = to;
        Set<Integer> memberIds = new HashSet<>();
        map.forEach((k, v) -> {
            if (v.size() >= finalFrom && v.size() <= finalTo) {
                memberIds.add(k);
            }
        });

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }

    }

    @Override
    public Class<FactMemberShopVisits> supportedType() {
        return FactMemberShopVisits.class;
    }
}
