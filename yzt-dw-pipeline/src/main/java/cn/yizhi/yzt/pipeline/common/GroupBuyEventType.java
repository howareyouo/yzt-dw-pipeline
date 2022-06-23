package cn.yizhi.yzt.pipeline.common;

import lombok.Getter;

/**
 * @author aorui created on 2020/12/15
 */
@Getter
public enum GroupBuyEventType {

    // 浏览
    VIEW(0,"VIEW"),
    // 分享
    SHARE(1,"SHARE"),
    // 预约
    APPOINT(2,"APPOINT"),
    // 开团
    START_GROUP(3,"START_GROUP"),
    // 参团
    JOIN_GROUP(4,"JOIN_GROUP");

    GroupBuyEventType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    private Integer code;
    private String name;
}
