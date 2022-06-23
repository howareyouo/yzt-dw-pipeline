package cn.yizhi.yzt.pipeline.model.dim;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class DimPromotion {
    @Column(name = "id", type = Column.FieldType.PK)
    private int id;

    @Column(name = "promotion_id", type = Column.FieldType.KEY)
    private int promotionId;

    private String promotionName;

    @Column(name = "promotion_type", type = Column.FieldType.KEY)
    private String promotionType;

    private Integer promotionTypeCode;

    @Column(name = "start_date", type = Column.FieldType.KEY)
    private Date startDate;
    private Date endDate;
}
