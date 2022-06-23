package cn.yizhi.yzt.pipeline.model.fact;

import cn.yizhi.yzt.pipeline.model.ods.CouponUseRecord;
import cn.yizhi.yzt.pipeline.model.ods.OdsEmallOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author aorui created on 2020/10/27
 */
@Getter
@Setter
public class FactCouponJoinStream {
    private CouponUseRecord couponUseRecord;
    private OdsEmallOrder odsEmallOrder;
}
