package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Data;

/**
 * @Author: HuCheng
 * @Date: 2021/1/8 15:14
 */
@Data
public class FactMemberGroup {
    /**
     * 分组/标签 id
     */
    private Integer id;

    /**
     * 0 群组  1 智能标签
     */
    private int type;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 会员id
     */
    private Integer memberId;

    /**
     * 是否属于当前组 0 不属于  1 属于
     */
    private int isBelong;
}
