package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

/**
 * 消费频率
 * <p>
 * <p>
 * <p>
 * 消费频率=（最近一次消费时间-首次消费时间）➗（购买次数-1）
 * 前后均为闭区间
 * 可支持2位小数
 * 不受时间影响
 */
public class PurchaseRateFilter extends Filter<FactMemberUnion> {

    //范围
    double start;
    double end;

    //计算占比 有%号就计算
    boolean calRatio;


    String[] valueRange;

    public PurchaseRateFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        String[] valueRange = definition.getValueRange();
        if (valueRange != null) {
            //是否求占比
            calRatio = Arrays.stream(valueRange).anyMatch(a -> a.contains(RATIO_STR));
            if (calRatio) {
                double start = Double.parseDouble(valueRange[0].replace(RATIO_STR, ""));
                double end = Double.parseDouble(valueRange[1].replace(RATIO_STR, ""));
                this.start = start / 100D;
                this.end = end / 100D;
            } else {
                //求具体数据
                this.valueRange = valueRange;
            }
        }
    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {

        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }

        if (!calRatio) {
            Set<Integer> memberIds = stream.filter(a -> match(a.getPurchaseRate())).
                    map(FactMemberUnion::getId).collect(Collectors.toSet());

            if (CollectionUtil.isNullOrEmpty(memberIds)) {
                context.setFinished(true);
                context.setMemberIds(emptySet());
            } else {
                context.setMemberIds(memberIds);
            }
        } else {

            List<Integer> memberUnions = stream.filter(a -> a.getPurchaseRate() != null && a.getPurchaseRate().compareTo(BigDecimal.ZERO) != 0)
                    .sorted(Comparator.comparing(FactMemberUnion::getPurchaseRate).reversed())
                    .map(FactMemberUnion::getId)
                    .collect(Collectors.toList());
            int size = memberUnions.size();
            if (memberUnions.size() > 0) {
                //总数乘以占比  如有小数进1 比如 2.3 -> 3
                int start = (int) Math.ceil(size * this.start);
                int end = (int) Math.ceil(size * this.end);
                if (start > 0 && start == end) {
                    start = start - 1;
                }
                //先排序  再截取 分页
                Set<Integer> memberIds = memberUnions.stream()
                        .skip(start).limit(end - start).collect(Collectors.toSet());
                //返回流出去
                if (CollectionUtil.isNullOrEmpty(memberIds)) {
                    context.setFinished(true);
                    context.setMemberIds(emptySet());
                } else {
                    context.setMemberIds(memberIds);
                }
            } else {
                context.setFinished(true);
                context.setMemberIds(emptySet());
            }

        }


    }


    public boolean match(BigDecimal value) {

        if (value == null) {
            return false;
        }

        BigDecimal from = BigDecimal.valueOf(Integer.MIN_VALUE);
        BigDecimal to = BigDecimal.valueOf(Integer.MAX_VALUE);
        if (StringUtils.isNotBlank(valueRange[0])) {
            from = new BigDecimal(valueRange[0]);
        }

        if (valueRange != null && valueRange.length > 1 && StringUtils.isNotBlank(valueRange[1])) {
            to = new BigDecimal(valueRange[1]);
        }

        if (value.compareTo(from) >= 0 && value.compareTo(to) <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }


}
