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
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * 消费金额
 * <p>
 * 导入的累积消费金额+成功下单且付款的的订单额（不含退款）
 * 前后均为闭区间
 * 可支持2位小数
 * 如上方有发生时间的筛选，则代表该时间段内的在我们系统的消费金额
 */
@Getter
@Setter
public class TimeLessTotalAmountFilter extends Filter<FactMemberUnion> {

    //范围
    double start;
    double end;

    //计算占比
    boolean calRatio;


    String[] valueRange;

    public TimeLessTotalAmountFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        String[] valueRange = definition.getValueRange();
        if (valueRange != null) {
            //是否求占比
            calRatio = Arrays.stream(valueRange).anyMatch(a -> a.contains(RATIO_STR));
            if (calRatio) {
                Double start = Double.parseDouble(valueRange[0].replace(RATIO_STR, ""));
                Double end = Double.parseDouble(valueRange[1].replace(RATIO_STR, ""));
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

        //先排序  再截取 分页
        List<Integer> totalUnions = context.getStreamSupplier().get().filter(a -> a.getTotalOrderAmount() != null && a.getTotalOrderAmount().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(FactMemberUnion::getTotalOrderAmount).reversed())
                .map(a -> a.getId()).collect(Collectors.toList());
        //返回流出去

        int size = totalUnions.size();
        if (size > 0) {
            //总数乘以占比  如有小数进1 比如 2.3 -> 3
            int start = (int) Math.ceil(size * this.start);
            int end = (int) Math.ceil(size * this.end);
            if (start > 0 && start == end) {
                start = start - 1;
            }
            
            Set<Integer> memberIds = totalUnions.parallelStream().skip(start).limit(end - start).collect(Collectors.toSet());
            if (CollectionUtil.isNullOrEmpty(memberIds)) {
                context.setMemberIds(Collections.emptySet());
                context.setFinished(true);
            } else {
                context.setMemberIds(memberIds);
                context.setFinished(true);
            }
        } else {
            context.setMemberIds(Collections.emptySet());
            context.setFinished(true);
        }

    }

    private boolean adaptation(BigDecimal totalOrderAmount, BigDecimal consumeAmount) {
        if (totalOrderAmount == null) {
            totalOrderAmount = new BigDecimal(0);
        }
        if (consumeAmount == null) {
            consumeAmount = new BigDecimal(0);
        }
        BigDecimal value = totalOrderAmount.add(consumeAmount);
        return math(value);
    }


    public boolean math(BigDecimal value) {
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

    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }
}
