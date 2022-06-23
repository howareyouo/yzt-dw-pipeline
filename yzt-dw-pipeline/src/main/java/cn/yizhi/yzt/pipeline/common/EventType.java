package cn.yizhi.yzt.pipeline.common;

import lombok.Getter;

/**
 * 埋点事件
 * @author aorui
 */
@Getter
public enum EventType {

    //浏览店铺心跳
    VIEW_SHOP_HEART_BEAT(1,"ViewShopHeartBeat"),

    //浏览店铺
    VIEW_SHOP(2,"ViewShop"),

    //分享店铺
    SHARE_SHOP(3,"ShareShop"),

    //搜索商品
    SEARCH_GOODS(4,"SearchGoods"),

    //查看商品
    VIEW_GOODS(5,"ViewGoods"),

    //分享商品
    SHARE_GOODS(6,"ShareGoods"),

    //加入购物车
    ADD_TO_SHOPPING_CART(7,"AddToShoppingCart"),

    //修改购物车商品数量
    CHANGE_SHOPPING_CART(8,"ChangeShoppingCart"),

    //领取优惠券
    RECEIVE_COUPON(9,"ReceiveCoupon"),

    //分享优惠券
    SHARE_COUPON(10,"ShareCoupon"),

    //浏览活动
    VIEW_ACTIVITY(11,"ViewActivity"),

    //活动主页直接加入购物车
    ADD_TO_SHOPPING_CART_BY_ACTIVITY(12,"AddToShoppingCartByActivity"),

    //分享活动
    SHARE_ACTIVITY(13,"ShareActivity"),

    //通过活动浏览商品详情
    VIEW_GOODS_BY_ACTIVITY(14,"ViewGoodsByActivity"),

    //分享活动商品
    SHARE_GOODS_BY_ACTIVITY(15,"ShareGoodsByActivity"),

    //进入直播间
    ENTER_LIVE_ROOM(16,"EnterLiveRoom"),

    //通过直播领取优惠劵
    RECEIVE_COUPON_BY_LIVE_ROOM(17,"ReceiveCouponByLiveRoom"),

    //在直播间浏览优惠劵
    VIEW_COUPON_BY_LIVE_ROOM(17,"ViewCouponByLiveRoom")
    ;

    EventType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    private Integer code;
    private String name;
}
