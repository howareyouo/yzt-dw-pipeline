package cn.yizhi.yzt.pipeline.model.fact.shop;

import lombok.Data;

/**
 * @author hucheng
 * @date 2020/11/6 16:24
 */
@Data
public class FactShopNewMember {
    /**
     * 时间
     */
    private String rowTime;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 店铺id
     */
    private Integer shopId;


    /**
     * 新注册用户数
     */
    private int registerCount;
}
