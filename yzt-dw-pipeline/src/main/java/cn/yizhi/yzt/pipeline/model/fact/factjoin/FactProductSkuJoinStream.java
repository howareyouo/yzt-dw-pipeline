package cn.yizhi.yzt.pipeline.model.fact.factjoin;

import cn.yizhi.yzt.pipeline.model.fact.FactOrderPromotionItem;
import cn.yizhi.yzt.pipeline.model.ods.ProductSku;
import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/10/29
 */
@Getter
@Setter
public class FactProductSkuJoinStream {
    private FactOrderPromotionItem factOrderPromotionItem;
    private ProductSku productSku;

    public FactProductSkuJoinStream(FactOrderPromotionItem factOrderPromotionItem, ProductSku productSku) {
        this.factOrderPromotionItem = factOrderPromotionItem;
        this.productSku = productSku;
    }
}
