package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/11/5
 */
@Getter
@Setter
public class FactFullReducePvUv {

    /**
     *
     */
    private String rowDate;

    /**
     * 店铺Id
     */
    private Integer shopId;

    /**
     * 满减活动id
     */
    private Integer promotionId;

    /**
     *
     */
    private Integer pv;

    /**
     *
     */
    private Integer uv;

    /**
     * 主页分享次数
     */
    private Integer shareCount;

}
