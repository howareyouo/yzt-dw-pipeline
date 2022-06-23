package cn.yizhi.yzt.pipeline.model.fact.factjoin;

import cn.yizhi.yzt.pipeline.model.fact.product.FactProduct;
import cn.yizhi.yzt.pipeline.model.fact.FactProductLog;
import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/10/29
 */
@Getter
@Setter
public class FactProductLogJoinStream {

    private FactProduct factProduct;
    private FactProductLog factProductLog;

    public FactProductLogJoinStream(FactProduct factProduct, FactProductLog factProductLog) {
        this.factProduct = factProduct;
        this.factProductLog = factProductLog;
    }
}
