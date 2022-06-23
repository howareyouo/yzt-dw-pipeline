package cn.yizhi.yzt.pipeline.config;

/**
 * @author hucheng
 * @date 2020/6/12 11:03
 */
public class Tables {
    //source table
    public static final String SOURCE_TABLE_FLASH_ACTIVITY_PRODUCT = "ods_flashsale_activity_product";
    public static final String SOURCE_TABLE_FLASH_ACTIVITY = "ods_flashsale_activity";

    public static final String SOURCE_TABLE_ORDER_ITEM = "ods_order_item";
    public static final String SOURCE_TABLE_ORDER_PROMOTION = "ods_order_promotion";
    public static final String SOURCE_TABLE_ODS_ORDER_ITEM = "ods_order_item";

    //sink table
    public static final String SINK_TABLE_FLASH_ACTIVITY_PRODUCT = "fact_flash_sale_product_v1";
    public static final String SINK_TABLE_FLASH_ACTIVITY = "fact_flash_sale_v1";
    public static final String SINK_TABLE_FLASH_ACTIVITY_DAY = "fact_flash_sale_day_v1";


    public static final String SINK_TABLE_SHOP = "fact_shop_v1";
    public static final String SINK_TABLE_SHOP_MONTH = "fact_shop_month_v1";
    public static final String SINK_TABLE_SHOP_DAY = "fact_shop_day_v1";
    public static final String SINK_TABLE_SHOP_HOUR = "fact_shop_hour_v1";

    public static final String SINK_TABLE_COUPON = "fact_coupon_v1";
    public static final String SINK_TABLE_FULL_REDUCE = "fact_full_reduce_v1";
    public static final String SINK_TABLE_FULL_REDUCE_ALL = "fact_full_reduce_all_v1";
    public static final String SINK_TABLE_PRODUCT_FULL_REDUCE = "fact_product_full_reduce_v1";
    public static final String SINK_TABLE_PRODUCT = "fact_product_v1";
    public static final String SINK_TABLE_USER_FULL_REDUCE = "fact_user_full_reduce_v1";

    //会员
    public static final String SINK_TABLE_MEMBER_SHOP_VISITS = "mg_fact_member_shop_visits_v1";

    //群组
    public static final String SINK_TABLE_MEMBER_GROUP_POPULATION = "mg_fact_member_group_population";

    public static final String SINK_TABLE_PROMOTION_ALL = "fact_promotion_all_v1";

    //活动日志
    public static final String SINK_TABLE_PROMOTION_LOG = "fact_promotion_log_v1";

    //拼团一起
    public static final String SINK_TABLE_GROUP_BUY_V1 = "fact_group_buy_v1";
    public static final String SINK_TABLE_ORDER_GROUP_BUY_V1 = "fact_order_group_buy_v1";

    //拼团二期
    public static final String SINK_TABLE_GROUP_BUY_ORDER_V1 = "fact_group_buy_order_v1";
    public static final String SINK_TABLE_GROUP_BUY_STAFF_V1 = "fact_group_buy_staff_v1";

    //会员商品
    public static final String SINK_TABLE_MEMBER_PRODUCT = "mg_fact_member_product_daily_v1";

    //会员优惠券
    public static final String SINK_TABLE_MEMBER_COUPON = "mg_fact_member_coupon_daily_v1";

    //会员订单
    public static final String SINK_TABLE_MEMBER_ORDER = "mg_fact_member_order_daily_v1";

    //会员基础UNION
    public static final String SINK_TABLE_MEMBER_FACT_UNION = "mg_fact_member_union_v1";

}
