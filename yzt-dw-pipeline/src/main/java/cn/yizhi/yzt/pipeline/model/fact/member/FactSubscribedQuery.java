package cn.yizhi.yzt.pipeline.model.fact.member;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author aorui created on 2021/1/7
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactSubscribedQuery {

    @JsonProperty("id")
    Integer id;


    @JsonProperty("openid")
    String openid;


    @JsonProperty("appid")
    String appid;


    @JsonProperty("subscribed")
    Integer subscribed;

    @JsonProperty("shop_id")
    Integer shopId;


    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactSubscribedQueryWithType {
        @JsonProperty("id")
        Integer id;


        @JsonProperty("openid")
        String openid;


        @JsonProperty("appid")
        String appid;


        @JsonProperty("subscribed")
        Integer subscribed;

        @JsonProperty("shop_id")
        Integer shopId;

        @Ignore
        @JsonProperty("data_type")
        DataType dataType = DataType.MEMBER_WX_SUBSCRIBED;

    }


    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactSubscribedQueryWithRelation {
        @JsonProperty("id")
        Integer id;


        @JsonProperty("openid")
        String openid;


        @JsonProperty("appid")
        String appid;


        @JsonProperty("subscribed")
        Integer subscribed;

        @JsonProperty("shop_id")
        Integer shopId;

        @JsonProperty("relation")
        Integer relation;

        @JsonProperty("apptype")
        Integer apptype;

        @JsonProperty("__deleted")
        boolean __deleted;

    }


    public static FactSubscribedQuery.FactSubscribedQueryWithType from(FactSubscribedQuery o) {
        FactSubscribedQueryWithType factSubscribedQueryWithType = new FactSubscribedQueryWithType();
        factSubscribedQueryWithType.setId(o.getId());
        factSubscribedQueryWithType.setOpenid(o.getOpenid());
        factSubscribedQueryWithType.setAppid(o.getAppid());
        factSubscribedQueryWithType.setSubscribed(o.getSubscribed());
        factSubscribedQueryWithType.setShopId(o.getShopId());
        factSubscribedQueryWithType.setDataType(DataType.MEMBER_WX_SUBSCRIBED);
        return factSubscribedQueryWithType;

    }

    public static FactSubscribedQuery.FactSubscribedQueryWithType from(FactSubscribedQueryWithRelation o) {
        FactSubscribedQueryWithType factSubscribedQueryWithType = new FactSubscribedQueryWithType();
        factSubscribedQueryWithType.setId(o.getId());
        factSubscribedQueryWithType.setOpenid(o.getOpenid());
        factSubscribedQueryWithType.setAppid(o.getAppid());
        factSubscribedQueryWithType.setSubscribed(o.getSubscribed());
        if ((o.getApptype() != null && o.getApptype() != 1) || (o.getRelation() != null && o.getRelation() != 1)) {
            factSubscribedQueryWithType.setSubscribed(0);
        }

        if (o.__deleted){
            factSubscribedQueryWithType.setSubscribed(0);
        }

            factSubscribedQueryWithType.setShopId(o.getShopId());
        factSubscribedQueryWithType.setDataType(DataType.MEMBER_WX_SUBSCRIBED);
        return factSubscribedQueryWithType;

    }

}
