package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import cn.yizhi.yzt.pipeline.util.JsonMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.util.CollectionUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 最近消费时间
 * <p>
 * <p>
 * <p>
 * 不受时间的影响
 * 前后均为闭区间，选择到天
 */
@Getter
@Setter
public class LatestPurchaseFilter extends Filter<FactMemberUnion> {

    String[] valueRange;


    public LatestPurchaseFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {
        if (definition.getValueRange() != null) {
            valueRange = definition.getValueRange();
        }

    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }
        Set<Integer> memberIds = stream.filter(this::adaptation).map(FactMemberUnion::getId).collect(Collectors.toSet());

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }
    }

    /**
     * 条件过滤
     */
    public boolean adaptation(FactMemberUnion data) {
        if (data.getShopId() == 227) {
            System.out.println(JsonMapper.defaultMapper().toJson(data));
        }
        //高级筛选的规则，群组是按固定天数
        if (valueRange != null) {
            int start = 0;
            int end = Integer.MAX_VALUE;
            if (isNotBlank(valueRange[0])) {
                start = Integer.parseInt(valueRange[0]);
            }
            if (valueRange.length == 2 && isNotBlank(valueRange[1])) {
                end = Integer.parseInt(valueRange[1]);
            }
            if (data.getLastOrderTime() != null) {
                LocalDate when = data.getLastOrderTime().toLocalDateTime().toLocalDate();
                // long days = when.until(referenceTime, ChronoUnit.DAYS);
                long days = ChronoUnit.DAYS.between(when, referenceTime);
                return start <= days && days <= end;
            }
            //满足标签的筛选条件
        } else {
            return data.getLastOrderTime() != null && isDatelegal(data.getLastOrderTime().toLocalDateTime().toLocalDate());
        }

        return false;
    }

    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }
}
