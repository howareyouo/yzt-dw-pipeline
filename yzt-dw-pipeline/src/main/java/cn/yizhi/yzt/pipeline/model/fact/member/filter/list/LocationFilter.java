package cn.yizhi.yzt.pipeline.model.fact.member.filter.list;


import cn.yizhi.yzt.pipeline.model.fact.member.FactMemberUnion;
import cn.yizhi.yzt.pipeline.model.fact.member.FilterContext;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.Filter;
import cn.yizhi.yzt.pipeline.model.fact.member.filter.FilterDefinition;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.CollectionUtil;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 默认为空
 * 可选择到省市2级，也可只选1级
 */
@Getter
@Setter
public class LocationFilter extends Filter<FactMemberUnion> {

    public static String SPILT = "/";

    String province;

    String city;


    public LocationFilter(FilterDefinition definition) {
        super(definition);
    }

    public void init(FilterDefinition definition) {
        String value = definition.getValue();
        province = "";
        city = "";
        if (StringUtils.isNotBlank(value)) {
            String[] locations = value.split(SPILT);
            province = locations[0];

            if (locations.length > 1) {
                city = locations[1];
            }
        }
    }

    @Override
    public void filter(FilterContext<FactMemberUnion> context) {
        Stream<FactMemberUnion> stream = context.getStreamSupplier().get();
        if (!CollectionUtil.isNullOrEmpty(context.getMemberIds())) {
            stream = stream.filter(a -> context.getMemberIds().contains(a.getId()));
        }

        Set<Integer> memberIds = stream.filter(a -> adaptation(a.getProvince(), a.getCity())).map(FactMemberUnion::getId).collect(Collectors.toSet());

        if (CollectionUtil.isNullOrEmpty(memberIds)) {
            context.setFinished(true);
            context.setMemberIds(Collections.emptySet());
        } else {
            context.setMemberIds(memberIds);
        }
    }

    public boolean adaptation(String province, String city) {
        province = (province == null ? "" : province);
        city = (city == null ? "" : city);

        if (StringUtils.isNotBlank(this.province) && StringUtils.isNotBlank(this.city)) {
            return this.province.replace("省", "").equals(province.replace("省", ""))
                    && this.city.replace("市", "").equals(city.replace("市", ""));
        }


        if (StringUtils.isNotBlank(this.province) && StringUtils.isBlank(this.city)) {
            return this.province.replace("省", "").equals(province.replace("省", ""));
        }

        return false;
    }

    @Override
    public Class<FactMemberUnion> supportedType() {
        return FactMemberUnion.class;
    }

    public static void main(String[] args) {
        String s = "北京市/北京市";

        String[] locations = s.split(SPILT);
        System.out.println(locations[0]);
        System.out.println(locations[1]);

    }
}
