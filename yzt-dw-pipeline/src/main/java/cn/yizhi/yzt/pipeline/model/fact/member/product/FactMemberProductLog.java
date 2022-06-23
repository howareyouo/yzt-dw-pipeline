package cn.yizhi.yzt.pipeline.model.fact.member.product;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Author: HuCheng
 * @Date: 2021/1/5 16:43
 */
@Getter
@Setter
@ToString
public class FactMemberProductLog {
    /**
     * 处理时间
     */
    @JsonProperty("analysis_date")
    private String analysisDate;

    /**
     * 店铺ID
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * 会员id
     */
    @JsonProperty("member_id")
    private Integer memberId;

    /**
     * 商品id
     */
    @JsonProperty("product_id")
    private Integer productId;

    /**
     * 商品浏览数
     */
    @JsonProperty("view_times")
    private Integer viewedTimes;

    /**
     * 分享次数
     */
    @JsonProperty("share_times")
    private Integer shareTimes;

    /**
     * 加入购物车时间
     */
    @JsonProperty("cart_add_date")
    private Timestamp cartAddDate;

    /**
     * 加入购物车次数
     */
    @JsonProperty("cart_add_times")
    private Integer cartAddTimes;


    /**
     * 分组id，数组
     */
    @JsonProperty("group_ids")
    private String groupIds = "[]";

    /**
     * 分组ids（不入库）
     */
    @Ignore
    private List<Integer> groups;


    @Ignore
    @JsonProperty("data_type")
    private DataType dataType;
}
