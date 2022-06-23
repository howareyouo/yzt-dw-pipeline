package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import lombok.Data;

@Data
public class FactFullReduceAll {
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
     * 截止今日累计uv
     */
    private Integer uv;

    /**
     * 截止今日累计付款人数
     */
    private Integer payNumber;
}
