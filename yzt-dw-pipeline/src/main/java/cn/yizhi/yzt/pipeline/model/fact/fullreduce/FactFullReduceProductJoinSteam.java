package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import cn.yizhi.yzt.pipeline.model.fact.FactFullReduce;
import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotionItem;
import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/11/5
 */
@Getter
@Setter
public class FactFullReduceProductJoinSteam {
    private FactOrderPromotionItem left;
    private FactFullReduce right;

    public FactFullReduceProductJoinSteam(FactOrderPromotionItem left, FactFullReduce right) {
        this.left = left;
        this.right = right;
    }
}
