package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductLog;
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
 * 分享的商品
 * 默认不限,则代表不对该选项进行筛选
 * 列表将门店当前优惠券展示供选择
 * 可多选，多选只要满足一个则就会被查询出来
 * <p>
 * 商品选择组件 {products:[],productGroups:[]}
 * 优惠券选择组件 {couponTemplates:[], couponTypes:[]}
 */
@Setter
@Getter
public class ShareProductsFilter extends Filter<FactMemberProductLog> {

    List<Integer> products;

    List<Integer> productGroups;


    boolean isAll;

    public ShareProductsFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {


        if (definition.isAny()) {
            isAll = true;
            return;
        }


        if (!CollectionUtil.isNullOrEmpty(definition.getProducts())) {
            products = definition.getProducts();
        }

        if (!CollectionUtil.isNullOrEmpty(definition.getProductGroups())) {
            productGroups = definition.getProductGroups();
        }


    }

    @Override
    public void filter(FilterContext<FactMemberProductLog> context) {
        Stream<FactMemberProductLog> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getMemberId()));
        }

        Set<Integer> memberIds = stream.filter(a -> adaptation(a)).map(FactMemberProductLog::getMemberId).collect(Collectors.toSet());

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
    public boolean adaptation(FactMemberProductLog data) {
        if (!isDatelegal(LocalDate.parse(data.getAnalysisDate(), yearMonthDayfmt))) {
            return false;
        }
        if (data.getShareTimes() <= 0) {
            return false;
        }
        //是否满足商品判断
        return isAll||(products != null && products.contains(data.getProductId())) ||
                (productGroups != null && data.getGroups() != null && productGroups.stream().anyMatch(a -> data.getGroups().contains(a)));
    }

    @Override
    public Class<FactMemberProductLog> supportedType() {
        return FactMemberProductLog.class;
    }
}
