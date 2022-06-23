package cn.yizhi.yzt.pipeline.model.fact.member;

import cn.yizhi.yzt.pipeline.common.DataType;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import cn.yizhi.yzt.pipeline.model.fact.FactMemberShopVisits;
import cn.yizhi.yzt.pipeline.model.fact.member.coupon.FactMemberCoupon;
import cn.yizhi.yzt.pipeline.model.fact.member.coupon.FactMemberCouponLog;
import cn.yizhi.yzt.pipeline.model.fact.member.order.FactMemberOrder;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductLog;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductOrder;
import cn.yizhi.yzt.pipeline.model.fact.member.product.FactMemberProductRefund;
import cn.yizhi.yzt.pipeline.model.ods.OdsMemberGroup;
import cn.yizhi.yzt.pipeline.model.ods.OdsMemberTag;
import cn.yizhi.yzt.pipeline.model.ods.OdsMemberUnion;
import cn.yizhi.yzt.pipeline.model.ods.OdsTagDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author hucheng
 * 来源FactMemberBase 中统计的数据，都是取 id  作为membershopId
 * @date 2020/7/31 11:33
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MgFactMemberUnion extends FactMemberUnion {
    /**
     * 处理时间
     */
    @JsonProperty("analysis_date")
    String analysisDate;


    /**
     * 优惠券模板id
     */
    @JsonProperty("coupon_template_id")
    Integer couponTemplateId;


    /**
     * 核销次数
     */
    @JsonProperty("used_times")
    Integer usedTimes;

    /**
     * 失效次数
     */
    @JsonProperty("deprecated_times")
    Integer deprecatedTimes;

    /**
     * 过期次数
     */
    @JsonProperty("expired_times")
    Integer expiredTimes;


    /**
     * 领取次数
     */
    @JsonProperty("apply_times")
    Integer applyTimes;

    /**
     * 分享次数
     */
    @JsonProperty("share_times")
    Integer shareTimes;

    /**
     * * 优惠劵类型 0 满减劵    1 折扣劵      2 随机金额优惠劵       3 包邮劵
     */
    @JsonProperty("coupon_type")
    Integer couponType;

    /***************以上来源优惠券模板  FactMemberCoupon  FactMemberCouponLog  **************/


    /**
     * 商品id
     */
    @JsonProperty("product_id")
    Integer productId;

    /**
     * 商品浏览数
     */
    @JsonProperty("view_times")
    Integer viewedTimes;

    /**
     * 分享次数
     */
    @JsonProperty("share_product_times")
    Integer shareProductTimes;

    /**
     * 下单次数
     */
    @JsonProperty("order_times")
    Integer orderTimes;

    /**
     * 下单金额
     */
    @JsonProperty("order_amount")
    BigDecimal orderAmount;

    /**
     * 购买件数
     */
    @JsonProperty("order_quantity")
    Integer orderQuantity;

    /**
     * 付款订单数
     */
    @JsonProperty("paid_times")
    Integer paidTimes;

    /**
     * 付款金额
     */
    @JsonProperty("paid_amount")
    BigDecimal paidAmount;

    /**
     * 退款次数
     */
    @JsonProperty("refund_times")
    Integer refundTimes;


    /**
     * 退款金额
     */
    @JsonProperty("refund_amount")
    BigDecimal refundAmount;

    /**
     * 加入购物车时间
     */
    @JsonProperty("cart_add_date")
    Timestamp cartAddDate;

    /**
     * 加入购物车次数
     */
    @JsonProperty("cart_add_times")
    Integer cartAddTimes;


    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("visits_start")
    private Timestamp visitsStart;

    @JsonProperty("visits_end")
    private Timestamp visitsEnd;

    @JsonProperty("visits_duration")
    private Integer visitsDuration;

    @JsonProperty("uuid")
    private String uuid;


    //*********************群组/智能标签******相关信息 MemberGroup/TagDefinition *************************
    /**
     * 唯一标志
     * membertotal 也是这个。
     */
    private Integer id;

    /**
     * 门店id
     */
    @JsonProperty("shop_id")
    private Integer shopId;


    /**
     * 群组规则定义 JSONArray
     */
    @JsonProperty("filters")
    private String filters;

    /**
     * 群组版本
     */
    @JsonProperty("version")
    private Integer version;

    /**
     * 删除标志
     */
    @JsonProperty("__deleted")
    private boolean __deleted;
    //*********************群组/智能标签******相关信息 MemberGroup/TagDefinition *************************

    //*********************智能标签关联******相关信息 MemberTag *************************
    /**
     * 标签ID
     */
    @JsonProperty("tag_id")
    private Integer tagId;

    //*********************智能标签关联******相关信息 MemberTag *************************


    //*********************订单每日统计******相关信息 FactMemberOrder *************************


    /**
     * 下单次数
     */
    @JsonProperty("order_count")
    private int orderCount;


    /**
     * 付款订单数
     */
    @JsonProperty("paid_count")
    private int paidCount;


    /**
     * 订单id
     */
    @JsonProperty("order_ids")
    private String orderIds;

    /**
     * 退款订单id
     */
    @JsonProperty("refund_ids")
    private String refundIds;

    //*********************订单每日统计******相关信息 FactMemberOrder *************************

    /**
     * 退款数量
     */
    @JsonProperty("refund_quantity")
    private int refundQuantity;

    /**
     * 数据类型
     */
    @JsonProperty("data_type")
    DataType dataType;


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

    /**
     * 转为FactMemberProductLog
     *
     * @param mgFactMemberUnion
     * @return
     */
    public static FactMemberProductLog buildFactMemberProductLog(MgFactMemberUnion mgFactMemberUnion) {
        FactMemberProductLog factMemberProductLog = new FactMemberProductLog();
        factMemberProductLog.setAnalysisDate(mgFactMemberUnion.getAnalysisDate());
        factMemberProductLog.setShopId(mgFactMemberUnion.getShopId());
        factMemberProductLog.setMemberId(mgFactMemberUnion.getMemberId());
        factMemberProductLog.setProductId(mgFactMemberUnion.getProductId());
        factMemberProductLog.setViewedTimes(mgFactMemberUnion.getViewedTimes());
        factMemberProductLog.setShareTimes(mgFactMemberUnion.getShareTimes());
        factMemberProductLog.setCartAddDate(mgFactMemberUnion.getCartAddDate());
        factMemberProductLog.setCartAddTimes(mgFactMemberUnion.getCartAddTimes());
        factMemberProductLog.setGroupIds(mgFactMemberUnion.getGroupIds());
        factMemberProductLog.setGroups(mgFactMemberUnion.getGroups());
        factMemberProductLog.setDataType(mgFactMemberUnion.getDataType());
        return factMemberProductLog;
    }


    /**
     * 已经过滤不是智能标签。
     *
     * @param mgFactMemberUnion
     * @return
     */
    public static OdsTagDefinition buildOdsTagDefinition(MgFactMemberUnion mgFactMemberUnion) {
        OdsTagDefinition odsTagDefinition = new OdsTagDefinition();
        odsTagDefinition.setId(mgFactMemberUnion.getId());
        odsTagDefinition.setShopId(mgFactMemberUnion.getShopId());
        odsTagDefinition.setFilters(mgFactMemberUnion.getFilters());
        odsTagDefinition.setVersion(mgFactMemberUnion.getVersion());
        odsTagDefinition.set__deleted(mgFactMemberUnion.is__deleted());
        odsTagDefinition.setDataType(mgFactMemberUnion.getDataType());
        return odsTagDefinition;

    }


    /**
     * 已经过滤不是智能标签。
     *
     * @param mgFactMemberUnion
     * @return
     */
    public static OdsMemberGroup buildOdsMemberGroup(MgFactMemberUnion mgFactMemberUnion) {
        OdsMemberGroup memberGroup = new OdsMemberGroup();
        memberGroup.setId(mgFactMemberUnion.getId());
        memberGroup.setShopId(mgFactMemberUnion.getShopId());
        memberGroup.setFilters(mgFactMemberUnion.getFilters());
        memberGroup.setVersion(mgFactMemberUnion.getVersion());
        memberGroup.set__deleted(mgFactMemberUnion.is__deleted());
        memberGroup.setDataType(mgFactMemberUnion.getDataType());
        return memberGroup;
    }


    /**
     * 转为FactMemberProductOrder
     *
     * @param mgFactMemberUnion
     * @return
     */
    public static FactMemberProductOrder buildFactMemberProductOrder(MgFactMemberUnion mgFactMemberUnion) {
        FactMemberProductOrder factMemberProductOrder = new FactMemberProductOrder();
        factMemberProductOrder.setAnalysisDate(mgFactMemberUnion.getAnalysisDate());
        factMemberProductOrder.setShopId(mgFactMemberUnion.getShopId());
        factMemberProductOrder.setMemberId(mgFactMemberUnion.getMemberId());
        factMemberProductOrder.setProductId(mgFactMemberUnion.getProductId());
        factMemberProductOrder.setOrderTimes(mgFactMemberUnion.getOrderTimes());
        factMemberProductOrder.setOrderAmount(mgFactMemberUnion.getOrderAmount());
        factMemberProductOrder.setOrderQuantity(mgFactMemberUnion.getOrderQuantity());
        factMemberProductOrder.setPaidTimes(mgFactMemberUnion.getPaidTimes());
        factMemberProductOrder.setPaidAmount(mgFactMemberUnion.getPaidAmount());
        factMemberProductOrder.setGroupIds(mgFactMemberUnion.getGroupIds());
        factMemberProductOrder.setGroups(mgFactMemberUnion.getGroups());
        factMemberProductOrder.setDataType(mgFactMemberUnion.getDataType());
        return factMemberProductOrder;

    }

    /**
     * 转为FactMemberProductRefund
     *
     * @param mgFactMemberUnion
     * @return
     */
    public static FactMemberProductRefund buildFactMemberProductRefund(MgFactMemberUnion mgFactMemberUnion) {
        FactMemberProductRefund factMemberProductRefund = new FactMemberProductRefund();
        factMemberProductRefund.setAnalysisDate(mgFactMemberUnion.getAnalysisDate());
        factMemberProductRefund.setShopId(mgFactMemberUnion.getShopId());
        factMemberProductRefund.setMemberId(mgFactMemberUnion.getMemberId());
        factMemberProductRefund.setProductId(mgFactMemberUnion.getProductId());
        factMemberProductRefund.setRefundTimes(mgFactMemberUnion.getRefundTimes());
        factMemberProductRefund.setRefundQuantity(mgFactMemberUnion.getRefundQuantity());
        factMemberProductRefund.setRefundAmount(mgFactMemberUnion.getRefundAmount());
        factMemberProductRefund.setDataType(mgFactMemberUnion.getDataType());
        return factMemberProductRefund;
    }

    /**
     * 转为FactMemberCoupon
     *
     * @param mgFactMemberUnion
     * @return
     */
    public static FactMemberCoupon buildFactMemberCoupon(MgFactMemberUnion mgFactMemberUnion) {
        FactMemberCoupon factMemberCoupon = new FactMemberCoupon();
        factMemberCoupon.setAnalysisDate(mgFactMemberUnion.getAnalysisDate());
        factMemberCoupon.setShopId(mgFactMemberUnion.getShopId());
        factMemberCoupon.setMemberId(mgFactMemberUnion.getMemberId());
        factMemberCoupon.setCouponTemplateId(mgFactMemberUnion.getCouponTemplateId());
        factMemberCoupon.setUsedTimes(mgFactMemberUnion.getUsedTimes());
        factMemberCoupon.setApplyTimes(mgFactMemberUnion.getApplyTimes());
        factMemberCoupon.setExpiredTimes(mgFactMemberUnion.getExpiredTimes());
        factMemberCoupon.setDeprecatedTimes(mgFactMemberUnion.getDeprecatedTimes());
        factMemberCoupon.setCouponType(mgFactMemberUnion.getCouponType());
        factMemberCoupon.setDataType(mgFactMemberUnion.getDataType());
        return factMemberCoupon;

    }

    /**
     * 转为FactMemberCouponLog
     *
     * @param mgFactMemberUnion
     * @return
     */
    public static FactMemberCouponLog buildFactMemberCouponLog(MgFactMemberUnion mgFactMemberUnion) {
        FactMemberCouponLog factMemberCouponLog = new FactMemberCouponLog();
        factMemberCouponLog.setAnalysisDate(mgFactMemberUnion.getAnalysisDate());
        factMemberCouponLog.setShopId(mgFactMemberUnion.getShopId());
        factMemberCouponLog.setMemberId(mgFactMemberUnion.getMemberId());
        factMemberCouponLog.setCouponTemplateId(mgFactMemberUnion.getCouponTemplateId());
        factMemberCouponLog.setShareTimes(mgFactMemberUnion.getShareTimes());
        factMemberCouponLog.setCouponType(mgFactMemberUnion.getCouponType());
        factMemberCouponLog.setDataType(mgFactMemberUnion.getDataType());
        return factMemberCouponLog;
    }

    public static OdsMemberTag buildMemberTagFrom(MgFactMemberUnion mgFactMemberUnion) {
        OdsMemberTag odsMemberTag = new OdsMemberTag();
        odsMemberTag.setId(mgFactMemberUnion.getId());
        odsMemberTag.setMemberId(mgFactMemberUnion.getMemberId());
        odsMemberTag.setTagId(mgFactMemberUnion.getTagId());
        odsMemberTag.setShopId(mgFactMemberUnion.getShopId());
        odsMemberTag.set__deleted(mgFactMemberUnion.is__deleted());
        odsMemberTag.setDataType(mgFactMemberUnion.getDataType());
        return odsMemberTag;

    }

    /**
     * 转为buildFactMemberBase
     *
     * @param mgFactMemberUnion
     * @return
     */
    public static OdsMemberUnion buildFactMemberBase(MgFactMemberUnion mgFactMemberUnion) {
        OdsMemberUnion odsMemberUnion = new OdsMemberUnion();
        odsMemberUnion.setId(mgFactMemberUnion.getId());
        odsMemberUnion.setUserId(mgFactMemberUnion.getUserId());
        odsMemberUnion.setCountryCode(mgFactMemberUnion.getCountryCode());
        odsMemberUnion.setPhone(mgFactMemberUnion.getPhone());
        odsMemberUnion.setMemberId(mgFactMemberUnion.getMemberId());
        odsMemberUnion.setName(mgFactMemberUnion.getName());
        odsMemberUnion.setNickname(mgFactMemberUnion.getNickname());
        odsMemberUnion.setAvatar(mgFactMemberUnion.getAvatar());
        odsMemberUnion.setGender(mgFactMemberUnion.getGender());
        odsMemberUnion.setWechat(mgFactMemberUnion.getWechat());
        odsMemberUnion.setBirthday(mgFactMemberUnion.getBirthday());
        odsMemberUnion.setProvince(mgFactMemberUnion.getProvince());
        odsMemberUnion.setCity(mgFactMemberUnion.getCity());
        odsMemberUnion.setMainShopId(mgFactMemberUnion.getMainShopId());
        odsMemberUnion.setShopId(mgFactMemberUnion.getShopId());
        odsMemberUnion.setStaffId(mgFactMemberUnion.getStaffId());
        odsMemberUnion.setPaymentPassword(mgFactMemberUnion.getPaymentPassword());
        odsMemberUnion.setSource(mgFactMemberUnion.getSource());
        odsMemberUnion.setRemark(mgFactMemberUnion.getRemark());
        odsMemberUnion.setDisabled(mgFactMemberUnion.getDisabled());
        odsMemberUnion.setBalanceAmount(mgFactMemberUnion.getBalanceAmount());
        odsMemberUnion.setGiftAmount(mgFactMemberUnion.getGiftAmount());
        odsMemberUnion.setPoints(mgFactMemberUnion.getPoints());
        odsMemberUnion.setDebtAmount(mgFactMemberUnion.getDebtAmount());
        odsMemberUnion.setConsumeTimes(mgFactMemberUnion.getConsumeTimes());
        odsMemberUnion.setConsumeAmount(mgFactMemberUnion.getConsumeAmount());
        odsMemberUnion.setLastConsumeDate(mgFactMemberUnion.getLastConsumeDate());
        odsMemberUnion.setLastConsumeServices(mgFactMemberUnion.getLastConsumeServices());
        odsMemberUnion.setLastServiceStaff(mgFactMemberUnion.getLastServiceStaff());
        odsMemberUnion.setLastViewShopTime(mgFactMemberUnion.getLastViewShopTime());
        odsMemberUnion.setLastConsigneeAddress(mgFactMemberUnion.getLastConsigneeAddress());
        odsMemberUnion.setReferrer(mgFactMemberUnion.getReferrer());
        odsMemberUnion.setFollower(mgFactMemberUnion.getFollower());
        odsMemberUnion.setCustom1(mgFactMemberUnion.getCustom1());
        odsMemberUnion.setCustom2(mgFactMemberUnion.getCustom2());
        odsMemberUnion.setCustom3(mgFactMemberUnion.getCustom3());
        odsMemberUnion.setCustom4(mgFactMemberUnion.getCustom4());
        odsMemberUnion.setCustom5(mgFactMemberUnion.getCustom5());
        odsMemberUnion.setCustom6(mgFactMemberUnion.getCustom6());
        odsMemberUnion.setCreatedAt(mgFactMemberUnion.getCreatedAt());
        odsMemberUnion.setUpdatedAt(mgFactMemberUnion.getUpdatedAt());
        odsMemberUnion.setDataType(mgFactMemberUnion.getDataType());
        return odsMemberUnion;
    }


    public static FactMemberShopVisits buildFactMemberShopVisits(MgFactMemberUnion mgFactMemberUnion) {
        FactMemberShopVisits factMemberShopVisits = new FactMemberShopVisits();
        factMemberShopVisits.setShopId(mgFactMemberUnion.getShopId());
        factMemberShopVisits.setMemberId(mgFactMemberUnion.getMemberId());
        factMemberShopVisits.setDeviceId(mgFactMemberUnion.getDeviceId());
        factMemberShopVisits.setVisitsStart(mgFactMemberUnion.getVisitsStart());
        factMemberShopVisits.setVisitsEnd(mgFactMemberUnion.getVisitsEnd());
        factMemberShopVisits.setVisitsDuration(mgFactMemberUnion.getVisitsDuration());
        factMemberShopVisits.setUuid(mgFactMemberUnion.getUuid());
        return factMemberShopVisits;
    }


    public static FactMemberTotal buildFactMemberTotal(MgFactMemberUnion mgFactMemberUnion) {
        FactMemberTotal factMemberTotal = new FactMemberTotal();
        factMemberTotal.setShopId(mgFactMemberUnion.getShopId());
        factMemberTotal.setId(mgFactMemberUnion.getId());
        factMemberTotal.setOrderCount(mgFactMemberUnion.getOrderCount());
        factMemberTotal.setTotalOrderAmount(mgFactMemberUnion.getTotalOrderAmount());
        factMemberTotal.setRefundAmount(mgFactMemberUnion.getRefundAmount());
        factMemberTotal.setRefundCount(mgFactMemberUnion.getRefundCount());
        factMemberTotal.setLastOrderTime(mgFactMemberUnion.getLastOrderTime());
        factMemberTotal.setFirstOrderTime(mgFactMemberUnion.getFirstOrderTime());
        factMemberTotal.setAvgConsumeAmount(mgFactMemberUnion.getAvgConsumeAmount());
        factMemberTotal.setPurchaseRate(mgFactMemberUnion.getPurchaseRate());
        factMemberTotal.setDataType(mgFactMemberUnion.getDataType());
        return factMemberTotal;
    }


    public static OdsMemberUnion buildFactMemberBaseVistiTime(FactMemberShopVisits o) {
        OdsMemberUnion odsMemberUnion = new OdsMemberUnion();

        odsMemberUnion.setId(o.getMemberId());
        odsMemberUnion.setLastViewShopTime(o.getVisitsStart());
        if (o.getVisitsEnd() != null) {
            odsMemberUnion.setLastViewShopTime(o.getVisitsEnd());
        }
        return odsMemberUnion;
    }

    public static FactSubscribedQuery buildFactMemberBaseWxSubscribed(MgFactMemberUnion value) {
        FactSubscribedQuery factSubscribedQuery = new FactSubscribedQuery();
        factSubscribedQuery.setId(value.getId());
        factSubscribedQuery.setOpenid(value.getOpenid());
        factSubscribedQuery.setAppid(value.getAppid());
        factSubscribedQuery.setSubscribed(value.getSubscribed());
        factSubscribedQuery.setShopId(value.getShopId());
        return factSubscribedQuery;

    }

    public static FactMemberOrder buildFactMemberOrder(MgFactMemberUnion value) {
        FactMemberOrder factMemberOrder = new FactMemberOrder();
        factMemberOrder.setAnalysisDate(value.getAnalysisDate());
        factMemberOrder.setShopId(value.getShopId());
        factMemberOrder.setMemberId(value.getMemberId());
        factMemberOrder.setOrderCount(value.getOrderCount());
        factMemberOrder.setOrderAmount(value.getOrderAmount());
        factMemberOrder.setPaidCount(value.getPaidCount());
        factMemberOrder.setPaidAmount(value.getPaidAmount());
        factMemberOrder.setRefundCount(value.getRefundCount());
        factMemberOrder.setRefundAmount(value.getRefundAmount());
        factMemberOrder.setOrderIds(value.getOrderIds());
        factMemberOrder.setRefundIds(value.getRefundIds());
        factMemberOrder.setDataType(value.getDataType());
        return factMemberOrder;
    }
}
