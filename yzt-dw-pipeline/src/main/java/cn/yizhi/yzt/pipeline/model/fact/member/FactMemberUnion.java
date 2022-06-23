package cn.yizhi.yzt.pipeline.model.fact.member;


import cn.yizhi.yzt.pipeline.kafka.KafkaPojoDeserializationSchema;
import cn.yizhi.yzt.pipeline.model.ods.OdsMemberUnion;
import cn.yizhi.yzt.pipeline.util.Beans;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.List;

/**
 * 用户基础信息维度表
 * winner
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactMemberUnion {

    @JsonProperty("id")
    Integer id;
    @JsonProperty("user_id")
    Integer userId;
    @JsonProperty("country_code")
    String countryCode;
    @JsonProperty("phone")
    String phone;
    @JsonProperty("member_id")
    Integer memberId;
    @JsonProperty("name")
    String name;
    @JsonProperty("nickname")
    String nickname;
    @JsonProperty("avatar")
    String avatar;
    @JsonProperty("gender")
    Integer gender;
    @JsonProperty("wechat")
    String wechat;
    // kafka connect debezium解析date为int类型，需要特殊处理
    @JsonDeserialize(using = KafkaPojoDeserializationSchema.DateDeserializer.class)
    @JsonProperty("birthday")
    Date birthday;
    @JsonProperty("province")
    String province;
    @JsonProperty("city")
    String city;
    @JsonProperty("main_shop_id")
    Integer mainShopId;
    @JsonProperty("shop_id")
    Integer shopId;
    @JsonProperty("staff_id")
    Integer staffId;
    @JsonProperty("payment_password")
    String paymentPassword;
    @JsonProperty("source")
    String source;
    @JsonProperty("remark")
    String remark;
    @JsonProperty("disabled")
    Integer disabled;
    @JsonProperty("balance_amount")
    BigDecimal balanceAmount;
    @JsonProperty("gift_amount")
    BigDecimal giftAmount;
    @JsonProperty("points")
    BigDecimal points;
    @JsonProperty("debt_amount")
    BigDecimal debtAmount;
    @JsonProperty("consume_times")
    Integer consumeTimes;
    @JsonProperty("consume_amount")
    BigDecimal consumeAmount;
    @JsonProperty("last_consume_date")
    Timestamp lastConsumeDate;
    @JsonProperty("last_consume_services")
    String lastConsumeServices;
    @JsonProperty("last_service_staff")
    String lastServiceStaff;
    @JsonProperty("last_view_shop_time")
    Timestamp lastViewShopTime;
    @JsonProperty("last_consignee_address")
    String lastConsigneeAddress;
    @JsonProperty("referrer")
    String referrer;
    @JsonProperty("follower")
    String follower;
    @JsonProperty("custom1")
    String custom1;
    @JsonProperty("custom2")
    String custom2;
    @JsonProperty("custom3")
    String custom3;
    @JsonProperty("custom4")
    String custom4;
    @JsonProperty("custom5")
    String custom5;
    @JsonProperty("custom6")
    String custom6;
    @JsonProperty("created_at")
    Timestamp createdAt;
    @JsonProperty("updated_at")
    Timestamp updatedAt;


    /**
     * 以下是通过 job stream 处理
     */

    //*************  来自Job FactMemberSubscribedStreamJob *************
    //是否关注公众号. 1是, 0否
    @JsonProperty("subscribed")
    Integer subscribed;


    @JsonProperty("appid")
    String appid;

    @JsonProperty("openid")
    String openid;


    //*************  来自Job FactCastMemberTagAndGroupStreamJob *************
    //用户打上的所有标签数据
    List<Integer> tagIds;


    //*************  来自Job FactMemberTotalStreamJob *************
    @JsonProperty("last_order_time")
    private Timestamp lastOrderTime;

    //伊智通产生总订单数
    @JsonProperty("order_count")
    int orderCount;

    //伊智通产生总订单额
    @JsonProperty("total_order_amount")
    BigDecimal totalOrderAmount ;

    //累计退款金额
    @JsonProperty("refund_amount")
    BigDecimal refundAmount;

    //累计退款单数
    @JsonProperty("refund_count")
    Integer refundCount;

    /**
     * 首次消费时间
     */
    @JsonProperty("first_order_time")
    private Timestamp firstOrderTime;

    //*************   需单独计算  *************
    //客单价
    @JsonProperty("avg_consume_amount")
    BigDecimal avgConsumeAmount;


    //消费频率
    @JsonProperty("purchase_rate")
    BigDecimal purchaseRate;


    public static FactMemberUnion buildFactMemberBase(FactMemberTotal factMemberTotal) {
        FactMemberUnion factMemberUnion = new FactMemberUnion();
        factMemberUnion.setShopId(factMemberTotal.getShopId());
        factMemberUnion.setId(factMemberTotal.getId());
        factMemberUnion.setLastOrderTime(factMemberTotal.getLastOrderTime());
        factMemberUnion.setFirstOrderTime(factMemberTotal.getFirstOrderTime());
        factMemberUnion.setOrderCount(factMemberTotal.getOrderCount());
        factMemberUnion.setTotalOrderAmount(factMemberTotal.getTotalOrderAmount());
        factMemberUnion.setRefundAmount(factMemberTotal.getRefundAmount());
        factMemberUnion.setRefundCount(factMemberTotal.getRefundCount());
        factMemberUnion.setAvgConsumeAmount(factMemberTotal.getAvgConsumeAmount());
        factMemberUnion.setPurchaseRate(factMemberTotal.getPurchaseRate());
        return factMemberUnion;

    }

    public static FactMemberUnion merge(FactMemberUnion old, OdsMemberUnion fresh) {
        if (old == null) {
            old = new FactMemberUnion();
            //吧默认new 的设置 null 避免值覆盖
            old.setTotalOrderAmount(null);
            old.setPurchaseRate(null);
            old.setFirstOrderTime(null);
            old.setLastOrderTime(null);

        }
        Beans.copyProperties(fresh, old, true);
        return old;
    }

    public static FactMemberUnion mergeSubscribed(FactMemberUnion old, FactSubscribedQuery fresh) {
        if (old == null) {
            old = new FactMemberUnion();
            //吧默认new 的设置 null 避免值覆盖
            old.setTotalOrderAmount(null);
            old.setPurchaseRate(null);
            old.setFirstOrderTime(null);
            old.setLastOrderTime(null);

        }
        Beans.copyProperties(fresh, old, true);
        return old;
    }

}
