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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.yizhi.yzt.pipeline.util.TimeUtil.yearMonthDayfmt;

/**
 * 平均消费金额/客单价过滤
 * <p>
 * 客单价=累计消费金额➗购买次数
 * 前后均为闭区间
 * 可支持2位小数
 */
@Getter
@Setter
public class AvgAmountFilter extends Filter<FactMemberOrder> {

    BigDecimal avgAmountFrom;
    BigDecimal avgAmountTo;

    boolean timeRangeAbsent;

    public AvgAmountFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        avgAmountFrom = BigDecimal.ZERO;
        avgAmountTo = BigDecimal.valueOf(Integer.MAX_VALUE);
        String[] range = definition.getValueRange();
        if (range == null) {
            throw new IllegalArgumentException("平均消费金额/客单价过滤参数错误：" + Arrays.toString(range));
        }

        if (StringUtils.isNotBlank(range[0])) {
            avgAmountFrom = new BigDecimal(range[0]);
        }

        if (range.length > 1 && StringUtils.isNotBlank(range[1])) {
            avgAmountTo = new BigDecimal(range[1]);
        }

        timeRangeAbsent = definition.getTimeRange() == null && definition.getRelativeTimeRange() == null;
    }

    @Override
    public void filter(FilterContext<FactMemberOrder> context) {
        Stream<FactMemberOrder> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getMemberId()));
        }
        // 退款的 和 付款的都先算入其中
        stream = stream.filter(a -> isDatelegal(LocalDate.parse(a.getAnalysisDate(), yearMonthDayfmt))).filter(a -> a.getRefundCount() > 0 || a.getPaidCount() > 0);

        Map<Integer, FactMemberUnion> factMemberUnionMap = null;

        // 未指定时间区间时, 获取导入的消费金额/次数
        if (timeRangeAbsent) {
            Stream<FactMemberUnion> memberUnionStream = context.getMemberUnionSupplier().get();
            factMemberUnionMap = memberUnionStream.collect(Collectors.toMap(FactMemberUnion::getId, Function.identity()));
        }

        Map<Integer, FactMemberUnion> finalFactMemberUnionMap = factMemberUnionMap;


        Map<Integer, AvgDto> avgDtoMap = stream.collect(Collectors.groupingBy(FactMemberOrder::getMemberId, Collectors.collectingAndThen(Collectors.toList(), list -> {
            AvgDto avgDto = new AvgDto();
            double sumAmount = 0D;
            int times = 0;
            double avg = 0D;
            int memberId = 0;
            //累计退款金额也需要算出来，去除。
            double refundAmount = 0D;
            //退款次数也是样的。
            int refundTimes = 0;


            for (FactMemberOrder factMemberOrder : list) {
                memberId = factMemberOrder.getMemberId();
                sumAmount = sumAmount + factMemberOrder.getPaidAmount().doubleValue();
                times = times + factMemberOrder.getPaidCount();
                refundTimes = refundTimes + factMemberOrder.getRefundCount();
                refundAmount = refundAmount + factMemberOrder.getRefundAmount().doubleValue();
            }

            avgDto.setSumAmount(sumAmount - refundAmount);
            avgDto.setMemberId(memberId);
            avgDto.setTimes(times - refundTimes);
            if (avgDto.getTimes() != 0) {
                avg = avgDto.getSumAmount() / avgDto.getTimes();
            }
            avgDto.setAvg(avg);
            return avgDto;
        })));

        // 没有时间选择的。
        if (finalFactMemberUnionMap != null) {

            for (Map.Entry<Integer, FactMemberUnion> factMemberUnionEntry : finalFactMemberUnionMap.entrySet()) {
                FactMemberUnion memberUnion = factMemberUnionEntry.getValue();
                if (avgDtoMap.containsKey(factMemberUnionEntry.getKey())) {
                    AvgDto avgDto = avgDtoMap.get(factMemberUnionEntry.getKey());
                    FactMemberUnion value = memberUnion;
                    avgDto.setTimes(avgDto.getTimes() + (value.getConsumeTimes() == null ? 0 : value.getConsumeTimes()));
                    avgDto.setSumAmount(avgDto.getSumAmount() + (value.getConsumeAmount() == null ? BigDecimal.ZERO : value.getConsumeAmount()).doubleValue());
                    if (avgDto.getTimes() != 0) {
                        Double avg = avgDto.getSumAmount() / avgDto.getTimes();
                        avgDto.setAvg(avg);
                    }
                } else {
                    AvgDto avgDto = new AvgDto();
                    double avg = 0D;
                    avgDto.setMemberId(factMemberUnionEntry.getKey());
                    avgDto.setSumAmount(((memberUnion.getConsumeAmount() == null ? BigDecimal.ZERO : memberUnion.getConsumeAmount()).doubleValue()));
                    avgDto.setTimes(memberUnion.getConsumeTimes() == null ? 0 : memberUnion.getConsumeTimes());
                    if (avgDto.getTimes() != 0) {
                        avg = avgDto.getSumAmount() / avgDto.getTimes();
                    }
                    avgDto.setAvg(avg);
                    avgDtoMap.put(avgDto.getMemberId(), avgDto);
                }

            }
        }


        Set<Integer> memberIds = avgDtoMap.values().stream().filter(avgDto -> adaptation(new BigDecimal(avgDto.getAvg()))).map(a -> a.getMemberId()).collect(Collectors.toSet());

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }
    }

    @Getter
    @Setter
    public static class AvgDto {
        double sumAmount;
        int times;
        int memberId;
        double avg;
    }

    /**
     * 过滤
     *
     * @return
     */
    private boolean adaptation(BigDecimal avg) {
        if (avg != null && avg.compareTo(avgAmountFrom) >= 0 && avg.compareTo(avgAmountTo) <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public Class<FactMemberOrder> supportedType() {
        return FactMemberOrder.class;
    }

}
