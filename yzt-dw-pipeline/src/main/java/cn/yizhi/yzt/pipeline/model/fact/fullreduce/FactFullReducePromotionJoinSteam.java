package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import cn.yizhi.yzt.pipeline.model.fact.FactProductLog;
import cn.yizhi.yzt.pipeline.model.ods.OdsFullReducePromotion;

/**
 * @author aorui created on 2020/11/6
 */
public class FactFullReducePromotionJoinSteam {

    private FactProductLog left;

    private OdsFullReducePromotion right;

    public FactFullReducePromotionJoinSteam(FactProductLog left, OdsFullReducePromotion right) {
        this.left = left;
        this.right = right;
    }
}
