package cn.yizhi.yzt.pipeline.config;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 从Kafka Connect/Debezium采集过来的mysql业务表对应的topics, 去掉了debezium的"database.server.name"前缀
 */
public class SourceTopics {
    private static final String PREFIX = "yzt-mysql.";
    //门店相关表
    public static final String TOPIC_SHOP = PREFIX + "yzt_account_db.shop";
    public static final String TOPIC_SHOP_GROUP = PREFIX + "yzt_account_db.shop_group";

    // 会员相关的表
    public static final String TOPIC_CONSIGNEE_ADDR = PREFIX + "yzt_member_db.consignee_addr";
    public static final String TOPIC_MEMBER_BASE = PREFIX + "yzt_member_db.member_base";
    public static final String TOPIC_MEMBER_EXTEND = PREFIX + "yzt_member_db.member_extend";
    public static final String TOPIC_MEMBER_UNION = PREFIX + "yzt_member_db.member_union";
    public static final String TOPIC_SHOP_MEMBER = PREFIX + "yzt_member_db.shop_member";
    public static final String TOPIC_MEMBER_SOCIAL_ACCOUNT = PREFIX + "yzt_member_db.social_account";
    public static final String TOPIC_USER = PREFIX + "yzt_member_db.user";

    //发送业务标签消息 给到 member服务使用
    public static final String TOPIC_TAG_UPDATE_TO_MEMBER = "flink_tag_update_to_member";

    //发送业务群组消息 给到 bigdate服务使用
    public static final String TOPIC_MEMBER_GROUP_UPDATE_TO_BIGDATA = "flink_member_group_update_to_bigdata";


    //标签分组
    public static final String TOPIC_MEMBER_GROUP = PREFIX + "yzt_member_db.member_group";
    //标签定义
    public static final String TOPIC_TAG_DEFINITION = PREFIX + "yzt_member_db.tag_definition";
    //标签关联
    public static final String TOPIC_MEMBER_TAG = PREFIX + "yzt_member_db.member_tag";

    // 订单相关表;
    public static final String TOPIC_EMALL_ORDER = PREFIX + "yzt_order_db.emall_order";
    public static final String TOPIC_ORDER_ITEM = PREFIX + "yzt_order_db.order_item";
    public static final String TOPIC_ORDER_PROMOTION = PREFIX + "yzt_order_db.order_promotion";
    public static final String TOPIC_ORDER_REFUND = PREFIX + "yzt_order_db.refund";
    public static final String TOPIC_REFUND_ITEM = PREFIX + "yzt_order_db.refund_item";
    public static final String TOPIC_DELIVERY_PACKAGE = PREFIX + "yzt_order_db.delivery_package";
    public static final String TOPIC_ORDER_CONSIGNEE = PREFIX + "yzt_order_db.order_consignee";
    //直播间订单商品关联表
    public static final String TOPIC_LIVE_ROOM_ORDER_ITEM = PREFIX + "yzt_wxlive_db.live_order_item";

    //自定义页面相关表
    public static final String TOPIC_PAGE_CATEGORY_TEMPLATE = PREFIX + "yzt_product_db.category_template";
    public static final String TOPIC_PAGE_CATEGORY = PREFIX + "yzt_product_db.page_category";
    public static final String TOPIC_PAGE_TEMPLATE = PREFIX + "yzt_product_db.page_template";

    // 商品相关表
    public static final String TOPIC_PRODUCT = PREFIX + "yzt_product_db.product";
    public static final String TOPIC_PRODUCT_GROUP = PREFIX + "yzt_product_db.product_group";
    public static final String TOPIC_PRODUCT_RE_GROUP = PREFIX + "yzt_product_db.product_re_group";
    public static final String TOPIC_PRODUCT_RE_SPEC = PREFIX + "yzt_product_db.product_re_spec";
    public static final String TOPIC_PRODUCT_SETTING = PREFIX + "yzt_product_db.product_setting";
    public static final String TOPIC_PRODUCT_SKU = PREFIX + "yzt_product_db.product_sku";
    public static final String TOPIC_PRODUCT_SKU_EXTEND = PREFIX + "yzt_product_db.product_sku_extend";
    public static final String TOPIC_PRODUCT_SPEC = PREFIX + "yzt_product_db.product_spec";
    public static final String TOPIC_PRODUCT_SPEC_VALUE = PREFIX + "yzt_product_db.product_spec_value";
    public static final String TOPIC_PRODUCT_SKU__SPEC_RE = PREFIX + "yzt_product_db.product_re_spec";

    //优惠券相关表
    public static final String TOPIC_COUPON = PREFIX + "yzt_coupon_db.coupon";
    public static final String TOPIC_COUPON_TEMPLATE = PREFIX + "yzt_coupon_db.coupon_template";
    public static final String TOPIC_COUPON_USE_RECORD = PREFIX + "yzt_coupon_db.coupon_use_record";
    public static final String TOPIC_COUPON_RECEIVE = "coupon_streams";

