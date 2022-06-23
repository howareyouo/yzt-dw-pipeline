package cn.yizhi.yzt.pipeline.common;

import lombok.Getter;

/**
 * @Author: HuCheng
 * @Date: 2021/1/8 15:35
 * <p>
 * 供FilterMemberGroupStreamJob 聚合流使用，定义数据来源类型
 */
@Getter
public enum DataType {
    //商品日志
    PRODUCT_LOG,
    //商品订单
    PRODUCT_ORDER,
    //商品退款
    PRODUCT_REFUND,
    //优惠劵日志
    COUPON_LOG,
    //优惠劵
    COUPON_RECEIVED,
    //店铺浏览
    SHOP_VIEW,
    //会员每日订单数据
    MEMBER_ORDER,
    //会员累计订单数据
    MEMBER_TOTAL,
    //店铺群租定义
    SHOP_MEMBER_GROUP,
    //店铺智能标签定义
    SHOP_MEMBER_TAG_DEFINITION,
    //会员标签关联数据
    MEMBER_TAG,
    //用户基础信息
    MEMBER_UNION,
    //用户微信
    MEMBER_WX_SUBSCRIBED,
}
