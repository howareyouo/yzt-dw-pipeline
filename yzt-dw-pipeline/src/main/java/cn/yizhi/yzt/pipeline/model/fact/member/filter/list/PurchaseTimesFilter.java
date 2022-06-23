package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import cn.yizhi.yzt.pipeline.model.fact.member.order.FactMemberOrder;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.yizhi.yzt.pipeline.util.TimeUtil.yearMonthDayfmt;

/**
 * 购买次数
 * <p>
 * <p>
 * <p>
 * 导入的购买次数+成功下单且付款的次数（不含退款）
 * 前后均为闭区间
 * 只可为大于0整数
 * 如上方有发生时间的筛选，则代表该时间段内的在我们系统的购买次数
 */
public class PurchaseTimesFilter extends Filter<FactMemberOrder> {

    String[] valueRange;

    boolean timeRangeAbsent;


    public PurchaseTimesFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        String[] valueRange = definition.getValueRange();
        this.valueRange = valueRange;

        timeRangeAbsent = definition.getTimeRange() == null && definition.getRelativeTimeRange() == null;

    }

    @Override
    public void filter(FilterContext<FactMemberOrder> context) {
        Stream<FactMemberOrder> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getMemberId()));
        }

        stream = stream.filter(a -> isDatelegal(LocalDate.parse(a.getAnalysisDate(), yearMonthDayfmt))).filter(a -> a.getPaidCount() > 0);

        Map<Integer, Integer> factMemberUnionMap = null;
        // 未指定时间区间时, 获取导入的消费金额/次数
        if (timeRangeAbsent) {
            Stream<FactMemberUnion> memberUnionStream = context.getMemberUnionSupplier().get();
            factMemberUnionMap = memberUnionStream.collect(Collectors.toMap(FactMemberUnion::getId, a -> a.getConsumeTimes() == null ? 0 : a.getConsumeTimes()));
        }
        Set<Integer> memberIds = new HashSet<>();
        Map<Integer, Integer> map = stream.collect(Collectors.groupingBy(FactMemberOrder::getMemberId, Collectors.summingInt(a -> a.getPaidCount() - a.getRefundCount())));


        //导入的也计算其中
        if (factMemberUnionMap != null) {
            for (Map.Entry<Integer, Integer> memberTimes : factMemberUnionMap.entrySet()) {
                Integer value = memberTimes.getValue();

                if (map.containsKey(memberTimes.getKey())) {
                    Integer paidTimes = map.get(memberTimes.getKey());
                    if (paidTimes != null) {
                        value = value + paidTimes;
                    }
                }

                boolean adaptation = adaptation(value);
                if (adaptation) {
                    memberIds.add(memberTimes.getKey());
                }

            }
        } else {//不计算导入的。 是有时间计算的。
            for (Map.Entry<Integer, Integer> integerLongEntry : map.entrySet()) {
                Integer value = integerLongEntry.getValue();
                boolean adaptation = adaptation(value);
                if (adaptation) {
                    memberIds.add(integerLongEntry.getKey());
                }
            }
        }

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }


    }

    @Override
    public Class<FactMemberOrder> supportedType() {
        return FactMemberOrder.class;
    }


    public boolean adaptation(Integer value) {


        if (value == null) {
            value = 0;
        }

        Integer from = Integer.MIN_VALUE;
        Integer to = Integer.MAX_VALUE;
        if (StringUtils.isNotBlank(valueRange[0])) {
            from = new Double(valueRange[0]).intValue();
        }
        // 50.55
        if (valueRange.length > 1 && StringUtils.isNotBlank(valueRange[1])) {
            to = new Double(valueRange[1]).intValue();
        }

        if (value.compareTo(from) >= 0 && value.compareTo(to) <= 0) {
            return true;
        }
        return false;
    }

}
