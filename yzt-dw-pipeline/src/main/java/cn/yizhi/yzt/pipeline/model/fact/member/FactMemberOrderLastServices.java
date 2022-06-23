package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author hucheng
 * @date 2020/8/3 16:22
 */
@Getter
@Setter
@ToString
public class FactMemberOrderLastServices {
    private Integer shopId;

    private Integer memberId;
    /**
     * 上次消费服务
     */
    private String lastConsumeServices;
}
