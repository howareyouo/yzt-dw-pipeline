package cn.yizhi.yzt.pipeline.model.fact.factjoin;

import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import cn.yizhi.yzt.pipeline.model.ods.OdsOrderItem;
import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/10/29
 */
@Setter
@Getter
public class FactProductJoinStream {

    private OdsEmallOrder odsEmallOrder;

    private OdsOrderItem odsOrderItem;
}
