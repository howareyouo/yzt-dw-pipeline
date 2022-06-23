package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 积分余额
 * <p>
 * <p>
 * 字段定义：会员截止当前的积分余额（导入积分余额+我们系统产生的积分余额）
 * 不受查询时间影响
 * 前后均为闭区间
 * 可支持2位小数
 */
@Getter
@Setter
public class PointsFilter extends Filter<FactMemberUnion> {

    String[] valueRange;

    public PointsFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        String[] valueRange = definition.getValueRange();

        if (valueRange != null) {
            this.valueRange = valueRange;
        }

    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }

        Set<Integer> memberIds = stream.filter(a -> a.getPoints() != null && adaptation(a.getPoints())).map(FactMemberUnion::getId).collect(Collectors.toSet());

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }
    }

    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }


    public boolean adaptation(BigDecimal value) {
        BigDecimal from = BigDecimal.valueOf(Integer.MIN_VALUE);
        BigDecimal to = BigDecimal.valueOf(Integer.MAX_VALUE);
        if (StringUtils.isNotBlank(valueRange[0])) {
            from = new BigDecimal(valueRange[0]);
        }

        if (valueRange.length > 1 && StringUtils.isNotBlank(valueRange[1])) {
            to = new BigDecimal(valueRange[1]);
        }

        if (value.compareTo(from) >= 0 && value.compareTo(to) <= 0) {
            return true;
        }
        return false;
    }
}
