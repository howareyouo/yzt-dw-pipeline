package cn.yizhi.yzt.pipeline.model.dim;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
public class DimProductSKu {
    // 保持field为public, flink groupby keyBy等操作需要
    @Column(name = "id", type = Column.FieldType.PK)
    public int id;

    @Column(name = "sku_id", type = Column.FieldType.KEY)
    public int skuId;

    public int productId;

    public int shopId;
    public String skuImage;
    public boolean isDefaultSku;
    public BigDecimal purchasePrice;
    public BigDecimal retailPrice;
    public String skuSpec;
    public Timestamp createdAt;
    @Column(name = "start_date", type = Column.FieldType.KEY)
    public Date startDate;
    public Date endDate;
}
