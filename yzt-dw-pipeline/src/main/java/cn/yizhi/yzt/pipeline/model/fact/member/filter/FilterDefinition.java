package cn.yizhi.yzt.pipeline.model.fact.member.filter;


import cn.yizhi.yzt.pipeline.util.JsonMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FilterDefinition {

    public static final String ANY_VALUE = "any";


    /**
     * 过滤条件
     */
    FilterCondition condition;


    /**
     * 时间区间, 与 jumpRange 互斥
     */
    String[] timeRange;


    /**
     * nd: 最近n天,-n指的是 距离今天之前的确定的仅那一天数据, 比如 今天 使用 0d ，昨日使用 -1d ，近一天使用 1d
     * 0m:本月, -1m:前一个月,1m最近一个月是包含上个月的
     * -1y:前一年,
     * -1q前季度 与 timeRange 互斥
     */
    String relativeTimeRange;


    /**
     * 值区间 /消费金额 /购买次数/ 积分余额 /距最近消费 等范围
     * "*"代表开发空间
     */
    String[] valueRange;


    /**
     * 精确值/集合值(逗号分隔) // any 代表全部
     * <p>
     */
    String value;

    boolean any;

    //优化券模板
    List<Integer> couponTemplates;

    //"优惠券类型集合"
    List<Integer> couponTypes;

    //"商品id集合"
    List<Integer> products;

    //"商品分组id集合"
    List<Integer> productGroups;

    public static void main(String[] args) {
        FilterDefinition filterDefinition = new FilterDefinition();
        filterDefinition.setCondition(FilterCondition.AGE);
        String[] strings = new String[2];
        strings[0] = "10";
        strings[1] = "11";
        filterDefinition.setValueRange(strings);
        List<FilterDefinition> os = new ArrayList<>();
        os.add(filterDefinition);
        String s = JsonMapper.nonEmptyMapper().toJson(os);

        System.out.println(s);

    }
}