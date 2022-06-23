package cn.yizhi.yzt.pipeline.model.fact.member;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author: HuCheng
 * @Date: 2021/2/4 14:51
 */
@Data
public class FactMemberLastConsumeService {
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * shop_member的id 作为主键id
     */
    @JsonProperty("id")
    private Integer id;


    /**
     * 上次消费服务
     */
    private String lastConsumeServices;

}
