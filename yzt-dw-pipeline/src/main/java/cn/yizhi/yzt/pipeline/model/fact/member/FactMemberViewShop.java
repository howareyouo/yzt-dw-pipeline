package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

/**
 * @author hucheng
 * @date 2020/7/31 11:44
 */
@Getter
@Setter
@ToString
public class FactMemberViewShop  {
    private Integer shopId;

    private Integer memberId;
    /**
     * 上次浏览店铺时间
     */
    private Timestamp lastViewShopTime;
}
