package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductOrder;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.util.CollectionUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.yizhi.yzt.pipeline.util.TimeUtil.yearMonthDayfmt;

/**
 * 下单未付款的过滤条件
 * <p>
 *
 * <p>
 * 商品选择组件 {products:[],productGroups:[]}
 * 优惠券选择组件 {couponTemplates:[], couponTypes:[]}
 */
@Getter
@Setter
public class UnpaidProductsFilter extends Filter<FactMemberProductOrder> {


    boolean isAll;


    public UnpaidProductsFilter(FilterDefinition definition) {
        super(definition);
    }

    public void init(FilterDefinition definition) {


        if (definition.isAny()) {
            isAll = true;
            return;
        }




    }

    @Override
    public void filter(FilterContext<FactMemberProductOrder> context) {
        Stream<FactMemberProductOrder> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getMemberId()));
        }

        //满足时间的
        stream = stream.filter(data -> isDatelegal(LocalDate.parse(data.getAnalysisDate(), yearMonthDayfmt)));
        //按用户 groupBy 再按照 商品groupby  再统计 a.getOrderTimes() - a.getPaidTimes()
        final Map<Integer, Map<Integer, Integer>> mapMap = stream.collect(Collectors.groupingBy(FactMemberProductOrder::getMemberId,
                Collectors.groupingBy(FactMemberProductOrder::getProductId, Collectors.summingInt(a -> (a.getOrderTimes() - a.getPaidTimes())))));

        Set<Integer> memberIds = new HashSet<>();
        for (Integer key : mapMap.keySet()) {
            boolean anyMatch = mapMap.get(key).entrySet().stream().anyMatch(a -> a.getValue() > 0);
            if (anyMatch) {
                memberIds.add(key);
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
    public Class<FactMemberProductOrder> supportedType() {
        return FactMemberProductOrder.class;
    }

    public static void main(String[] args) {
        List<Integer> result = new ArrayList<>();

        result.add(1);
        result.add(2);
        result.add(3);
        result.add(4);
        result.add(5);
        result.add(-1);
        Supplier<Stream<Integer>> streamSupplier = () -> result.stream();
        final List<Integer> collect = streamSupplier.get().filter(a -> a > 1).collect(Collectors.toList());
        final List<Integer> integers = streamSupplier.get().filter(a -> a > 2).collect(Collectors.toList());
        System.out.println(collect);
        System.out.println(integers);


    }
}
