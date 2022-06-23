package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.coupon.FactMemberCoupon;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.util.CollectionUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.yizhi.yzt.pipeline.util.TimeUtil.yearMonthDayfmt;

/**
 * 领取优惠券
 * 默认不限,则代表不对该选项进行筛选
 * 列表将门店当前优惠券展示供选择
 * 可多选，多选只要满足一个则就会被查询出来
 * <p>
 * 商品选择组件 {products:[],productGroups:[]}
 * 优惠券选择组件 {couponTemplates:[], couponTypes:[]}
 */
@Getter
@Setter
public class GainCouponsFilter extends Filter<FactMemberCoupon> {

    List<Integer> couponTemplates;

    List<Integer> couponTypes;


    boolean isAll;

    public GainCouponsFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {


        if (definition.isAny()) {
            isAll = true;
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
        Stream<FactMemberCoupon> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getMemberId()));
        }

        Set<Integer> memberIds = stream.filter(a -> adaptation(a)).map(FactMemberCoupon::getMemberId).collect(Collectors.toSet());

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

        if (!isDatelegal(LocalDate.parse(data.getAnalysisDate(), yearMonthDayfmt))) {
            return false;
        }

        //是否满足商品判断
        return isAll || data.getApplyTimes() > 0 && ((couponTemplates != null && couponTemplates.contains(data.getCouponTemplateId())) ||
                (couponTypes != null && couponTypes.contains(data.getCouponType())));
    }


    @Override
    public Class<FactMemberCoupon> supportedType() {
        return FactMemberCoupon.class;
    }
}
