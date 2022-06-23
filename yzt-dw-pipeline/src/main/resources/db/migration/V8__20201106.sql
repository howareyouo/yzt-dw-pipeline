-- flash_sale
create TABLE `fact_flash_sale_v1`
(
    `activity_id`              int(11) NOT NULL DEFAULT 0 COMMENT '活动id',
    `pv`                       int(11)          DEFAULT 0,
    `uv`                       int(11)          DEFAULT 0,
    `order_count`              int(11)          DEFAULT 0 COMMENT '下单次数',
    `pay_coupon_total`         decimal(20, 2)   DEFAULT '0.00' COMMENT '优惠总额',
    `order_number`             int(11)          DEFAULT 0 COMMENT '下单人数',
    `pay_total`                decimal(20, 2)   DEFAULT '0.00' COMMENT '付款总额',
    `pay_count`                int(11)          DEFAULT 0 COMMENT '付款单数',
    `pay_number`               int(11)          DEFAULT 0 COMMENT '付款人数',
    `sale_count`               int(11)          DEFAULT 0 COMMENT '商品销量',
    `inventory`                int(11)          DEFAULT 0 COMMENT '剩余库存',
    `share_user_count`         int(11)          DEFAULT 0 COMMENT '分享人数',
    `share_count`              int(11)          DEFAULT 0 COMMENT '分享次数',
    `appointment_count`        int(11)          DEFAULT 0 COMMENT '预约人数',
    `cancel_appointment_count` int(11)          DEFAULT 0 COMMENT '取消预约人数',
    PRIMARY KEY (`activity_id`),
    KEY `f_f_s_a` (`activity_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- flash_sale_product
create TABLE `fact_flash_sale_product_v1`
(
    `activity_id`              int(11) NOT NULL DEFAULT 0 COMMENT '活动id',
    `product_id`               int(11) NOT NULL DEFAULT 0 COMMENT '商品id',
    `pv`                       int(11)          DEFAULT 0,
    `uv`                       int(11)          DEFAULT 0,
    `order_count`              int(11)          DEFAULT 0 COMMENT '下单次数',
    `order_number`             int(11)          DEFAULT 0 COMMENT '下单人数',
    `pay_total`                decimal(20, 2)   DEFAULT '0.00' COMMENT '付款总额',
    `pay_coupon_total`         decimal(20, 2)   DEFAULT '0.00' COMMENT '优惠总金额',
    `pay_count`                int(11)          DEFAULT 0 COMMENT '付款单数',
    `pay_number`               int(11)          DEFAULT 0 COMMENT '付款人数',
    `sale_count`               int(11)          DEFAULT 0 COMMENT '商品销量',
    `inventory`                int(11)          DEFAULT 0 COMMENT '剩余库存',
    `share_count`              int(11)          DEFAULT 0 COMMENT '分享次数',
    `share_user_count`         int(11)          DEFAULT 0 COMMENT '分享人数',
    `appointment_count`        int(11)          DEFAULT 0 COMMENT '预约人数',
    `cancel_appointment_count` int(11)          DEFAULT 0 COMMENT '取消预约人数',
    PRIMARY KEY (`activity_id`, `product_id`),
    KEY `f_f_s_a` (`activity_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ----------------------------
--  Table structure for `fact_shop`
-- ----------------------------
create TABLE `fact_shop_v1`
(
    `row_time`             varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    `channel`              varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    `shop_id`              int(11)                                NOT NULL,
    `member_count`         int(11)        DEFAULT 0 COMMENT '累计客户数',
    `order_amount`         decimal(20, 2) DEFAULT '0.00' COMMENT '累计订单额',
    `order_count`          int(11)        DEFAULT 0 COMMENT '累计订单数',
    `order_user_count`     int(11)        DEFAULT 0 COMMENT '已消费客户数',
    `refund_count`         int(11)        DEFAULT 0 COMMENT '退款单数',
    `refund_amount`        decimal(20, 2) DEFAULT '0.00' COMMENT '累计退款额',
    `not_order_user_count` int(11)        DEFAULT 0 COMMENT '未消费客户数',
    PRIMARY KEY (`row_time`, `channel`, `shop_id`),
    KEY `shop_row_date_index` (`row_time`) USING BTREE,
    KEY `shop_shop_id_index` (`shop_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;



-- ----------------------------
--  Table structure for `fact_shop_day`
-- ----------------------------
create TABLE `fact_shop_day_v1`
(
    `row_time`                  varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日期',
    `channel`                   varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道',
    `shop_id`                   int(11)                                NOT NULL COMMENT '店铺ID',
    `pv`                        int(11)        DEFAULT 0 COMMENT '浏览量',
    `uv`                        int(11)        DEFAULT 0 COMMENT '访客',
    `pv_product`                int(11)        DEFAULT 0 COMMENT '店铺下商品浏览次数',
    `uv_product`                int(11)        DEFAULT 0 COMMENT '店铺下商品浏览人数',
    `add_cart_count`            int(11)        DEFAULT 0 COMMENT '加购次数',
    `add_cart_user_count`       int(11)        DEFAULT 0 COMMENT '加购人数',
    `share_count`               int(11)        DEFAULT 0 COMMENT '分享次数',
    `share_user_count`          int(11)        DEFAULT 0 COMMENT '分享人数',
    `order_count`               int(11)        DEFAULT 0 COMMENT '订单数',
    `order_user_count`          int(11)        DEFAULT 0 COMMENT '下单人数',
    `order_amount`              decimal(20, 2) DEFAULT '0.00' COMMENT '下单金额',
    `pay_count`                 int(11)        DEFAULT 0 COMMENT '付款订单数',
    `pay_user_count`            int(11)        DEFAULT 0 COMMENT '付款人数',
    `pay_amount`                decimal(20, 2) DEFAULT '0.00' COMMENT '支付总金额',
    `refund_count`              int(11)        DEFAULT 0 COMMENT '退款订单数',
    `refund_user_count`         int(11)        DEFAULT 0 COMMENT '退款人数',
    `refund_amount`             decimal(20, 2) DEFAULT '0.00' COMMENT '退款金额',
    `sale_count`                int(11)        DEFAULT 0 COMMENT '商品销量',
    `promotion_order_amount`    decimal(20, 2) DEFAULT '0.00' COMMENT '活动订单金额',
    `promotion_discount_amount` decimal(20, 2) DEFAULT '0.00' COMMENT '活动订单优惠金额',
    `register_count`            int(11)        DEFAULT 0 COMMENT '注册用户数',
    `promotion_count`           int(11)        DEFAULT 0 COMMENT '当日产生订单活动数',
    PRIMARY KEY (`row_time`, `channel`, `shop_id`),
    KEY `s_d_shop_id_index` (`shop_id`) USING BTREE,
    KEY `fsd_row_date_index` (`row_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ----------------------------
--  Table structure for `fact_shop_hour`
-- ----------------------------
create TABLE `fact_shop_hour_v1`
(
    `row_time`                  varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日期',
    `channel`                   varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道',
    `shop_id`                   int(11)                                NOT NULL COMMENT '店铺ID',
    `pv`                        int(11)        DEFAULT 0 COMMENT '浏览量',
    `uv`                        int(11)        DEFAULT 0 COMMENT '访客',
    `pv_product`                int(11)        DEFAULT 0 COMMENT '店铺下商品浏览次数',
    `uv_product`                int(11)        DEFAULT 0 COMMENT '店铺下商品浏览人数',
    `add_cart_count`            int(11)        DEFAULT 0 COMMENT '加购次数',
    `add_cart_user_count`       int(11)        DEFAULT 0 COMMENT '加购人数',
    `share_count`               int(11)        DEFAULT 0 COMMENT '分享次数',
    `share_user_count`          int(11)        DEFAULT 0 COMMENT '分享人数',
    `order_count`               int(11)        DEFAULT 0 COMMENT '订单数',
    `order_user_count`          int(11)        DEFAULT 0 COMMENT '下单人数',
    `order_amount`              decimal(20, 2) DEFAULT '0.00' COMMENT '下单金额',
    `pay_count`                 int(11)        DEFAULT 0 COMMENT '付款订单数',
    `pay_user_count`            int(11)        DEFAULT 0 COMMENT '付款人数',
    `pay_amount`                decimal(20, 2) DEFAULT '0.00' COMMENT '支付总金额',
    `refund_count`              int(11)        DEFAULT 0 COMMENT '退款订单数',
    `refund_user_count`         int(11)        DEFAULT 0 COMMENT '退款人数',
    `refund_amount`             decimal(20, 2) DEFAULT '0.00' COMMENT '退款金额',
    `sale_count`                int(11)        DEFAULT 0 COMMENT '商品销量',
    `promotion_order_amount`    decimal(20, 2) DEFAULT '0.00' COMMENT '活动订单金额',
    `promotion_discount_amount` decimal(20, 2) DEFAULT '0.00' COMMENT '活动订单优惠金额',
    `register_count`            int(11)        DEFAULT 0 COMMENT '注册用户数',
    `promotion_count`           int(11)        DEFAULT 0 COMMENT '当日产生订单活动数',
    PRIMARY KEY (`row_time`, `channel`, `shop_id`),
    KEY `s_d_shop_id_index` (`shop_id`) USING BTREE,
    KEY `fsd_row_date_index` (`row_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ----------------------------
--  Table structure for `fact_shop_month`
-- ----------------------------
create TABLE `fact_shop_month_v1`
(
    `row_time`                  varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日期',
    `channel`                   varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道',
    `shop_id`                   int(11)                                NOT NULL COMMENT '店铺ID',
    `pv`                        int(11)        DEFAULT 0 COMMENT '浏览量',
    `uv`                        int(11)        DEFAULT 0 COMMENT '访客',
    `pv_product`                int(11)        DEFAULT 0 COMMENT '店铺下商品浏览次数',
    `uv_product`                int(11)        DEFAULT 0 COMMENT '店铺下商品浏览人数',
    `add_cart_count`            int(11)        DEFAULT 0 COMMENT '加购次数',
    `add_cart_user_count`       int(11)        DEFAULT 0 COMMENT '加购人数',
    `share_count`               int(11)        DEFAULT 0 COMMENT '分享次数',
    `share_user_count`          int(11)        DEFAULT 0 COMMENT '分享人数',
    `order_count`               int(11)        DEFAULT 0 COMMENT '订单数',
    `order_user_count`          int(11)        DEFAULT 0 COMMENT '下单人数',
    `order_amount`              decimal(20, 2) DEFAULT '0.00' COMMENT '下单金额',
    `pay_count`                 int(11)        DEFAULT 0 COMMENT '付款订单数',
    `pay_user_count`            int(11)        DEFAULT 0 COMMENT '付款人数',
    `pay_amount`                decimal(20, 2) DEFAULT '0.00' COMMENT '支付总金额',
    `refund_count`              int(11)        DEFAULT 0 COMMENT '退款订单数',
    `refund_user_count`         int(11)        DEFAULT 0 COMMENT '退款人数',
    `refund_amount`             decimal(20, 2) DEFAULT '0.00' COMMENT '退款金额',
    `sale_count`                int(11)        DEFAULT 0 COMMENT '商品销量',
    `promotion_order_amount`    decimal(20, 2) DEFAULT '0.00' COMMENT '活动订单金额',
    `promotion_discount_amount` decimal(20, 2) DEFAULT '0.00' COMMENT '活动订单优惠金额',
    `register_count`            int(11)        DEFAULT 0 COMMENT '注册用户数',
    `promotion_count`           int(11)        DEFAULT 0 COMMENT '当日产生订单活动数',
    PRIMARY KEY (`row_time`, `channel`, `shop_id`),
    KEY `s_d_shop_id_index` (`shop_id`) USING BTREE,
    KEY `fsd_row_date_index` (`row_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 优惠券
create TABLE `fact_coupon_v1`
(
    `row_date`            varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
    `shop_id`             int(11)                                NOT NULL DEFAULT '0' COMMENT '店铺id',
    `coupon_id`           int(11)                                NOT NULL DEFAULT '0' COMMENT '优惠劵id',
    `channel`             varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '渠道',
    `pay_amount`          decimal(20, 2)                                  DEFAULT '0.00' COMMENT '当日支付总金额',
    `preferential_amount` decimal(20, 2)                                  DEFAULT '0.00' COMMENT '优惠总金额',
    `write_off_number`    int(11)                                         DEFAULT 0 COMMENT '核销人数',
    `write_off_count`     int(11)                                         DEFAULT 0 COMMENT '核销张数',
    `sale_count`          int(11)                                         DEFAULT 0 COMMENT '商品销量',
    `order_count`         int(11)                                         DEFAULT 0 COMMENT '当日总订单数',
    `order_number`        int(11)                                         DEFAULT 0 COMMENT '当日下单人数',
    `new_member_count`    int(11)                                         DEFAULT 0 COMMENT '用劵新客',
    `old_member_count`    int(11)                                         DEFAULT 0 COMMENT '用劵老客',
    PRIMARY KEY (`row_date`, `shop_id`, `coupon_id`, `channel`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 满减
create TABLE `fact_full_reduce_v1`
(
    `shop_id`             int(11)                                NOT NULL DEFAULT '0' COMMENT '店铺ID',
    `row_date`            varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
    `promotion_id`        int(11)                                NOT NULL DEFAULT '0' COMMENT '满减活动id',
    `pv`                  int(11)                                         DEFAULT 0,
    `uv`                  int(11)                                         DEFAULT 0,
    `product_pv`          int(11)                                         DEFAULT 0 COMMENT '主页商品访问量',
    `order_count`         int(11)                                         DEFAULT 0 COMMENT '当日总订单数',
    `pay_total`           decimal(20, 2)                                  DEFAULT '0.00' COMMENT '付款总额',
    `pay_number`          int(11)                                         DEFAULT 0 COMMENT '当日付款人数',
    `sale_count`          int(11)                                         DEFAULT 0 COMMENT '商品销量',
    `product_amount`      decimal(20, 2)                                  DEFAULT '0.00' COMMENT '活动商品销售额',
    `preferential_amount` decimal(20, 2)                                  DEFAULT '0.00' COMMENT '优惠额',
    `addcart_count`       int(11)                                         DEFAULT 0 COMMENT '主页加购次数',
    `share_count`         int(11)                                         DEFAULT 0 COMMENT '主页分享次数',
    PRIMARY KEY (`shop_id`, `row_date`, `promotion_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 商品满减
create TABLE `fact_product_full_reduce_v1`
(
    `row_date`        varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    `shop_id`         int(11)                                NOT NULL DEFAULT '0' COMMENT '店铺ID',
    `product_id`      int(11)                                NOT NULL DEFAULT '0' COMMENT '商品id',
    `full_reduce_id`  int(11)                                NOT NULL DEFAULT '0' COMMENT '满减id',
    `pv`              int(11)                                         DEFAULT 0 COMMENT '通过满减活动的pv',
    `uv`              int(11)                                         DEFAULT 0 COMMENT '通过满减活动的uv',
    `sale_total`      decimal(20, 2)                                  DEFAULT '0.00' COMMENT '销售额_通过满减活动',
    `sale_count`      int(11)                                         DEFAULT 0 COMMENT '销量_通过满减活动',
    `pay_count`       int(11)                                         DEFAULT 0 COMMENT '当日付款单数_通过满减活动',
    `pay_number`      int(11)                                         DEFAULT 0 COMMENT '当日付款人数_通过满减活动',
    `discount_amount` decimal(20, 2)                                  DEFAULT '0.00' COMMENT '活动优惠总金额',
    `share_count`     int(11)                                         DEFAULT 0 COMMENT '当日分享次数',
    PRIMARY KEY (`row_date`, `shop_id`, `product_id`, `full_reduce_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 用户满减
create TABLE `fact_user_full_reduce_v1`
(
    `row_date`                varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
    `shop_id`                 int(11)                                NOT NULL DEFAULT '0' COMMENT '店铺ID',
    `member_id`               int(11)                                NOT NULL DEFAULT '0' COMMENT '商品id',
    `full_reduce_id`          int(11)                                NOT NULL DEFAULT '0' COMMENT '满减id',
    `order_count`             int(11)                                         DEFAULT 0 COMMENT '通过满减活动的订单数',
    `sale_count`              int(11)                                         DEFAULT 0 COMMENT '通过满减活动商品购买数',
    `pay_amount`              decimal(20, 2)                                  DEFAULT '0.00' COMMENT '支付总金额',
    `discount_amount`         decimal(20, 2)                                  DEFAULT '0.00' COMMENT '优惠总金额',
    `share_count_full_reduce` int(11)                                         DEFAULT 0 COMMENT '活动分享次数',
    `share_count_product`     int(11)                                         DEFAULT 0 COMMENT '商品分享次数',
    `last_join_time`          timestamp(3)                           NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON update CURRENT_TIMESTAMP(3) COMMENT '最后参与时间',
    PRIMARY KEY (`row_date`, `shop_id`, `member_id`, `full_reduce_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 商品
create TABLE `fact_product_v1`
(
    `row_date`       varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
    `product_id`     int(11)                                NOT NULL DEFAULT '0' COMMENT '商品id',
    `shop_id`        int(11)                                NOT NULL DEFAULT '0' COMMENT '店铺id',
    `channel`        varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '渠道',
    `pv`             int(11)                                         DEFAULT 0,
    `uv`             int(11)                                         DEFAULT 0,
    `sale_total`     decimal(20, 2)                                  DEFAULT '0.00' COMMENT '销售额',
    `sale_count`     int(11)                                         DEFAULT 0 COMMENT '销量',
    `order_count`    int(11)                                         DEFAULT 0 COMMENT '当日总订单数',
    `order_number`   int(11)                                         DEFAULT 0 COMMENT '当日下单人数',
    `pay_count`      int(11)                                         DEFAULT 0 COMMENT '当日付款单数',
    `pay_number`     int(11)                                         DEFAULT 0 COMMENT '当日付款人数',
    `inventory`      int(11)                                         DEFAULT 0 COMMENT '剩余库存',
    `share_count`    int(11)                                         DEFAULT 0 COMMENT '分享次数',
    `share_number`   int(11)                                         DEFAULT 0 COMMENT '分享人数',
    `addcart_count`  int(11)                                         DEFAULT 0 COMMENT '加购次数',
    `addcart_number` int(11)                                         DEFAULT 0 COMMENT '加购人数',
    PRIMARY KEY (`row_date`, `product_id`, `shop_id`, `channel`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 秒杀每天数据指标
create TABLE `fact_flash_sale_day_v1`
(
    `row_date`        varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
    `shop_id`         int(11)                                NOT NULL DEFAULT '0' COMMENT '店铺ID',
    `promotion_type`  int(11)                                         DEFAULT 0 COMMENT '活动类型',
    `order_count`     int(11)                                         DEFAULT 0 COMMENT '每天活动订单数',
    `order_amount`    decimal(20, 2)                                  DEFAULT '0.00' COMMENT '每天活动订单额',
    `discount_amount` decimal(20, 2)                                  DEFAULT '0.00' COMMENT '每天优惠金额',
    PRIMARY KEY (`shop_id`, `promotion_type`, `row_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

ALTER TABLE fact_user_full_reduce_v1
    MODIFY last_join_time timestamp NULL DEFAULT NULL COMMENT '最后参与时间';

-- 增加索引
ALTER TABLE ods_flashsale_period ADD KEY `ods_flashsale_period_activity_id_index` (`activity_id`);
ALTER TABLE ods_flashsale_period ADD KEY `ods_flashsale_period_activity_code_index` (`activity_code`);
ALTER TABLE ods_flashsale_period ADD KEY `ods_flashsale_period_execute_activity_id_index` (`execute_activity_id`);

ALTER TABLE ods_flashsale_appointment ADD KEY `ods_flashsale_appointment_shop_id_index` (`shop_id`);
ALTER TABLE ods_flashsale_appointment ADD KEY `ods_flashsale_appointment_activity_id_index` (`activity_id`);
ALTER TABLE ods_flashsale_appointment ADD KEY `ods_flashsale_appointment_product_id_index` (`product_id`);

-- 新增索引
ALTER TABLE fact_coupon_v1 ADD INDEX f_c_s ( shop_id );
ALTER TABLE fact_coupon_v1 ADD INDEX f_c_c ( coupon_id );
ALTER TABLE fact_coupon_v1 ADD INDEX f_c_ch ( channel );
ALTER TABLE fact_flash_sale_day_v1 ADD INDEX f_f_s_d_r ( row_date );
ALTER TABLE fact_flash_sale_day_v1 ADD INDEX f_f_s_d_p_t ( promotion_type );
ALTER TABLE fact_full_reduce_v1 ADD INDEX f_f_r_r ( row_date );
ALTER TABLE fact_full_reduce_v1 ADD INDEX f_f_r_p ( promotion_id );
ALTER TABLE fact_product_full_reduce_v1 ADD INDEX f_p_f_r_r ( row_date );
ALTER TABLE fact_product_full_reduce_v1 ADD INDEX f_p_f_r_p ( product_id );
ALTER TABLE fact_product_full_reduce_v1 ADD INDEX f_p_f_r_f ( full_reduce_id );
ALTER TABLE fact_product_v1 ADD INDEX f_p_p ( product_id );
ALTER TABLE fact_product_v1 ADD INDEX f_p_s ( shop_id );
ALTER TABLE fact_product_v1 ADD INDEX f_p_c ( channel );
ALTER TABLE fact_promotion_log_v1 ADD INDEX f_p_l_p ( promotion_type );
ALTER TABLE fact_promotion_log_v1 ADD INDEX f_p_l_p_i ( promotion_id );
ALTER TABLE fact_promotion_log_v1 ADD INDEX f_p_l_c ( channel );
ALTER TABLE fact_shop_day_v1 ADD INDEX f_s_d ( channel );
ALTER TABLE fact_shop_hour_v1 ADD INDEX f_s_h ( channel );
ALTER TABLE fact_shop_month_v1 ADD INDEX f_s_m ( channel );
ALTER TABLE fact_user_full_reduce_v1 ADD INDEX f_u_f_r_s ( shop_id );
ALTER TABLE fact_user_full_reduce_v1 ADD INDEX f_u_f_r_m ( member_id );
ALTER TABLE fact_user_full_reduce_v1 ADD INDEX f_u_f_r_f ( full_reduce_id );

-- shop增加字段
ALTER TABLE `ods_shop` ADD COLUMN `del_flag` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除标志 0:未删除，1:已删除' AFTER `story`;

ALTER TABLE `ods_shop` ADD COLUMN `chain_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '连锁名称' AFTER `updated_at`;

ALTER TABLE `ods_shop` ADD COLUMN `is_trial` tinyint(4) NULL DEFAULT 1 COMMENT '是否试用：1-是' AFTER `chain_name`;

ALTER TABLE `ods_shop` ADD COLUMN `product_version_id` int(11) NULL DEFAULT 0 COMMENT '产品版本Id' AFTER `is_trial`;

ALTER TABLE `ods_shop` ADD COLUMN `valid_from` timestamp(0) NULL DEFAULT NULL COMMENT '存续期-开始时间' AFTER `product_version_id`;

ALTER TABLE `ods_shop` ADD COLUMN `valid_to` timestamp(0) NULL DEFAULT NULL COMMENT '存续期-结束时间' AFTER `valid_from`;

ALTER TABLE `ods_shop` ADD COLUMN `last_login_at` timestamp(0) NULL DEFAULT NULL COMMENT '最近登录时间' AFTER `valid_to`;