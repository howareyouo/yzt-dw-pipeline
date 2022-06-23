package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.coupon.FactMemberCoupon;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 持有效券 -- 对应选择优惠券模板
 * <p>
 * 默认不限,则代表不对该选项进行筛选
 * 列表将门店当前优惠券展示供选择
 * 可多选，多选只要满足一个则就会被查询出来
 * <p>
 * 商品选择组件 {products:[],productGroups:[]}
 * 优惠券选择组件 {couponTemplates:[], couponTypes:[]}
 */
@Getter
@Setter
public class HoldCouponsFilter extends Filter<FactMemberCoupon> {

    List<Integer> couponTemplates;

    List<Integer> couponTypes;


    boolean isAll;

    public HoldCouponsFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {

        if (definition.isAny()) {
            isAll = true;
            return;
        }

      

        if (!CollectionUtil.isNullOrEmpty(definition.getCouponTemplates())) {
            couponTemplates = definition.getCouponTemplates();
        }

        if (!CollectionUtil.isNullOrEmpty(definition.getCouponTypes())) {
            couponTypes = definition.getCouponTypes();
        }


    }


    @Override
    public void filter(FilterContext<FactMemberCoupon> context) {
        Stream<FactMemberCoupon> factMemberCouponStream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            factMemberCouponStream = factMemberCouponStream.filter(a -> context.getMemberIds().contains(a.getMemberId()));
        }


        //持有效券是指所有的券。
        factMemberCouponStream = factMemberCouponStream.filter(a -> adaptation(a));
        //按用户 groupBy 再按照 商品groupby  再统计 累计加

        Map<Integer, Map<Integer, Integer>> resultMember = factMemberCouponStream.collect
                (Collectors.groupingBy(FactMemberCoupon::getMemberId,
                        Collectors.groupingBy(FactMemberCoupon::getCouponTemplateId,
                                Collectors.summingInt(a -> a.getApplyTimes() - a.getUsedTimes() - a.getExpiredTimes() - a.getDeprecatedTimes()))));
        Set<Integer> memberIds = new HashSet<>();
        for (Integer key : resultMember.keySet()) {
            boolean anyMatch = resultMember.get(key).entrySet().stream().anyMatch(a -> a.getValue() > 0);
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

    /**
     * 条件过滤
     *
     * @param data
     * @return
     */
    public boolean adaptation(FactMemberCoupon data) {

        //是否满足商品判断
        return isAll || ((couponTemplates != null && couponTemplates.contains(data.getCouponTemplateId())) ||
                (couponTypes != null && couponTypes.contains(data.getCouponType())));
    }

    @Override
    public Class<FactMemberCoupon> supportedType() {
        return FactMemberCoupon.class;
    }
}
