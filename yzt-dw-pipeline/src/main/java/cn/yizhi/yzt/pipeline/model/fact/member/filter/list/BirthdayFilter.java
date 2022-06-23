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
import java.time.format.DateTimeFormatter;
import java.sql.Date;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.yizhi.yzt.pipeline.util.TimeUtil.fmt;

/**
 * 生日过滤条件
 * <p>
 * <p>
 * 默认为空，可选日期（可选月和日）
 * 1.读取客户的存在系统的生日 计算最近天数
 * 2.
 * 默认为空，可选日期（可选月和日）
 * 开始时间不能大于结束时间，前后都为闭区间
 */
@Getter
@Setter
public class BirthdayFilter extends Filter<FactMemberUnion> {

    //生日范围 mm-dd
    private String birthdayFrom;
    private String birthdayTo;

    private boolean range;

    public BirthdayFilter(FilterDefinition definition) {
        super(definition);
    }

    @Override
    public void init(FilterDefinition definition) {

        String[] range = definition.getValueRange();

        this.range = false;

        birthdayFrom = "01-01";
        birthdayTo = "12-31";

        if (range != null && StringUtils.isNotBlank(range[0])) {
            birthdayFrom = range[0];
            this.range = true;
        }

        if (range != null && range.length > 1 && StringUtils.isNotBlank(range[1])) {
            birthdayTo = range[1];
            this.range = true;
        }

    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }

        Set<Integer> memberIds = stream.filter(a -> adaptation(a)).map(FactMemberUnion::getId).collect(Collectors.toSet());
        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }
    }

    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }


    /**
     * 过滤
     *
     * @param factMemberUnion
     * @return
     */
    private boolean adaptation(FactMemberUnion factMemberUnion) {
        //今天日期格式化装换 mm-dd
        if (factMemberUnion.getBirthday() == null) {
            return false;
        }

        Date birthday = factMemberUnion.getBirthday();
        LocalDate birthDate = birthday.toLocalDate();

        String birthStr = birthDate.format(fmt);
        //时间字符串比较也是同样可行的。
        if (range && StringUtils.isNotBlank(birthdayFrom) && StringUtils.isNotBlank(birthdayTo) &&
                birthStr.compareTo(birthdayFrom) >= 0 && birthStr.compareTo(birthdayTo) <= 0) {
            return true;
        }

        if (!range) {
            //设置是同一年
            int year = referenceTime.getYear();
            birthDate = LocalDate.of(year, birthDate.getMonthValue(), birthDate.getDayOfMonth());

            if (isDatelegal(birthDate)) {
                return true;
            }
        }


        return false;
    }

    public static void main(String[] args) {
        LocalDate dateOfBirth = LocalDate.of(2000, 03, 03);


        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        String birthStr = dateOfBirth.format(fmt);
        String birthdayFrom = "02-03";
        String birthdayTo = "11-04";
        if (StringUtils.isNotBlank(birthdayFrom) && StringUtils.isNotBlank(birthdayTo) &&
                birthStr.compareTo(birthdayFrom) >= 0 && birthStr.compareTo(birthdayTo) <= 0) {
            System.out.println("-------------");
        }


        System.out.println();
        LocalDate today = LocalDate.now();
        int daya = today.getDayOfMonth();
        int year = today.getYear();
        String dateStr = today.format(fmt);
        System.out.println(dateStr);
        System.out.println(dateOfBirth);
        int value = dateOfBirth.getMonth().getValue();
        dateOfBirth.lengthOfMonth();
        System.out.println(value);
    }

}
