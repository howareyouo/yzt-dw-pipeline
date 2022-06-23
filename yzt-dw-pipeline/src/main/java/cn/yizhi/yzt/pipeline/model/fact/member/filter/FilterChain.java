package cn.yizhi.yzt.pipeline.model.fact.member.filter;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterResult;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.list.*;
import cn.yizhi.yzt.pipeline.util.JsonMapper;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.list.*;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
@Setter
public class FilterChain {

    Map<Class<?>, List<Filter<?>>> typeMapping = new HashMap<>();

    public static volatile Map<FilterCondition, Class<? extends Filter<?>>> filterMapping = new HashMap<>();

    static {
        filterMapping.put(FilterCondition.AGE, AgeFilter.class);
        filterMapping.put(FilterCondition.AVG_AMOUNT, AvgAmountFilter.class);
        filterMapping.put(FilterCondition.BALANCE, BalanceFilter.class);
        filterMapping.put(FilterCondition.BIRTHDAY, BirthdayFilter.class);
        filterMapping.put(FilterCondition.BUYS_PRODUCTS, BuysProductsFilter.class);
        filterMapping.put(FilterCondition.CART_PRODUCTS, CartProductsFilter.class);
        filterMapping.put(FilterCondition.CREATED_AT, CreatedAtFilter.class);
        filterMapping.put(FilterCondition.FIRST_PURCHASE, FirstPurchaseFilter.class);
        filterMapping.put(FilterCondition.GAIN_COUPONS, GainCouponsFilter.class);
        filterMapping.put(FilterCondition.GENDER, GenderFilter.class);
        filterMapping.put(FilterCondition.HOLD_COUPONS, HoldCouponsFilter.class);
        filterMapping.put(FilterCondition.LATEST_PURCHASE, LatestPurchaseFilter.class);
        filterMapping.put(FilterCondition.LOCATION, LocationFilter.class);
        filterMapping.put(FilterCondition.POINTS, PointsFilter.class);
        filterMapping.put(FilterCondition.PURCHASE_TIMES, PurchaseTimesFilter.class);
        filterMapping.put(FilterCondition.PURCHASE_RATE, PurchaseRateFilter.class);
        filterMapping.put(FilterCondition.SHARE_PRODUCTS, ShareProductsFilter.class);
        filterMapping.put(FilterCondition.SHARE_COUPONS, ShareCouponsFilter.class);
        filterMapping.put(FilterCondition.SOURCE, SourceFilter.class);
        filterMapping.put(FilterCondition.SUBSCRIBED, SubscribedFilter.class);
        filterMapping.put(FilterCondition.TAGS, TagsFilter.class);
        filterMapping.put(FilterCondition.TIMELESS_TOTAL_AMOUNT, TimeLessTotalAmountFilter.class);
        filterMapping.put(FilterCondition.TOTAL_AMOUNT, TotalAmountFilter.class);
        filterMapping.put(FilterCondition.VISIT_TIMES, VisitTimesFilter.class);
        filterMapping.put(FilterCondition.UNPAID_PRODUCTS, UnpaidProductsFilter.class);
        filterMapping.put(FilterCondition.USED_COUPONS, UsedCouponsFilter.class);
        filterMapping.put(FilterCondition.VIEW_PRODUCTS, ViewProductsFilter.class);
        filterMapping.put(FilterCondition.VISIT_DURATION, VisitDurationFilter.class);
    }


    public <T> FilterResult filterResult(Class<T> dataType, Supplier<Stream<T>> stream, LocalDate now, Supplier<Stream<FactMemberUnion>> memberUnionSupplier) {
        List<Filter<?>> filters = typeMapping.get(dataType);
        FilterResult result = new FilterResult();
        if (CollectionUtil.isNullOrEmpty(filters)) {
            result.setUsed(false);
            return result;
        }

        result.setUsed(true);
        filters.sort(Comparator.comparing(f -> -f.getDefinition().getCondition().order));
        FilterContext<T> context = new FilterContext<>();
        context.setStreamSupplier(stream);
        for (Filter filter : filters) {
            filter.setReferenceTime(now);

            if (context.isFinished()) {
                result.setMemberIds(context.getMemberIds());
                return result;
            }
            context.setMemberUnionSupplier(memberUnionSupplier);
            filter.filter(context);

        }

        if (CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            boolean contains = context.getMemberIds().contains((Integer) 0);
            if (contains) {
                context.getMemberIds().remove((Integer) 0);
            }
        }
        result.setMemberIds(context.getMemberIds());

        return result;
    }

    public static FilterChain createFromJSONArray(String jsonArray) throws ReflectiveOperationException {
        List<FilterDefinition> definitions = JsonMapper.nonEmptyMapper().fromJsonArray(jsonArray, FilterDefinition.class);

        FilterChain filterChain = new FilterChain();

        for (FilterDefinition definition : definitions) {
            FilterCondition condition = definition.getCondition();
            Class<? extends Filter<?>> filterClass = filterMapping.get(condition);
            if (filterClass == null) {
                throw new IllegalArgumentException("FilterCondition: " + condition + "unsupported!");
            }
            Constructor<?> constructor = filterClass.getConstructors()[0];
            Filter<?> filter = (Filter<?>) constructor.newInstance(definition);

            filterChain.typeMapping.compute(filter.supportedType(), (K, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(filter);
                return v;
            });
        }
        return filterChain;
    }


    /**
     * 内存工具处理。
     *
     * @param filter
     * @param cache
     * @return
     * @throws ReflectiveOperationException
     */
    public static FilterChain fromCache(String filter, Cache<String, Object> cache) {
//        try {
//            FilterChain filterChain = null;
//            if (StringUtils.isBlank(filter)) {
//                return null;
//            }
//            filterChain = createFromJSONArray(filter);
//            return filterChain;
//        } catch (Exception e) {
//            System.out.println("filterchain：" + e.getMessage());
//            throw new IllegalArgumentException(e);
//        }
        if (StringUtils.isEmpty(filter)) {
            return null;
        }

        String key = filter;

        Object present = cache.getIfPresent(key);
        if (present == null) {
            FilterChain filterChain = null;
            if (StringUtils.isBlank(filter)) {
                return null;
            }
            try {
                filterChain = createFromJSONArray(filter);
                cache.put(key, filterChain);
                return filterChain;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (present instanceof String) {
            try {
                return createFromJSONArray(filter);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            return null;
        }

        return (FilterChain) present;
    }

    public static void main(String[] args) {
        try {

            FilterChain fromJSONArray = createFromJSONArray("[{\"condition\":\"GENDER\",\"value\":\"2\"},{\"condition\":\"BIRTHDAY\",\"valueRange\":[\"02-01\",\"12-02\"]}]");

            System.out.println(fromJSONArray);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

    }
}
