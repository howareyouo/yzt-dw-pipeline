package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class AgeFilter extends Filter<FactMemberUnion> {
    /**
     * 年龄=当前年份-生日年份
     * 生日为大于等于0的整数
     * -勾选后，时间默认为近30天
     * -前后闭区间
     * -后面不填则为证正无穷大
     * -前面不填则为在**数值内
     * -后面输入框数值需大于前面输入框
     */
    private Integer ageFrom;
    private Integer ageTo;

    public AgeFilter(FilterDefinition definition) {
        super(definition);
    }


    @Override
    public void init(FilterDefinition definition) {
        ageFrom = 0;
        ageTo = Integer.MAX_VALUE;
        String[] range = definition.getValueRange();
        if (range == null) {
            throw new IllegalArgumentException("年龄参数错误：" + Arrays.toString(range));
        }

        if (StringUtils.isNotBlank(range[0])) {
            ageFrom = Integer.parseInt(range[0]);
        }

        if (range.length > 1 && StringUtils.isNotBlank(range[1])) {
            ageTo = Integer.parseInt(range[1]);
        }
    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> factMemberUnionStream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            factMemberUnionStream = factMemberUnionStream.filter(a -> context.getMemberIds().contains(a.getId()));
        }
        Set<Integer> memberIds = factMemberUnionStream.filter(a -> a.getBirthday() != null && adaptation(getAge(a.getBirthday().toLocalDate())))
                .map(FactMemberUnion::getId).collect(Collectors.toSet());
        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setMemberIds(Collections.emptySet());
            context.setFinished(true);
        } else {
            context.setMemberIds(memberIds);
        }
    }

    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }

    public boolean adaptation(Integer age) {
        return ageFrom <= age && age <= ageTo;
    }


    private Integer getAge(LocalDate birthDay) {
        int year = referenceTime.getYear() - birthDay.getYear();
        return year <= 0 ? 0 : year;
    }

}
