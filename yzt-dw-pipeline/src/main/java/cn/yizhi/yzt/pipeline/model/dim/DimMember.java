package cn.yizhi.yzt.pipeline.model.dim;


import cn.yizhi.yzt.pipeline.jdbc.Column;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * 会员维度
 */
@Data
public class DimMember {
    @Column(name = "id", type = Column.FieldType.PK)
    private int id;

    @Column(name = "member_id", type = Column.FieldType.KEY)
    private int memberId;

    private int memberBaseId;

    private int userId;
    private int shopId;
    private int mainShopId;
    private String source;
    private String remark;
    private boolean disabled;
    private String name;
    private String nickname;
    private String avatar;
    private String gender;
    private String wechat;
    private Date birthday;
    private String province;
    private String city;
    private String countryCode;
    private String phone;
    private Timestamp createdAt;

    @Column(name = "start_date", type = Column.FieldType.KEY)
    private Date startDate;
    private Date endDate;
}
