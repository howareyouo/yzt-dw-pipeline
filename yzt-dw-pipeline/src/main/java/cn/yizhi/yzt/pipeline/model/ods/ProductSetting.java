package cn.yizhi.yzt.pipeline.model.ods;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSetting {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("product_id")
    private Integer productId;

    /**
     * 库存扣减方式 1-下单扣减 2-付款扣减
     */
    @JsonProperty("deduction_type")
    private int deductionType;

    /**
     * 上架方式 1-立即上架 2-自定义上架时间 3-暂不上架
     */
    @JsonProperty("on_shelf_type")
    private int onShelfType;

    /**
     * 上架时间
     */
    @JsonProperty("on_shelf_time")
    private Timestamp onShelfTime;

    /**
     * 优先级
     */
    @JsonProperty("priority")
    private int priority;

    /**
     * 门店id
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 是否允许退款
     */
    @JsonProperty("allow_refund")
    private boolean allowRefund;

    /**
     * 运费计算方式 1-统一运费 2-运费模版
     */
    @JsonProperty("ship_cost_type")
    private int shipCostType;

    /**
     * 统一运费
     */
    @JsonProperty("ship_cost")
    private BigDecimal shipCost;

    /**
     * 运费模版
     */
    @JsonProperty("ship_cost_tpl_id")
    private Integer shipCostTplId;
    @JsonProperty("allow_exp_delivery")
    private boolean allowExpDelivery; // 是否至此快递发货
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("allow_pick_up")
    private boolean allowPickUp; // 是否支持上门自提
    @JsonProperty("allow_local_delivery")
    private boolean allowLocalDelivery; //是否支持同城配送
    @JsonProperty("now_show_ivt")
    private boolean notShowIvt; // 详情页是否不显示库存
    @JsonProperty("image_spec_id")
    private Integer imageSpecId; // 详情页规格图配置id

    /**
     * 是否展示电子凭证
     */
    @JsonProperty("show_voucher")
    private boolean showVoucher;
    /**
     * 有效期相关设定
     */
    @JsonProperty("shelf_life")
    private String shelfLife;
}