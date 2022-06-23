package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import cn.yizhi.yzt.pipeline.model.ods.OdsFullReducePromotionOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsFullReducePromotionOrderItem;
import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/11/5
 */
@Getter
@Setter
public class FactFullReduceItemFullJoinSteam {

    private OdsFullReducePromotionOrder left;

    private OdsFullReducePromotionOrderItem right;

    public FactFullReduceItemFullJoinSteam(OdsFullReducePromotionOrder left, OdsFullReducePromotionOrderItem right) {
        this.left = left;
        this.right = right;
    }
}
