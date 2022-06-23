package cn.yizhi.yzt.pipeline.model.dim;

import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Data;

import java.sql.Date;

@Data
public class DimShop {
    @Column(name = "id", type = Column.FieldType.PK)
    private int id;

    @Column(name = "shop_id", type = Column.FieldType.KEY)
    private int shopId;
    private String shopName;

    private int mainShopId;
    private String mainShopName;

    private int groupId;
    private String groupName;
    private String groupMode;
    private boolean supportShopPrice;
    private String abbr;
    private String businessScope;
    private String managementMode;
    private String country;
    private String province;
    private String city;
    private String district;
    private String address;

    private String logo;
    private String wxQrCode;
    private String status;
    private String accessStatus;
    // 拉链表的两个字段：
    @Column(name = "start_date", type = Column.FieldType.KEY)
    private Date startDate;
    private Date endDate;
}
