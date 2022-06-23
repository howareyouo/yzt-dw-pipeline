package cn.yizhi.yzt.pipeline.model.dim;


import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * 商品维度表
 * @author zjzhang
 */
@Getter
@Setter
public class DimProduct {
    @Column(name = "id", type = Column.FieldType.PK)
    private int id;

    private int shopId;

    @Column(name = "product_id", type = Column.FieldType.KEY)
    private int productId;
    private String productSerial;
    private String productName;
    private String image;
    private String productDesc;
    private String shareDesc;
    private String detail;
    private String video;
    private String productType;
    private BigDecimal markingPrice;
    private BigDecimal weight;
    private BigDecimal shipCost;
    private String shipCostType;
    private boolean onShelfStatus;
    private boolean allowRefund;
    private boolean allowExpDelivery;
    private boolean allowPickup;
    private boolean allowLocalDelivery;
    private Timestamp createdAt;

    @Column(name = "start_date", type = Column.FieldType.KEY)
    private Date startDate;
    private Date endDate;
}
