package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author: HuCheng
 * @Date: 2021/2/4 16:45
 */
@Data
public class MgFactMemberOrderLastAddr {
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * shop_member的id 作为主键id
     */
    @JsonProperty("id")
    private Integer id;


    /**
     * 上次收货地址
     */
    private String lastConsigneeAddress;
}
