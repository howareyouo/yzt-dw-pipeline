package cn.yizhi.yzt.pipeline.model.fact.member.filter;

import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.list.BirthdayFilter;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

import static cn.yizhi.yzt.pipeline.util.TimeUtil.yearMonthDayfmt;

@Getter
@Setter
@Slf4j
public abstract class Filter<T> {
    //分割符号
    public final static String SPLIT_STR = ",";

    //求百分占比
    public final static String RATIO_STR = "%";

    protected FilterDefinition definition;

    //时间参照物，可变化的，每次调用Filter 之前使用。为了处理近多少天前的数据。。。。。。
    @JsonIgnore
    protected LocalDate referenceTime;


    public Filter(FilterDefinition definition) {
        this.definition = definition;
        init(definition);
    }

    public abstract void init(FilterDefinition definition);

    public abstract void filter(FilterContext<T> in);

    public abstract Class<T> supportedType();

    public boolean isDatelegal(LocalDate dataDay) {

        if (dataDay == null || referenceTime == null) {
            log.error("规则过滤设置无效默认返回false");
            return false;
        }

        if (definition.timeRange == null && definition.relativeTimeRange == null) {
            return true;
        }

        String[] timeRange = definition.getTimeRange();
        String relativeTimeRange = definition.getRelativeTimeRange();

        //时间范围判断处理。
        if (timeRange != null && timeRange[0] != null && timeRange[1] != null
                && (dataDay.compareTo(LocalDate.parse(timeRange[0], yearMonthDayfmt)) >= 0
                && dataDay.compareTo(LocalDate.parse(timeRange[1], yearMonthDayfmt)) <= 0)
        ) {
            return true;
        }
        //3d: 最近n天, 0m:本月, 1m:前一个月, 1y:前一年, 1q前季度 与 timeRange 互斥
        if (relativeTimeRange != null && !relativeTimeRange.equals("")) {
            String numberStr = relativeTimeRange.substring(0, relativeTimeRange.length() - 1);
            boolean isAddup = numberStr.contains("+");//代表未来的

            String whichTimer = relativeTimeRange.substring(relativeTimeRange.length() - 1);
            int number = Integer.parseInt(numberStr);//判断是否近多少天，  负数 指定某一天的
            int value = Math.abs(number);
            switch (whichTimer) {
                case "d":
                    //往前推 最近多少天
                    if (!isAddup) {
                        LocalDate recentDay = referenceTime.plusDays(-value);
                        if (number >= 0 && (dataDay.compareTo(recentDay) >= 0 && dataDay.compareTo(referenceTime) <= 0)) {

                            return true;
                        } else if (number < 0 && recentDay.compareTo(dataDay) == 0) {//指定某一天的数据。刚好匹配
                            //取绝对值计算
                            return true;
                        }
                    } else {//计算未来的
                        LocalDate futureDay = referenceTime.plusDays(value);
                        if (number >= 0 && (dataDay.compareTo(futureDay) <= 0 && dataDay.compareTo(referenceTime) >= 0)) {
                            return true;
                        } else if (number < 0 && futureDay.compareTo(dataDay) == 0) {//指定某一天的数据。刚好匹配
                            //取绝对值计算
                            return true;
                        }

                    }


                    break;
                case "m":
                    //最近月
                    LocalDate localDate = referenceTime.plusMonths(-value);
                    String dealDataStr = localDate.format(TimeUtil.yearMonthfmt);
                    String dateStr = dataDay.format(TimeUtil.yearMonthfmt);
                    if (number <= 0 && dealDataStr.equals(dateStr)) {
                        return true;
                    } else if (number > 0 && (dealDataStr.equals(dateStr) || dataDay.compareTo(localDate) >= 0)) {
                        return true;
                    }

                    break;
                case "y":
                    break;
                case "q":
                    break;

            }


        }

        return false;
    }


    public static void main(String[] args) {
        LocalDate localDate = LocalDate.now();

        FilterDefinition definition = new FilterDefinition();
        definition.setRelativeTimeRange("0m");
        BirthdayFilter ageFilter = new BirthdayFilter(definition);
        ageFilter.setReferenceTime(localDate);
        LocalDate localDate1 = localDate.plusDays(-21);
        boolean datelegal = ageFilter.isDatelegal(localDate1);
        System.out.println(datelegal);

    }
}
