package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.FactMemberShopVisits;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import cn.yizhi.yzt.pipeline.util.BigDecimalUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 兴趣人群
 * 商城单次平均浏览时长前20%的客户
 */
@Getter
@Setter
public class VisitDurationFilter extends Filter<FactMemberShopVisits> {
    String[] valueRange;

    //范围
    double start;
    double end;

    //计算占比
    boolean calRatio;


    public VisitDurationFilter(FilterDefinition definition) {
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
    public void filter(FilterContext<FactMemberShopVisits> context) {
        if (calRatio) {
            Map<Integer, TopData> dataList = context.getStreamSupplier().get().filter(a -> a.getMemberId() != null && a.getMemberId() > 0).collect(Collectors.groupingBy(FactMemberShopVisits::getMemberId,
                    Collectors.collectingAndThen(Collectors.toList(), list -> {
                        Integer memberId = 0;
                        //需要，总浏览次数
                        long times = 0;
                        //需要，总浏览时长
                        long visitsDuration = 0;

                        for (FactMemberShopVisits memberShopVisits : list) {
                            memberId = memberId == 0 ? memberShopVisits.getMemberId() : memberId;
                            times++;
                            visitsDuration = visitsDuration + memberShopVisits.getVisitsDuration();
                        }

                        TopData topData = initData(memberId, times, visitsDuration);
                        if (topData.getTimes() != null && topData.getVisitsDuration() != null) {
                            double div = BigDecimalUtil.div(topData.getVisitsDuration(), topData.getTimes(), 4);
                            topData.setAvg(div);
                        }
                        return topData;
                    })));

            if (dataList.containsKey((Integer) 0)) {
                dataList.remove((Integer) 0);
            }

            int size = dataList.size();

            if (size > 0) {
                //总数乘以占比  如有小数进1 比如 2.3 -> 3
                int start = (int) Math.ceil(size * this.start);
                int end = (int) Math.ceil(size * this.end);
                if (start > 0 && start == end) {
                    start = start - 1;
                }
                //先排序  再截取 分页
                Set<Integer> memberIds = dataList.values().parallelStream().sorted(Comparator.comparing(TopData::getAvg).reversed())
                        .skip(start).limit(end - start).map(a -> a.getMemberId()).collect(Collectors.toSet());
                //返回流出去
                if (CollectionUtil.isNullOrEmpty(memberIds)) {
                    context.setFinished(true);
                    context.setMemberIds(Collections.emptySet());
                } else {
                    context.setMemberIds(memberIds);
                    context.setFinished(true);
                }
            } else {
                context.setFinished(true);
                context.setMemberIds(Collections.emptySet());
            }
        }
    }

    @Getter
    @Setter
    public static class TopData {
        private Integer memberId;
        private Long times;
        private Long visitsDuration;
        private Double avg = 0D;


    }

    public static TopData initData(Integer memberId, Long times, Long visitsDuration) {
        TopData topData = new TopData();
        topData.setMemberId(memberId);
        topData.setTimes(times);
        topData.setVisitsDuration(visitsDuration);
        return topData;
    }


    @Override
    public Class<FactMemberShopVisits> supportedType() {
        return FactMemberShopVisits.class;
    }


}
