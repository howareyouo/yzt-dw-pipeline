package cn.yizhi.yzt.pipeline.model.fact.userfullreduce;


import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/11/5
 */
@Getter
@Setter
public class FactUserShare {
    /**
     *
     */
    private String rowDate;

    /**
     * 店铺Id
     */
    private Integer shopId;

    /**
     * 用户id
     */
    private Integer memberId;

    /**
     * 满减id
     */
    private Integer fullReduceId;


    /**
     * 活动分享次数
     */
    private Integer shareCountFullReduce;


}
