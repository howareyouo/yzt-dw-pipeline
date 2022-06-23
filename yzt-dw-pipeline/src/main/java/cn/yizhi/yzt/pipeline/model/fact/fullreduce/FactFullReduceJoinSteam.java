package cn.yizhi.yzt.pipeline.model.fact.fullreduce;

import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/11/2
 */
@Getter
@Setter
public class FactFullReduceJoinSteam {

    private OdsEmallOrder odsEmallOrder;

    private FactFullReduceItemJoinSteam factFullReduceItemJoinSteam;

    public FactFullReduceJoinSteam(OdsEmallOrder odsEmallOrder, FactFullReduceItemJoinSteam factFullReduceItemJoinSteam) {
        this.odsEmallOrder = odsEmallOrder;
        this.factFullReduceItemJoinSteam = factFullReduceItemJoinSteam;
    }
}
