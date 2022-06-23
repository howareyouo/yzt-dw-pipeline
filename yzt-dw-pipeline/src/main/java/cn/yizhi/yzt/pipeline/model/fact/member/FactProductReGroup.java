package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 商品分组规则 来源数据库
 * @author aorui created on 2021/1/11
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactProductReGroup {

    private Integer id;

    /**
     * 店铺id
     */
    private Integer shop_id;

    /**
     * 分组id
     */
    private Integer group_id;

    /**
     * 商品id
     */
    private Integer product_id;

}
