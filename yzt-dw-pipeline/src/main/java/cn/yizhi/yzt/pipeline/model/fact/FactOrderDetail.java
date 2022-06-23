package cn.yizhi.yzt.pipeline.model.fact;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author hucheng
 * 事务型事实表
 * @date 2020/6/26 11:23
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactOrderDetail {
    @Column(name = "fk_shop", type = Column.FieldType.KEY)
    @JsonProperty("fk_shop")
    private Integer fkShop;

    @Column(name = "fk_date", type = Column.FieldType.KEY)
    @JsonProperty("fk_date")
    private int fkDate;

    @Column(name = "fk_member", type = Column.FieldType.KEY)
    @JsonProperty("fk_member")
    private Integer fkMember;

    @Column(name = "fk_product", type = Column.FieldType.KEY)
    @JsonProperty("fk_product")
    private Integer fkProduct;

    @Column(name = "fk_sku", type = Column.FieldType.KEY)
    @JsonProperty("fk_sku")
    private Integer fkSku;

    @Column(name = "fk_channel", type = Column.FieldType.KEY)
    @JsonProperty("fk_channel")
    private int fkChannel;

    @Column(name = "fk_payment", type = Column.FieldType.KEY)
    @JsonProperty("fk_payment")
    private int fkPayment;

    @Column(name = "order_id", type = Column.FieldType.KEY)
    @JsonProperty("order_id")
    private Long orderId;
    /**
     * 数量
     */
    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("price")
    private BigDecimal price;

    /**
     * 金额=retail_price（售价）  x  quantity
     */
    @JsonProperty("amount")
    private BigDecimal amount;
    /**
     * 总优惠金额  分开求会有金额交叉情况
     */
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;
    /**
     * 是否赠品
     */
    @Column(name = "is_giveaway", type = Column.FieldType.KEY)
    @JsonProperty("is_giveaway")
    private boolean isGiveaway;

    public boolean isGiveaway() {
        return isGiveaway;
    }

    /**
     * flink TypeExtractor检查setter方法很笨，不遵循java bean的命名方式，因此在此明确定义bool setter方法
     */
    public void setIsGiveaway(boolean isGiveaway) {
        this.isGiveaway = isGiveaway;
    }
}

