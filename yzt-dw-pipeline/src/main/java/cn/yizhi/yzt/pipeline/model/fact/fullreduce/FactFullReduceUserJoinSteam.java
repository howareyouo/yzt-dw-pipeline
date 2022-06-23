package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsFullReducePromotionOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/11/5
 */
@Getter
@Setter
public class FactFullReduceUserJoinSteam {

    private OdsFullReducePromotionOrder left;

    private OdsEmallOrder right;

    public FactFullReduceUserJoinSteam(OdsFullReducePromotionOrder left, OdsEmallOrder right) {
        this.left = left;
        this.right = right;
    }
}
