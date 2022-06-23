package cn.yizhi.yzt.pipeline.model.fact;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author hucheng
 * 周期性商品指标
 * @date 2020/6/26 11:31
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactProductDaily {
    @JsonProperty("fk_shop")
    private int fkShop;
    @JsonProperty("fk_date")
    private int fkDate;
    @JsonProperty("fk_channel")
    private int fkChannel;
    @JsonProperty("fk_product")
    private int fkProduct;

    //售卖数量
    @JsonProperty("sold_count")
    private int soldCount;
    @JsonProperty("pv")
    private int pv;
    @JsonProperty("uv")
    private int uv;
    @JsonProperty("share_count")
    private int shareCount;
    //分享人数
    @JsonProperty("share_member_count")
    private int shareMemberCount;
    //加入购物车的数量
    @JsonProperty("addcart_count")
    private int addCartCount;
    // 加入购物车的人数
    @JsonProperty("addcart_member")
    private int addCartMemberCount;
}