    //满减相关表
    public static final String TOPIC_FULL_REDUCE_PROMOTION = PREFIX + "yzt_coupon_db.full_reduce_promotion";
    public static final String TOPIC_FULL_REDUCE_PROMOTION_ORDER = PREFIX + "yzt_coupon_db.full_reduce_promotion_order";
    public static final String TOPIC_FULL_REDUCE_PROMOTION_ORDER_DETAIL = PREFIX + "yzt_coupon_db.full_reduce_promotion_order_detail";
    public static final String TOPIC_FULL_REDUCE_PROMOTION_ORDER_ITEM = PREFIX + "yzt_coupon_db.full_reduce_promotion_order_item";

    //满减商品事实
    public static final String TOPIC_PRODUCT_FULL_REDUCE = "fact-product-full-reduce";

    // 限时抢购相关表
    public static final String TOPIC_FLASHSALE_ACTIVITY = PREFIX + "yzt_flashsale_db.flashsale_activity";
    public static final String TOPIC_FLASHSALE_ACTIVITY_PRODUCT = PREFIX + "yzt_flashsale_db.flashsale_activity_product";
    public static final String TOPIC_FLASHSALE_PAGE_TEMPLATE = PREFIX + "yzt_flashsale_db.flashsale_page_template";
    public static final String TOPIC_FLASHSALE_APPOINTMENT = PREFIX + "yzt_flashsale_db.flashsale_appointment";

    //访问日志
    public static final String TOPIC_SDK_LOG_STREAM = "sdk_log_streams";

    public static final String TOPIC_ODS_LOG_STREAM = "ods_log_stream";

    //日志事实
    public static final String TOPIC_FACT_ORDER_DETAIL = "fact_order_detail";
    public static final String TOPIC_FACT_LOG_STREAM = "fact_log_stream";


    //订单明细
    public static final String TOPIC_FACT_ORDER_PROMOTION_ITEM = "pre-fact-order-promotion-item";
    public static final String TOPIC_FACT_ORDER_ITEM = "pre-fact-order-item";
    public static final String TOPIC_FACT_ORDER = "pre-fact-order";

    //公共指标topic
    public static final String TOPIC_FACT_PRODUCT_LOG = "pre-fact-product-log";
    public static final String TOPIC_FACT_PROMOTION_LOG = "pre-fact-promotion-log";
    public static final String TOPIC_FACT_MEMBER_LOG = "pre-fact-member-log";

    //累计不去重
    public static final String TOPIC_FACT_PROMOTION_PRODUCT_LOG_ALL = "pre-fact-promotion-product-log-all";
    public static final String TOPIC_FACT_PROMOTION_LOG_ALL = "pre-fact-promotion-log-all";

    //公共指标秒杀
    public static final String TOPIC_FACT_FLASH_SALE_PRODUCT_INVENTORY = "pre-fact_flash_sale_product_inventory";

    //拼团
    public static final String TOPIC_GROUP_BUY_PROMOTION_INSTANCE = PREFIX + "yzt_groupbuy_db.group_promotion_instance";
    public static final String TOPIC_GROUP_BUY_PROMOTION_PV_UV_MESSAGE = "group_buy_promotion_pv_uv_message";
    public static final String TOPIC_GROUP_BUY_PROMOTION_INSTANCE_MEMBER = PREFIX + "yzt_groupbuy_db.group_promotion_instance_member";
    public static final String TOPIC_GROUP_BUY_PROMOTION_APPOINTMENT = PREFIX + "yzt_groupbuy_db.group_promotion_appointment";

    //会员指标
    public static final String TOPIC_FACT_MEMBER_BASE = "fact_member_base";
    public static final String TOPIC_MEMBER_SACIAL_ACCOUNT = PREFIX + "yzt_member_db.social_account";
    public static final String TOPIC_MEMBER_WXAUTH_RELATION = PREFIX + "yzt_wxauth_db.wx_relation";
    public static final String TOPIC_MEMBER_WXMP_USER = PREFIX + "yzt_wxmp_db.wx_user";

    //会员聚合
    public static final String TOPIC_MG_FACT_MEMBER_UNION_GROUP = "mg_fact_member_union_group";

    public static final String TOPIC_ODS_PRODUCT_RE_GROUP = PREFIX + "yzt_product_db.product_re_group";

    //包装流
    public static final String TOPIC_FACT_PACK_ODS_LOG = "fact_pack_ods_log";
    public static final String TOPIC_FACT_PACK_ORDER_ITEM = "fact_pack_order_item";

    private ServerConfig serverConfig;
    private StreamExecutionEnvironment env;

    public SourceTopics(StreamExecutionEnvironment env, ServerConfig serverConfig) {
        this.env = env;
        this.serverConfig = serverConfig;
    }

}
