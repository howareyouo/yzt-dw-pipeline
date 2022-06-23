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
 * 储值余额过滤条件
 * <p>
 * 字段定义：会员截止当前的储值余额（需含导入的储值余额）
 * 不受查询时间影响
 * 前后均为闭区间
 * 可支持2位小数
 */
@Getter
@Setter
public class BalanceFilter extends Filter<FactMemberUnion> {

    BigDecimal balanceFrom;
    BigDecimal balanceTo;

    public BalanceFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        String[] range = definition.getValueRange();
        if (range == null ) {
            throw new IllegalArgumentException("储值余额过滤条件参数错误：" + range);
        }

        if (StringUtils.isNotBlank(range[0])) {
            balanceFrom = new BigDecimal(range[0]);
        }

        if (range.length > 1 && StringUtils.isNotBlank(range[1])) {
            balanceTo = new BigDecimal(range[1]);
        }
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
     * 过滤
     *
     * @param factMemberUnion
     * @return
     */
    private boolean adaptation(FactMemberUnion factMemberUnion) {
        BigDecimal balance = factMemberUnion.getBalanceAmount();
        if (balance == null) {
            balance = new BigDecimal(0);
        }

        return balance.compareTo(balanceFrom) >= 0 && balance.compareTo(balanceTo) <= 0;
    }


    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }
}
