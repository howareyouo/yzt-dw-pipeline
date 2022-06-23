package cn.yizhi.yzt.pipeline.model.fact.member.filter;

public enum FilterCondition {


    // 会员基本属性

    GENDER(0, "性别, 未知:0 男:1 女: 2"),

    BIRTHDAY(0, "生日"),

    AGE(0, "年龄"),

    LOCATION(0, "所在地"),

    SUBSCRIBED(0, "微信是否绑定(关注)"),

    CREATED_AT(0, "建档时间"),

    SOURCE(0, "客户来源"),


    // 价值属性

    TAGS(1, "会员标签"),

    FIRST_PURCHASE(1, "首次消费"),

    LATEST_PURCHASE(1, "最近消费"),

    PURCHASE_RATE(1, "消费频率"),

    BALANCE(1, "储值余额"),

    POINTS(1, "积分余额"),

    HOLD_COUPONS(99, "持有效券"),


    // 行为属性

    AVG_AMOUNT(2, "平均消费金额"),

    TOTAL_AMOUNT(2, "消费总额"),

    PURCHASE_TIMES(2, "购买次数"),

    VISIT_TIMES(2, "访问次数"),

    VISIT_DURATION(2, "访问时长"),


    /**
     * 全部，表示只要浏览过任意一个商品, 用 `any` 表示
     */
    VIEW_PRODUCTS(2, "浏览商品"),


    /**
     * 全部，表示只要将任意一个商品加入过购物车, 用 `any` 表示
     */
    CART_PRODUCTS(2, "加购商品"),


    /**
     * 全部，表示只要购买任意一个商品, 用 `any` 表示
     */
    BUYS_PRODUCTS(2, "购买商品"),


    SHARE_PRODUCTS(2, "分享商品"),


    UNPAID_PRODUCTS(2, "下单未付款商品"),


    GAIN_COUPONS(2, "领取优惠券"),


    USED_COUPONS(2, "核销/使用优惠券"),

    SHARE_COUPONS(2, "分享优惠券"),



    TIMELESS_TOTAL_AMOUNT(2, "全量的消费金额");


    /**
     * 条件类型, 0-基本属性, 1-价格属性, 2-行为属性
     */
    public int order;

    /**
     * 条件描述
     */
    public String description;

    FilterCondition(int order, String description) {
        this.order = order;
        this.description = description;
    }

}