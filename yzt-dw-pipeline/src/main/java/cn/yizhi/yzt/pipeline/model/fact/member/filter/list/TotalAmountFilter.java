package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import cn.yizhi.yzt.pipeline.model.fact.member.order.FactMemberOrder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.yizhi.yzt.pipeline.util.TimeUtil.yearMonthDayfmt;
import static java.util.stream.Collectors.*;

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
public class TotalAmountFilter extends Filter<FactMemberOrder> {

    //范围
    double start;
    double end;

    //计算占比
    boolean calRatio;


    boolean timeRangeAbsent;


    String[] valueRange;

    public TotalAmountFilter(FilterDefinition definition) {
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
        timeRangeAbsent = definition.getTimeRange() == null && definition.getRelativeTimeRange() == null;

    }

    @Override
    public void filter(FilterContext<FactMemberOrder> context) {
        Stream<FactMemberOrder> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getMemberId()));
        }

        if (!calRatio) {
            Stream<FactMemberOrder> memberOrderStream = stream.filter(a -> isDatelegal(LocalDate.parse(a.getAnalysisDate(), yearMonthDayfmt)));
            List<FactMemberOrder> memberOrders = memberOrderStream.collect(Collectors.toList());

            Supplier<Stream<FactMemberOrder>> dataStream = () -> memberOrders.stream();

            Map<Integer, BigDecimal> factMemberUnionMap = null;
            // 未指定时间区间时, 获取导入的消费金额/次数
            if (timeRangeAbsent) {
                Stream<FactMemberUnion> memberUnionStream = context.getMemberUnionSupplier().get();
                factMemberUnionMap = memberUnionStream.collect(Collectors.toMap(FactMemberUnion::getId, a -> (a.getConsumeAmount() == null ? BigDecimal.ZERO : a.getConsumeAmount())));
            }
            Set<Integer> memberIds = new HashSet<>();

            //消费总金额 等于 累计消费金额-累计退款金额
            Map<Integer, BigDecimal> map = dataStream.get()
                    .collect(groupingBy(FactMemberOrder::getMemberId, collectingAndThen(toList(), list -> {
                                BigDecimal sum = BigDecimal.ZERO;
                                for (FactMemberOrder fmo : list) {
                                    sum = sum.add(fmo.getPaidAmount().subtract(fmo.getRefundAmount()));
                                }
                                return sum;
                            }
                    )));


            //导入的也计算其中
            if (factMemberUnionMap != null) {
                for (Map.Entry<Integer, BigDecimal> amount : factMemberUnionMap.entrySet()) {
                    BigDecimal value = amount.getValue();

                    if (map.containsKey(amount.getKey())) {
                        BigDecimal paidAmount = map.get(amount.getKey());
                        if (paidAmount != null) {
                            value = value.add(paidAmount);
                        }
                    }

                    boolean adaptation = adaptation(value);
                    if (adaptation) {
                        memberIds.add(amount.getKey());
                    }

                }
            } else {//不计算导入的。 是有时间计算的。
                for (Map.Entry<Integer, BigDecimal> integerLongEntry : map.entrySet()) {
                    BigDecimal value = integerLongEntry.getValue();
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


        } else {

            Stream<FactMemberOrder> memberOrderStream = context.getStreamSupplier().get();

            Map<Integer, Double> map = memberOrderStream.collect(groupingBy(FactMemberOrder::getMemberId, summingDouble(a -> a.getPaidAmount().doubleValue() - a.getRefundAmount().doubleValue())));

            ArrayList<Map.Entry<Integer, Double>> list = new ArrayList<>(map.entrySet());
            int size = list.size();
            // 总数乘以占比  如有小数进1 比如 2.3 -> 3
            int start = (int) Math.ceil(size * this.start);
            int end = (int) Math.ceil(size * this.end);
            if (start > 0 && start == end) {
                start = start - 1;
            }
            Set<Integer> memberIds = list.stream().sorted(Comparator.comparing(Map.Entry<Integer, Double>::getValue).reversed()).skip(start).limit(end - start).map(a -> a.getKey()).collect(Collectors.toSet());

            if (CollectionUtil.isNullOrEmpty(memberIds)) {
                context.setFinished(true);
                context.setMemberIds(Collections.emptySet());
            } else {
                context.setMemberIds(memberIds);
                context.setFinished(true);
            }


        }


    }

    public static void main(String[] args) {
        int start = (int) Math.ceil(8d * 0.02);
        int end = (int) Math.ceil(8d * 0.03);

        System.out.println(start);
        System.out.println(end);
    }

    private boolean adaptation(BigDecimal totalOrderAmount) {
        if (totalOrderAmount == null) {
            totalOrderAmount = BigDecimal.ZERO;
        }
        return math(totalOrderAmount);
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

        return value.compareTo(from) >= 0 && value.compareTo(to) <= 0;
    }

    @Override
    public Class<FactMemberOrder> supportedType() {
        return FactMemberOrder.class;
    }
}
