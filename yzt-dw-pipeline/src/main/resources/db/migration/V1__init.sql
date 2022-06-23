/*
 Navicat MySQL Data Transfer

 Source Server         : yzt_dw_db
 Source Server Type    : MySQL
 Source Server Version : 50728
 Source Host           : 10.10.3.91
 Source Database       : yzt_dw_db

 Target Server Type    : MySQL
 Target Server Version : 50728
 File Encoding         : utf-8

 Date: 09/04/2020 10:38:32 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `dim_biz_channel`
-- ----------------------------
CREATE TABLE `dim_biz_channel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `channel_name` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道名称',
  `channel_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道代码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_coupon`
-- ----------------------------
CREATE TABLE `dim_coupon` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `coupon_id` int(11) DEFAULT NULL COMMENT '优惠劵id',
  `shop_id` int(11) DEFAULT NULL COMMENT '所在店',
  `coupon_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优惠劵名称',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `coupon_type` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '类型 0 满减劵    1 折扣劵   2 随机金额优惠劵  3 包邮劵',
  `issued_quantity` int(11) DEFAULT NULL COMMENT '发放总量',
  `issued_amount` decimal(20,2) DEFAULT NULL COMMENT '发放总金额',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '券有效期开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '券有效期 expiration',
  `coupon_state` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优惠券状态 0:未开始，1:进行中，2:已结束，3:停止发券',
  `use_rule` text COLLATE utf8mb4_unicode_ci COMMENT 'json value',
  `is_visible` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否可见 0:不可见，1:可见',
  `is_recover_issued` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '0:不恢复投放，1：恢复投放',
  `stop_issued_at` timestamp NULL DEFAULT NULL COMMENT '停止投放时间',
  `priority` int(11) DEFAULT NULL COMMENT '优惠券优先级',
  `del_flag` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '逻辑删除标志 0:未删除，1:已删除',
  `created_at` timestamp NULL DEFAULT NULL,
  `start_date` date DEFAULT NULL COMMENT '维度有效日期',
  `end_date` date DEFAULT NULL COMMENT '维度失效日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dim_coupon_uidx` (`coupon_id`,`start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_date`
-- ----------------------------
CREATE TABLE `dim_date` (
  `id` int(11) NOT NULL COMMENT '同day, 格式如20200616',
  `day` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '天 格式为  20200616',
  `day_in_month` int(11) DEFAULT NULL COMMENT '月天序数',
  `day_in_year` int(11) DEFAULT NULL COMMENT '年天序数',
  `month` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '月',
  `month_in_year` int(11) DEFAULT NULL COMMENT '年月序数',
  `year` int(11) DEFAULT NULL COMMENT '年',
  `half_of_year` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上半年  下半年',
  `quarter` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '季度',
  `weekday` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '星期数值',
  `weekday_eng` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '英文星期数值',
  `week` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '这一年的第几周',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_district`
-- ----------------------------
CREATE TABLE `dim_district` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `country` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `region` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域',
  `province` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
  `province_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份代码',
  `city` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `city_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市代码',
  `district` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县',
  `distruct_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县代码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_flash_sale`
-- ----------------------------
CREATE TABLE `dim_flash_sale` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `flash_sale_id` int(11) DEFAULT NULL COMMENT '秒杀id',
  `page_template_id` int(11) DEFAULT NULL COMMENT '页面模板id',
  `shop_id` int(11) DEFAULT NULL COMMENT '门店id',
  `activity_img` varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动主图',
  `activity_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动编码：周期活动相同',
  `activity_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动名称 活动名称',
  `activity_rule` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动时间类型 0：固定时间活动，1:周期活动',
  `start_time` timestamp(3) NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '活动结束时间, 最大时间时表示',
  `current_period` int(4) DEFAULT NULL COMMENT '周期活动时需要,当前周期',
  `period_sum` int(4) DEFAULT NULL COMMENT '周期活动时需要,周期数',
  `activity_state` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动状态：0：未开始,1:预热中,2:进行中,3已结束',
  `cron_rule` varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务调度规则',
  `tag_id` int(11) DEFAULT NULL COMMENT '活动标签id',
  `tag_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动标签名称',
  `preset_num` int(11) DEFAULT NULL COMMENT '预设人数',
  `only_new` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '参与门槛：0 否，1是',
  `buy_limit` int(4) DEFAULT NULL COMMENT '活动限购：0 不限购，其他数：限购数',
  `preheat_hour` int(4) DEFAULT NULL COMMENT '预热时间(小时)',
  `remind_ahead_minute` int(4) DEFAULT NULL COMMENT '提前提醒时间， 0：不提醒，其他则提醒',
  `lock_inventory_setting` int(4) DEFAULT NULL COMMENT '自动取消订单，释放库存时间',
  `barrage_setting` int(4) DEFAULT NULL COMMENT '弹幕配置 0：不开启，1：预热中就开启,2：进行中才开启',
  `joined_show` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '参与人数显示：0否，1显示',
  `buyed_show` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '购买人数显示：0否，1显示',
  `popular_show` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动人气块显示：0否，1显示',
  `index_template_id` int(11) DEFAULT NULL COMMENT '活动主页模版ID',
  `rush_template_id` int(11) DEFAULT NULL COMMENT '抢购模版ID',
  `member_tags` text COLLATE utf8mb4_unicode_ci COMMENT '需要给会员打的标签',
  `created_at` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `disabled` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '是否删除：0否，1是',
  `start_date` date DEFAULT NULL COMMENT '维度有效日期',
  `end_date` date DEFAULT NULL COMMENT '维度失效日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dim_flash_sale_uidx` (`flash_sale_id`,`start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_full_reduce`
-- ----------------------------
CREATE TABLE `dim_full_reduce` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `full_reduce_id` int(11) DEFAULT NULL COMMENT '满减id',
  `shop_id` int(11) DEFAULT NULL COMMENT '门店id',
  `name` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动名称',
  `product_rule` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品规则,json',
  `discount_rule` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优惠规则,json',
  `participate_rule` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '参与次数限制,json',
  `for_first_member` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '仅限新客参与:0-否 1-是',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `state` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优惠券状态 0:未开始，1:进行中，2:已结束',
  `del_flag` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '逻辑删除标志 0:未删除，1:已删除',
  `discount_rule_updated_at` timestamp NULL DEFAULT NULL COMMENT '优惠劵修改时间',
  `is_long_term` tinyint(1) DEFAULT NULL COMMENT '是否长期活动',
  `is_include_all_product` tinyint(1) DEFAULT NULL COMMENT '是否包含全部商品',
  `created_at` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `start_date` date DEFAULT NULL COMMENT '维度有效日期',
  `end_date` date DEFAULT NULL COMMENT '维度失效日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dim_full_reduce_uidx` (`full_reduce_id`,`start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_member`
-- ----------------------------
CREATE TABLE `dim_member` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `member_id` int(11) DEFAULT NULL COMMENT '会员id',
  `member_base_id` int(11) DEFAULT NULL COMMENT '会员在连锁内的id',
  `user_id` int(11) DEFAULT NULL COMMENT '会员在平台内的id',
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺id',
  `main_shop_id` int(11) DEFAULT NULL COMMENT '连锁总店/单店ID',
  `source` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源',
  `remark` varchar(700) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `disabled` tinyint(1) DEFAULT NULL COMMENT '0:活跃 1:已删除',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '姓名',
  `nickname` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `gender` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '性别 0: 未知 1: 男 2: 女',
  `wechat` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信号',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `province` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
  `city` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `country_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区号',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `openid` varchar(48) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '社交帐号openid',
  `openid_type` varchar(48) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '社交帐号平台，如微信，支付宝等',
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `start_date` date DEFAULT NULL COMMENT '维度有效日期',
  `end_date` date DEFAULT NULL COMMENT '维度失效日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dim_member_uidx` (`member_id`,`start_date`)
) ENGINE=InnoDB AUTO_INCREMENT=320228 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_payment_channel`
-- ----------------------------
CREATE TABLE `dim_payment_channel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `channel_name` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付渠道名称',
  `channel_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道代码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_product`
-- ----------------------------
CREATE TABLE `dim_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` int(11) DEFAULT NULL COMMENT '所在店',
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品主图',
  `group_id` int(11) DEFAULT NULL COMMENT '分组id',
  `group_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分组名',
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品名',
  `product_serial` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '序列号',
  `product_desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `share_desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分享描述',
  `detail` text COLLATE utf8mb4_unicode_ci COMMENT '详情',
  `video` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频',
  `product_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '1-实物商品；2-虚拟商品；3-电子卡券',
  `marking_price` decimal(20,2) DEFAULT NULL COMMENT '划线价',
  `weight` decimal(20,2) DEFAULT NULL COMMENT '重量',
  `ship_cost` decimal(20,2) DEFAULT NULL COMMENT '运费',
  `on_shelf_status` tinyint(1) DEFAULT NULL COMMENT '是否在售',
  `allow_refund` tinyint(1) DEFAULT NULL COMMENT '是否可申请退款,0-否；1-是',
  `ship_cost_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '1-统一邮费；2-运费模版',
  `allow_exp_delivery` tinyint(1) DEFAULT NULL COMMENT '快递发货id',
  `allow_pickup` tinyint(1) DEFAULT NULL COMMENT '上门自提id',
  `allow_local_delivery` tinyint(1) DEFAULT NULL COMMENT '是否支持同城配送',
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `start_date` date DEFAULT NULL COMMENT '维度有效日期',
  `end_date` date DEFAULT NULL COMMENT '维度失效日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dim_product_uidx` (`product_id`,`start_date`)
) ENGINE=InnoDB AUTO_INCREMENT=4481 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_product_sku`
-- ----------------------------
CREATE TABLE `dim_product_sku` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sku_id` int(11) DEFAULT NULL COMMENT 'sku id',
  `shop_id` int(11) DEFAULT NULL COMMENT '所在店',
  `sku_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'sku图片',
  `is_default_sku` tinyint(1) DEFAULT NULL COMMENT '是否是默认SKU',
  `purchase_price` decimal(20,2) DEFAULT NULL COMMENT '成本价',
  `retail_price` decimal(20,2) DEFAULT NULL COMMENT '售价',
  `sku_spec` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '规格',
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `start_date` date DEFAULT NULL COMMENT '维度有效日期',
  `end_date` date DEFAULT NULL COMMENT '维度失效日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dim_product_sku_uidx` (`sku_id`,`start_date`)
) ENGINE=InnoDB AUTO_INCREMENT=2315 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_promotion`
-- ----------------------------
CREATE TABLE `dim_promotion` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `promotion_id` int(11) DEFAULT NULL,
  `promotion_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动名称',
  `promotion_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动类型',
  `promotion_type_code` int(11) DEFAULT NULL COMMENT '活动类型 代号  1-优惠劵   2-满减   3-限时抢购',
  `start_date` date DEFAULT NULL COMMENT '维度有效日期',
  `end_date` date DEFAULT NULL COMMENT '维度失效日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dim_promotion_uidx` (`promotion_id`,`promotion_type`,`start_date`)
) ENGINE=InnoDB AUTO_INCREMENT=5976 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `dim_shop`
-- ----------------------------
CREATE TABLE `dim_shop` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` int(11) DEFAULT NULL,
  `shop_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `main_shop_id` int(11) DEFAULT NULL,
  `main_shop_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `group_id` int(11) DEFAULT NULL,
  `group_name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分组名',
  `group_mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分组模式',
  `support_shop_price` tinyint(1) DEFAULT NULL COMMENT '支持门店价',
  `abbr` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简称',
  `business_scope` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主营范围',
  `management_mode` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '经营模式(1: 电商, 2: 品牌商, 3: 单门店经营, 4: 多门店连锁, 5: 其他',
  `logo` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wx_qr_code` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺公众号二维码',
  `country` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `province` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
  `city` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `district` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县',
  `address` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `phone1` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺电话一',
  `phone2` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺电话二',
  `open_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业开始时间',
  `close_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业结束时间',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业状态(1: 营业中, 2: 休息中)',
  `access_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '访问状态: 1: 激活, 2: 未激活, 3: 过期, 4:暂停登录',
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '店铺创建时间',
  `start_date` date DEFAULT NULL COMMENT '维度有效日期',
  `end_date` date DEFAULT NULL COMMENT '维度失效日期',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dim_shop_uidx` (`shop_id`,`start_date`)
) ENGINE=InnoDB AUTO_INCREMENT=686 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_coupon`
-- ----------------------------
CREATE TABLE `fact_coupon` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shop_id` int(11) DEFAULT NULL,
  `coupon_id` int(11) DEFAULT NULL COMMENT '优惠劵id',
  `channel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pay_amount` decimal(38,18) DEFAULT NULL COMMENT '当日支付总金额',
  `preferential_amount` decimal(38,18) DEFAULT NULL COMMENT '优惠总金额',
  `write_off_number` int(11) DEFAULT NULL COMMENT '核销人数',
  `write_off_count` int(11) DEFAULT NULL COMMENT '核销张数',
  `sale_count` int(11) DEFAULT NULL COMMENT '商品销量',
  `order_count` int(11) DEFAULT NULL COMMENT '当日总订单数',
  `order_number` int(11) DEFAULT NULL COMMENT '当日下单人数',
  `new_member_count` int(11) DEFAULT NULL COMMENT '用劵新客',
  `old_member_count` int(11) DEFAULT NULL COMMENT '用劵老客',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4823 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_flash_sale`
-- ----------------------------
CREATE TABLE `fact_flash_sale` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shop_id` int(11) DEFAULT NULL,
  `activity_id` int(11) DEFAULT NULL COMMENT '活动id',
  `activity_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activity_state` tinyint(4) DEFAULT NULL COMMENT '活动状态',
  `pv` int(11) DEFAULT NULL,
  `uv` int(11) DEFAULT NULL,
  `order_count` int(11) DEFAULT NULL COMMENT '当日下单次数',
  `pay_coupon_total` decimal(38,2) DEFAULT NULL,
  `order_number` int(11) DEFAULT NULL COMMENT '当日下单人数',
  `pay_total` decimal(38,2) DEFAULT NULL COMMENT '当日付款总额',
  `pay_count` int(11) DEFAULT NULL COMMENT '当日付款单数',
  `pay_number` int(11) DEFAULT NULL COMMENT '当日付款人数',
  `sale_count` int(11) DEFAULT NULL COMMENT '商品销量',
  `inventory` int(11) DEFAULT NULL COMMENT '剩余库存',
  `share_user_count` int(11) DEFAULT NULL COMMENT '分享人数',
  `share_count` int(11) DEFAULT NULL COMMENT '分享次数',
  `appointment_count` int(11) DEFAULT NULL COMMENT '预约人数',
  PRIMARY KEY (`id`),
  KEY `f_f_s_a` (`activity_id`),
  KEY `f_f_s_a_c` (`activity_code`),
  KEY `f_f_s_s_index` (`shop_id`),
  KEY `f_f_s_r_d` (`row_date`)
) ENGINE=InnoDB AUTO_INCREMENT=6632 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_flash_sale_product`
-- ----------------------------
CREATE TABLE `fact_flash_sale_product` (
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` int(11) DEFAULT NULL COMMENT '商店id',
  `activity_id` int(11) DEFAULT NULL COMMENT '活动id',
  `activity_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动编号',
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品名称',
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品图片地址',
  `activit_price` decimal(38,2) DEFAULT NULL COMMENT '活动价格',
  `marking_price` decimal(38,2) DEFAULT NULL COMMENT '划线价格',
  `pv` int(11) DEFAULT NULL,
  `uv` int(11) DEFAULT NULL,
  `order_count` int(11) DEFAULT NULL COMMENT '当日下单次数',
  `order_number` int(11) DEFAULT NULL COMMENT '当日下单人数',
  `pay_total` decimal(38,2) DEFAULT NULL COMMENT '当日付款总额',
  `pay_coupon_total` decimal(38,2) DEFAULT NULL COMMENT '当日优惠总金额',
  `pay_count` int(11) DEFAULT NULL COMMENT '当日付款单数',
  `pay_number` int(11) DEFAULT NULL COMMENT '当日付款人数',
  `sale_count` int(11) DEFAULT NULL COMMENT '商品销量',
  `inventory` int(11) DEFAULT NULL COMMENT '剩余库存',
  `share_count` int(11) DEFAULT NULL COMMENT '分享次数',
  `share_user_count` int(11) DEFAULT NULL COMMENT '分享人数',
  `appointment_count` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预约人数',
  PRIMARY KEY (`id`),
  KEY `f_f_s_a` (`activity_id`),
  KEY `f_f_s_a_c` (`activity_code`),
  KEY `f_f_s_s_index` (`shop_id`),
  KEY `f_f_s_r_d` (`row_date`)
) ENGINE=InnoDB AUTO_INCREMENT=7241 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_full_reduce`
-- ----------------------------
CREATE TABLE `fact_full_reduce` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺ID',
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `promotion_id` int(11) DEFAULT NULL COMMENT '满减活动id',
  `pv` int(11) DEFAULT NULL,
  `uv` int(11) DEFAULT NULL,
  `product_pv` int(11) DEFAULT NULL COMMENT '主页商品访问量',
  `order_count` int(11) DEFAULT NULL COMMENT '当日总订单数',
  `pay_total` decimal(20,2) DEFAULT NULL COMMENT '付款总额',
  `pay_number` int(11) DEFAULT NULL COMMENT '当日付款人数',
  `sale_count` int(11) DEFAULT NULL COMMENT '商品销量',
  `product_amount` decimal(20,2) DEFAULT NULL COMMENT '活动商品销售额',
  `preferential_amount` decimal(20,2) DEFAULT NULL COMMENT '优惠额',
  `addcart_count` int(11) DEFAULT NULL COMMENT '主页加购次数',
  `share_count` int(11) DEFAULT NULL COMMENT '主页分享次数',
  PRIMARY KEY (`id`),
  KEY `ffr_shop_id_index` (`shop_id`),
  KEY `ffr_row_date_index` (`row_date`)
) ENGINE=InnoDB AUTO_INCREMENT=9451 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_live_room`
-- ----------------------------
CREATE TABLE `fact_live_room` (
  `shop_id` int(11) NOT NULL COMMENT '店铺ID',
  `live_room_id` int(11) NOT NULL COMMENT '直播间id',
  `order_count` int(11) DEFAULT NULL COMMENT '总订单数-只要包含了直播商品的订单都算直播订单',
  `order_amount` decimal(20,2) DEFAULT NULL COMMENT '总订单额',
  `product_amount` decimal(20,2) DEFAULT NULL COMMENT '直播商品销售额',
  `product_count` int(11) DEFAULT NULL COMMENT '直播商品销售量',
  `revived_coupon_count` int(11) DEFAULT NULL COMMENT '通过直播间领取优惠劵的数量',
  `pv` int(11) DEFAULT NULL COMMENT '累计观看次数',
  `uv` int(11) DEFAULT NULL COMMENT '累计观看人数',
  `uv_by_share` int(11) DEFAULT NULL COMMENT '通过分享进入直播间人数',
  `pay_number` int(11) DEFAULT NULL COMMENT '支付订单数',
  `new_number` int(11) DEFAULT NULL COMMENT '拓新客数',
  PRIMARY KEY (`shop_id`,`live_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_log_stream`
-- ----------------------------
CREATE TABLE `fact_log_stream` (
  `fk_shop` int(11) DEFAULT NULL,
  `fk_date` int(11) DEFAULT NULL COMMENT '日期外键',
  `fk_member` int(11) DEFAULT NULL COMMENT '会员外键',
  `fk_channel` int(11) DEFAULT NULL COMMENT '渠道外键',
  `fk_product` int(11) DEFAULT NULL COMMENT '商品外键',
  `fk_promotion` int(11) DEFAULT NULL COMMENT '活动外键',
  `device_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '唯一id 用作计算未登录用户的记录',
  `open_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `device_model` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备型号',
  `device_brand` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备名称',
  `system_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统名称',
  `system_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统版本',
  `app_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'app_version',
  `event_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件名称',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'url',
  `query` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'query参数',
  `keyword` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键字',
  `quantity` int(11) DEFAULT NULL COMMENT '数量',
  `event_time` bigint(20) DEFAULT NULL COMMENT '事件发生时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_member`
-- ----------------------------
CREATE TABLE `fact_member` (
  `member_id` int(11) NOT NULL COMMENT '会员id',
  `shop_id` int(11) NOT NULL COMMENT '店铺id',
  `order_count` int(11) DEFAULT NULL COMMENT '总订单数',
  `total_order_amount` decimal(20,2) DEFAULT NULL COMMENT '总订单额',
  `coupon_order_count` int(11) DEFAULT NULL COMMENT '使用优惠劵订单数',
  `coupon_order_amount` decimal(20,2) DEFAULT NULL COMMENT '使用优惠劵订单总额',
  `full_reduce_order_count` int(11) DEFAULT NULL COMMENT '使用满减订单数',
  `full_reduce_order_amount` decimal(20,2) DEFAULT NULL COMMENT '使用满减订单额',
  `flash_sale_order_count` int(11) DEFAULT NULL COMMENT '使用秒杀订单数',
  `flash_sale_order_amount` decimal(20,2) DEFAULT NULL COMMENT '使用秒杀消费金额',
  `last_order_time` timestamp NULL DEFAULT NULL COMMENT '上次消费时间',
  `last_view_shop_time` timestamp NULL DEFAULT NULL COMMENT '最近浏览店铺时间',
  `last_consignee_address` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近收货地址',
  `refund_amount` decimal(20,2) DEFAULT NULL COMMENT '累计退款金额',
  `refund_count` int(11) DEFAULT NULL COMMENT '累计退款单数',
  `debt_amount` decimal(20,2) DEFAULT NULL COMMENT '欠款',
  `referrer` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '介绍人',
  `follower` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跟踪员工',
  `balance_amount` decimal(20,2) DEFAULT NULL COMMENT '储值余额',
  `gift_amount` decimal(20,2) DEFAULT NULL COMMENT '赠金余额',
  `points` decimal(20,2) DEFAULT NULL COMMENT '积分',
  `first_order_time` timestamp NULL DEFAULT NULL COMMENT '首次付款时间',
  `promotion_type` int(11) DEFAULT NULL COMMENT '首次使用活动类型',
  `promotion_id` int(11) DEFAULT NULL COMMENT '首次使用活动id',
  `first_order_promotion_time` timestamp NULL DEFAULT NULL COMMENT '首次使用活动付款时间',
  `created_at` timestamp NULL DEFAULT NULL COMMENT '建档时间',
  `last_consume_services` text COLLATE utf8mb4_unicode_ci COMMENT '上次消费服务',
  `last_order_id` bigint(20) DEFAULT NULL COMMENT '上次消费订单号',
  `order_count_export` int(11) DEFAULT NULL COMMENT '导入订单数',
  `total_order_amount_export` decimal(20,2) DEFAULT NULL COMMENT '导入订单金额',
  PRIMARY KEY (`member_id`,`shop_id`),
  KEY `index_shopid` (`shop_id`),
  KEY `index_memberid` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_member_promotion`
-- ----------------------------
CREATE TABLE `fact_member_promotion` (
  `member_id` int(11) NOT NULL COMMENT '会员id',
  `shop_id` int(11) NOT NULL COMMENT '店铺id',
  `channel` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道',
  `promotion_type` int(11) NOT NULL COMMENT '活动类型',
  `promotion_id` int(11) NOT NULL COMMENT '活动id',
  `order_count` bigint(20) DEFAULT NULL COMMENT '总订单数',
  `promotion_count` bigint(20) DEFAULT NULL COMMENT '使用活动订单数',
  `original_order_time` timestamp NULL DEFAULT NULL,
  `original_order_promotion_time` timestamp NULL DEFAULT NULL,
  `total_amount` decimal(20,2) DEFAULT NULL COMMENT '总订单额',
  PRIMARY KEY (`member_id`,`shop_id`,`channel`,`promotion_type`,`promotion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_order`
-- ----------------------------
CREATE TABLE `fact_order` (
  `fk_shop` int(11) DEFAULT NULL,
  `fk_date` int(11) DEFAULT NULL COMMENT '日期外键',
  `fk_member` int(11) DEFAULT NULL COMMENT '会员外键',
  `fk_channel` int(11) DEFAULT NULL COMMENT '渠道外键',
  `fk_payment` int(11) DEFAULT NULL COMMENT '支付方式外键',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `union_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'union_no',
  `order_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单号',
  `status` int(11) DEFAULT NULL COMMENT '订单状态,0待付款/未付款,1未发货/待发货,2已发货/待收货,3交易完成/待评价,4交易完成/已评价,\n                                                        5交易关闭/客户主动关闭,51交易关闭/商家主动关闭,52交易关闭/超时关闭,53交易关闭/支付失败,54退款完成',
  `order_type` int(11) DEFAULT NULL COMMENT '订单类型: 0普通订单,1拼团订单,3限时抢购订单,4砍价订单',
  `product_amount` decimal(20,4) DEFAULT NULL COMMENT '商品总金额',
  `ship_cost_amount` decimal(20,4) DEFAULT NULL COMMENT '运费金额',
  `ship_cost_reduced` decimal(20,4) DEFAULT NULL COMMENT '运费减免金额',
  `discount_amount` decimal(20,4) DEFAULT NULL COMMENT '折扣金额',
  `total_amount` decimal(20,4) DEFAULT NULL COMMENT '订单总金额',
  `actual_amount` decimal(20,4) DEFAULT NULL COMMENT '实际付款金额',
  `remark` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注（改价）',
  `ship_method` int(11) DEFAULT NULL COMMENT '配送方式，0物流,1商家配送,2自提',
  `closed_at` timestamp NULL DEFAULT NULL COMMENT '订单关闭时间',
  `received_at` timestamp NULL DEFAULT NULL COMMENT '订单收货时间',
  `paid_at` timestamp NULL DEFAULT NULL COMMENT '订单支付时间',
  `cancel_reason` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关闭订单原因',
  `deleted` int(11) DEFAULT NULL COMMENT '删除标记，0正常，1已删除',
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_order_detail`
-- ----------------------------
CREATE TABLE `fact_order_detail` (
  `fk_shop` int(11) DEFAULT NULL,
  `fk_date` int(11) DEFAULT NULL COMMENT '日期外键',
  `fk_member` int(11) DEFAULT NULL COMMENT '会员外键',
  `fk_channel` int(11) DEFAULT NULL COMMENT '渠道外键',
  `fk_product` int(11) DEFAULT NULL COMMENT '商品外键',
  `fk_sku` int(11) DEFAULT NULL COMMENT 'sku外键',
  `fk_payment` int(11) DEFAULT NULL COMMENT '支付方式外键',
  `fk_promotion` int(11) DEFAULT NULL COMMENT '活动外键',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单id',
  `quantity` int(11) DEFAULT NULL COMMENT '数量',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '金额=retail_price（售价）  x  quantity',
  `discount_amount` decimal(20,4) DEFAULT NULL COMMENT '总优惠金额  分开求会有金额交叉情况',
  `is_giveaway` tinyint(1) DEFAULT NULL COMMENT '是否赠品'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_product`
-- ----------------------------
CREATE TABLE `fact_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺id',
  `channel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pv` int(11) DEFAULT NULL,
  `uv` int(11) DEFAULT NULL,
  `sale_total` decimal(38,18) DEFAULT NULL COMMENT '销售额',
  `sale_count` int(11) DEFAULT NULL COMMENT '销量',
  `order_count` int(11) DEFAULT NULL COMMENT '当日总订单数',
  `order_number` int(11) DEFAULT NULL COMMENT '当日下单人数',
  `pay_count` int(11) DEFAULT NULL COMMENT '当日付款单数',
  `pay_number` int(11) DEFAULT NULL COMMENT '当日付款人数',
  `inventory` int(11) DEFAULT NULL COMMENT '剩余库存',
  `share_count` int(11) DEFAULT NULL COMMENT '分享次数',
  `share_number` int(11) DEFAULT NULL COMMENT '分享人数',
  `addcart_count` int(11) DEFAULT NULL COMMENT '加购次数',
  `addcart_number` int(11) DEFAULT NULL COMMENT '加购人数',
  PRIMARY KEY (`id`),
  KEY `fp_row_date_index` (`row_date`),
  KEY `fp_shop_id_index` (`shop_id`),
  KEY `fp_product_id_index` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6174 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_product_full_reduce`
-- ----------------------------
CREATE TABLE `fact_product_full_reduce` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺ID',
  `product_id` int(11) DEFAULT NULL COMMENT '商品id',
  `full_reduce_id` int(11) DEFAULT NULL COMMENT '满减id',
  `pv` int(11) DEFAULT NULL COMMENT '通过满减活动的pv',
  `uv` int(11) DEFAULT NULL COMMENT '通过满减活动的uv',
  `sale_total` decimal(38,18) DEFAULT NULL COMMENT '销售额_通过满减活动',
  `sale_count` int(11) DEFAULT NULL COMMENT '销量_通过满减活动',
  `pay_count` int(11) DEFAULT NULL COMMENT '当日付款单数_通过满减活动',
  `pay_number` int(11) DEFAULT NULL COMMENT '当日付款人数_通过满减活动',
  `discount_amount` decimal(38,18) DEFAULT NULL COMMENT '活动优惠总金额',
  `share_count` int(11) DEFAULT '0' COMMENT '当日分享次数',
  PRIMARY KEY (`id`),
  KEY `fpfr_row_date_index` (`row_date`),
  KEY `fpfr_shop_id_index` (`shop_id`),
  KEY `fpfr_product_id_index` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=109326 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_shop`
-- ----------------------------
CREATE TABLE `fact_shop` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `channel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shop_id` int(11) DEFAULT NULL,
  `main_shop_id` int(11) DEFAULT NULL,
  `pv` int(11) DEFAULT NULL,
  `uv` int(11) DEFAULT NULL,
  `share_count` int(11) DEFAULT NULL COMMENT '分享次数',
  `all_amount` decimal(20,2) DEFAULT NULL COMMENT '累计订单额',
  `all_member` int(11) DEFAULT NULL COMMENT '截止当前累计客户数',
  `paied_member` int(11) DEFAULT NULL COMMENT '已消费客户数-派生指标',
  `not_paied_member` int(11) DEFAULT NULL COMMENT '未消费客户数-派生指标',
  `first_paied_member` int(11) DEFAULT NULL COMMENT '首购客户数-派生指标',
  `not_first_paied_member` int(11) DEFAULT NULL COMMENT '非首购客户数-派生指标',
  `pay_amount` decimal(20,2) DEFAULT NULL COMMENT '当日支付订单总额',
  `promotion_count` int(11) DEFAULT NULL COMMENT '有效活动数',
  `promotion_order_amount` decimal(20,2) DEFAULT NULL COMMENT '活动订单总额',
  `promotion_discount_amount` decimal(20,2) DEFAULT NULL COMMENT '优惠总金额',
  `all_refund_amount` decimal(20,2) DEFAULT NULL COMMENT '累计退款金额',
  PRIMARY KEY (`id`),
  KEY `shop_row_date_index` (`row_date`) USING BTREE,
  KEY `shop_shop_id_index` (`shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=78679 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_shop_daily`
-- ----------------------------
CREATE TABLE `fact_shop_daily` (
  `fk_shop` int(11) DEFAULT NULL,
  `fk_date` int(11) DEFAULT NULL COMMENT '日期外键',
  `fk_channel` int(11) DEFAULT NULL COMMENT '渠道外键',
  `sold_count` int(11) DEFAULT NULL COMMENT '商品销量',
  `pv` int(11) DEFAULT NULL,
  `uv` int(11) DEFAULT NULL,
  `share_count` int(11) DEFAULT NULL COMMENT '分享次数',
  `share_member_count` int(11) DEFAULT NULL COMMENT '分享人次',
  `add_cart_count` int(11) DEFAULT NULL COMMENT '加入购物车数量',
  `add_cart_member_count` int(11) DEFAULT NULL COMMENT '加入购物车人次'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_shop_day`
-- ----------------------------
CREATE TABLE `fact_shop_day` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '日期',
  `channel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道',
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺ID',
  `main_shop_id` int(11) DEFAULT NULL COMMENT '总店id',
  `pv` int(11) DEFAULT NULL COMMENT '浏览量',
  `uv` int(11) DEFAULT NULL COMMENT '访客',
  `addcart_member` int(11) DEFAULT NULL COMMENT '加购人数',
  `order_count` int(11) DEFAULT NULL COMMENT '订单数',
  `order_member` int(11) DEFAULT NULL COMMENT '下单人数',
  `pay_member` int(11) DEFAULT NULL COMMENT '付款人数',
  `register_count` int(11) DEFAULT NULL COMMENT '注册用户数',
  `pay_amount` decimal(20,2) DEFAULT NULL COMMENT '支付总金额',
  `pay_count` int(11) DEFAULT NULL COMMENT '付款订单数',
  `refund_count` int(11) DEFAULT NULL COMMENT '退款订单数',
  `refund_amount` decimal(20,2) DEFAULT NULL COMMENT '退款金额',
  `uv_product` int(11) DEFAULT NULL COMMENT '店铺下商品访问人数',
  PRIMARY KEY (`id`),
  KEY `s_d_shop_id_index` (`shop_id`) USING BTREE,
  KEY `fsd_row_date_index` (`row_date`)
) ENGINE=InnoDB AUTO_INCREMENT=77488 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_shop_hour`
-- ----------------------------
CREATE TABLE `fact_shop_hour` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `channel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shop_id` int(11) DEFAULT NULL,
  `main_shop_id` int(11) DEFAULT NULL,
  `pv` int(11) DEFAULT NULL,
  `uv` int(11) DEFAULT NULL,
  `addcart_count` int(11) DEFAULT NULL COMMENT '加购次数',
  `addcart_number` int(11) DEFAULT NULL COMMENT '加购人数',
  `share_count` int(11) DEFAULT NULL COMMENT '分享次数',
  `share_number` int(11) DEFAULT NULL COMMENT '分享人数',
  `order_count` int(11) DEFAULT NULL COMMENT '下单次数',
  `order_number` int(11) DEFAULT NULL COMMENT '下单人数',
  `pay_count` int(11) DEFAULT NULL COMMENT '付款次数',
  `pay_number` int(11) DEFAULT NULL COMMENT '付款人数',
  `sale_count` int(11) DEFAULT NULL COMMENT '商品销量-不含退款',
  `pay_amount` decimal(20,2) DEFAULT NULL COMMENT '付款总额',
  `order_amount` decimal(20,2) DEFAULT NULL COMMENT '下单金额',
  `refund_count` int(11) DEFAULT NULL COMMENT '退款单数',
  `refund_amount` decimal(20,2) DEFAULT NULL COMMENT '退款金额',
  PRIMARY KEY (`id`),
  KEY `s_h_row_time_index` (`row_time`) USING BTREE,
  KEY `s_h_shop_id_index` (`shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=319230 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_shop_month`
-- ----------------------------
CREATE TABLE `fact_shop_month` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `month` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '月',
  `channel` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道',
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺ID',
  `main_shop_id` int(11) DEFAULT NULL COMMENT '总店id',
  `pv` int(11) DEFAULT NULL COMMENT '浏览量',
  `order_count` int(11) DEFAULT NULL COMMENT '订单数',
  `register_count` int(11) DEFAULT NULL COMMENT '注册用户数',
  `pay_amount` decimal(20,2) DEFAULT NULL COMMENT '支付总金额',
  `pay_count` int(11) DEFAULT NULL COMMENT '付款订单数',
  `refund_count` int(11) DEFAULT NULL COMMENT '退款订单数',
  `refund_amount` decimal(20,2) DEFAULT NULL COMMENT '退款金额',
  PRIMARY KEY (`id`),
  KEY `s_m_month` (`month`) USING BTREE,
  KEY `s_m_shop_id` (`shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6268 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `fact_user_full_reduce`
-- ----------------------------
CREATE TABLE `fact_user_full_reduce` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `row_date` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺ID',
  `member_id` int(11) DEFAULT NULL COMMENT '商品id',
  `full_reduce_id` int(11) DEFAULT NULL COMMENT '满减id',
  `order_count` int(11) DEFAULT NULL COMMENT '通过满减活动的订单数',
  `sale_count` int(11) DEFAULT NULL COMMENT '通过满减活动商品购买数',
  `pay_amount` decimal(38,18) DEFAULT NULL COMMENT '支付总金额',
  `discount_amount` decimal(38,18) DEFAULT NULL COMMENT '优惠总金额',
  `share_count_full_reduce` int(11) DEFAULT NULL COMMENT '活动分享次数',
  `share_count_product` int(11) DEFAULT NULL COMMENT '商品分享次数',
  `last_join_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后参与时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2702 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_category_template`
-- ----------------------------
CREATE TABLE `ods_category_template` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL,
  `template_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_consignee_addr`
-- ----------------------------
CREATE TABLE `ods_consignee_addr` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL COMMENT 'user表主键',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `consignee` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人',
  `country` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `province` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `district` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区县',
  `town` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '乡镇',
  `address` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `building` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '房屋，建筑信息',
  `postcode` varchar(6) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮编',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `is_default` tinyint(4) DEFAULT '0' COMMENT '是否是默认地址 1: 是, 0: 不是',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_coupon`
-- ----------------------------
CREATE TABLE `ods_coupon` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL COMMENT 'id',
  `shop_id` int(11) NOT NULL COMMENT '门店id',
  `coupon_template_id` int(11) NOT NULL COMMENT '优惠券模板id',
  `member_id` int(11) NOT NULL COMMENT '会员id',
  `member_name` varchar(32) NOT NULL COMMENT '姓名',
  `member_phone` varchar(16) NOT NULL COMMENT '手机号',
  `coupon_code` varchar(32) NOT NULL COMMENT '优惠券券码 唯一',
  `coupon_type` tinyint(4) NOT NULL COMMENT '优惠券类型',
  `discount_amount` decimal(12,2) DEFAULT '0.00' COMMENT '优惠金额',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '生效时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '失效时间',
  `source` varchar(32) NOT NULL COMMENT '领取来源类型',
  `verification_type` tinyint(4) DEFAULT '0' COMMENT '核销类型，0:未核销 1:商城核销，2:验证工具核销',
  `can_return` tinyint(4) DEFAULT '0' COMMENT '0 :不可退，1:可退',
  `state` tinyint(4) DEFAULT '0' COMMENT '用券状态 0:未使用，1:已使用，2:已失效，3:作废',
  `used_at` timestamp NULL DEFAULT NULL COMMENT '核销时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券领取表';

-- ----------------------------
--  Table structure for `ods_coupon_template`
-- ----------------------------
CREATE TABLE `ods_coupon_template` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL COMMENT 'id',
  `shop_id` int(11) NOT NULL COMMENT '门店id',
  `coupon_name` varchar(32) NOT NULL COMMENT '优惠券名称',
  `description` varchar(750) NOT NULL COMMENT '优惠券描述',
  `coupon_type` tinyint(4) NOT NULL COMMENT '优惠券类型',
  `issued_quantity` int(11) DEFAULT NULL COMMENT '发放总量',
  `inventory` int(11) DEFAULT NULL COMMENT '剩余库存',
  `verified_quantity` int(11) DEFAULT '0' COMMENT '已核销数',
  `issued_amount` decimal(12,2) DEFAULT '0.00' COMMENT '发放金额总量',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '券有效期开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '券有效期 expiration',
  `coupon_state` tinyint(4) NOT NULL COMMENT '优惠券状态 0:未开始，1:进行中，2:已结束，3:停止发券',
  `use_rule` text NOT NULL COMMENT 'json value',
  `is_visible` tinyint(4) DEFAULT '1' COMMENT '是否可见 0:不可见，1:可见',
  `is_recover_issued` tinyint(4) DEFAULT '1' COMMENT '0:不恢复投放，1：恢复投放',
  `stop_issued_at` timestamp NULL DEFAULT NULL COMMENT '停止投放时间',
  `priority` int(11) DEFAULT '1' COMMENT '优惠券优先级',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '逻辑删除标志 0:未删除，1:已删除',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板';

-- ----------------------------
--  Table structure for `ods_coupon_use_record`
-- ----------------------------
CREATE TABLE `ods_coupon_use_record` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL COMMENT 'id',
  `shop_id` int(11) NOT NULL COMMENT '门店id',
  `member_id` int(11) NOT NULL COMMENT '会员id',
  `coupon_template_id` int(11) NOT NULL COMMENT '优惠券模板id',
  `coupon_id` bigint(20) NOT NULL COMMENT '优惠券id',
  `promotion_type` tinyint(4) NOT NULL COMMENT '活动类型',
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `product_name` varchar(255) NOT NULL COMMENT '商品名称',
  `product_image` varchar(255) NOT NULL COMMENT '商品图片',
  `sku_id` int(11) NOT NULL COMMENT '商品SKU id',
  `sku_price` decimal(20,4) NOT NULL COMMENT 'SKU价格',
  `coupon_code` varchar(32) NOT NULL COMMENT '优惠券券码',
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `product_quantity` int(11) NOT NULL COMMENT '件数',
  `total_amount` decimal(20,4) NOT NULL COMMENT '订单总金额，不含优惠金额',
  `paid_amount` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '实付金额, 支付后更新',
  `discount_amount` decimal(20,4) NOT NULL COMMENT '优惠金额',
  `is_refund` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否退款',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券使用日志记录表';

-- ----------------------------
--  Table structure for `ods_emall_order`
-- ----------------------------
CREATE TABLE `ods_emall_order` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL,
  `main_shop_id` int(11) NOT NULL COMMENT '主店id, 为0时表示单店(非连锁)',
  `shop_id` int(11) NOT NULL COMMENT '门店id',
  `union_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '父单号',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `transaction_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付流水号',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付方式',
  `source` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单来源',
  `status` tinyint(4) NOT NULL COMMENT '订单状态,0待付款/未付款,1未发货/待发货,2已发货/待收货,3交易完成/待评价,4交易完成/已评价,\n                                                        5交易关闭/客户主动关闭,51交易关闭/商家主动关闭,52交易关闭/超时关闭,53交易关闭/支付失败,54退款完成',
  `order_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单类型: 0普通订单,1拼团订单,3限时抢购订单,4砍价订单',
  `product_amount` decimal(20,2) NOT NULL COMMENT '商品总金额',
  `ship_cost_amount` decimal(20,2) NOT NULL COMMENT '运费金额',
  `ship_cost_reduced` decimal(20,2) NOT NULL COMMENT '运费减免金额',
  `discount_amount` decimal(20,2) NOT NULL COMMENT '折扣金额',
  `total_amount` decimal(20,2) NOT NULL COMMENT '订单总金额',
  `actual_amount` decimal(20,2) NOT NULL COMMENT '实际付款金额',
  `remark` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注（改价）',
  `member_id` int(11) NOT NULL COMMENT '会员id',
  `member_name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会员姓名',
  `member_phone` varchar(11) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会员手机',
  `member_remark` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会员备注',
  `openid` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户的微信openId',
  `ship_method` tinyint(4) NOT NULL COMMENT '配送方式，0物流,1商家配送,2自提',
  `remind_ship_count` tinyint(4) NOT NULL DEFAULT '0' COMMENT '提醒发货次数',
  `closed_at` timestamp NULL DEFAULT NULL COMMENT '订单关闭时间',
  `received_at` timestamp NULL DEFAULT NULL COMMENT '订单收货时间',
  `paid_at` timestamp NULL DEFAULT NULL COMMENT '订单支付时间',
  `cancel_reason` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关闭订单原因',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标记，0正常，1已删除',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `shop_id_index` (`shop_id`) USING BTREE,
  KEY `source_index` (`source`) USING BTREE,
  KEY `created_at_index` (`created_at`) USING BTREE,
  KEY `e_o_ct_sid_index` (`created_at`,`shop_id`),
  KEY `o_e_order_index` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_flashsale_activity`
-- ----------------------------
CREATE TABLE `ods_flashsale_activity` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL COMMENT 'id',
  `shop_id` int(11) NOT NULL COMMENT '门店id',
  `activity_img` varchar(400) NOT NULL COMMENT '活动主图',
  `activity_code` varchar(100) NOT NULL COMMENT '活动编码：周期活动相同',
  `activity_name` varchar(50) NOT NULL COMMENT '活动名称 活动名称',
  `activity_rule` tinyint(4) NOT NULL COMMENT '活动时间类型 0：固定时间活动，1:周期活动',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '活动结束时间, 最大时间时表示',
  `current_period` int(4) DEFAULT NULL COMMENT '周期活动时需要,当前周期',
  `period_sum` int(4) DEFAULT NULL COMMENT '周期活动时需要,周期数',
  `activity_state` tinyint(4) NOT NULL COMMENT '活动状态：0：未开始,1:预热中,2:进行中,3已结束',
  `cron_rule` varchar(400) DEFAULT NULL COMMENT '任务调度规则',
  `tag_id` int(11) NOT NULL COMMENT '活动标签id',
  `tag_name` varchar(200) NOT NULL COMMENT '活动标签名称',
  `preset_num` int(11) NOT NULL DEFAULT '0' COMMENT '预设人数',
  `only_new` tinyint(1) NOT NULL DEFAULT '0' COMMENT '参与门槛：0 否，1是',
  `buy_limit` int(4) NOT NULL DEFAULT '0' COMMENT '活动限购：0 不限购，其他数：限购数',
  `preheat_hour` int(4) NOT NULL DEFAULT '0' COMMENT '预热时间(小时)',
  `remind_ahead_minute` int(4) NOT NULL DEFAULT '0' COMMENT '提前提醒时间， 0：不提醒，其他则提醒',
  `lock_inventory_setting` int(4) NOT NULL DEFAULT '0' COMMENT '自动取消订单，释放库存时间',
  `barrage_setting` int(4) NOT NULL DEFAULT '0' COMMENT '弹幕配置 0：不开启，1：预热中就开启,2：进行中才开启',
  `joined_show` tinyint(4) NOT NULL COMMENT '参与人数显示：0否，1显示',
  `buyed_show` tinyint(4) NOT NULL COMMENT '购买人数显示：0否，1显示',
  `popular_show` tinyint(4) NOT NULL COMMENT '活动人气块显示：0否，1显示',
  `index_template_id` int(11) NOT NULL COMMENT '活动主页模版ID',
  `rush_template_id` int(11) NOT NULL COMMENT '抢购模版ID',
  `member_tags` text COMMENT '需要给会员打的标签',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `disabled` tinyint(1) DEFAULT '0' COMMENT '是否删除：0否，1是',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '库存数据版本',
  `destory_time` timestamp NULL DEFAULT NULL COMMENT '活动结束时间',
  PRIMARY KEY (`id`),
  KEY `f_a_time_index` (`start_time`,`end_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动';

-- ----------------------------
--  Table structure for `ods_flashsale_activity_product`
-- ----------------------------
CREATE TABLE `ods_flashsale_activity_product` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `activity_id` int(11) NOT NULL COMMENT '活动编号',
  `recommend` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否推荐：0否，1是',
  `recommend_index` tinyint(1) NOT NULL DEFAULT '0' COMMENT '需要推荐排序',
  `reserved_num` int(11) NOT NULL DEFAULT '0' COMMENT '预设预约数',
  `pv_num` int(11) NOT NULL DEFAULT '0' COMMENT '预设浏览数',
  `uv_num` int(11) NOT NULL DEFAULT '0' COMMENT '预设访问数',
  `us_num` int(11) NOT NULL DEFAULT '0' COMMENT '预设分享数',
  `percentage_of_sales` int(11) NOT NULL DEFAULT '1' COMMENT '预设销售百分比',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `f_a_p_aid_index` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_flashsale_appointment`
-- ----------------------------
CREATE TABLE `ods_flashsale_appointment` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL COMMENT '被提醒的用户id',
  `user_name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '用户名称',
  `activity_id` int(11) NOT NULL COMMENT '活动id',
  `product_id` int(11) NOT NULL DEFAULT '0' COMMENT '商品id',
  `remind_content` text COLLATE utf8mb4_unicode_ci COMMENT '提醒内容',
  `remind_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提醒类别:activity活动、product商品',
  `priority` int(11) DEFAULT '0' COMMENT '提醒优先级',
  `number_of_reminders` int(11) DEFAULT '0' COMMENT '提醒次数',
  `reminder_record` text COLLATE utf8mb4_unicode_ci COMMENT '提醒记录Json',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_flashsale_page_template`
-- ----------------------------
CREATE TABLE `ods_flashsale_page_template` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '页面名称',
  `cover` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面',
  `description` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '页面内容',
  `preview` text COLLATE utf8mb4_unicode_ci COMMENT '页面预览内容',
  `draft` text COLLATE utf8mb4_unicode_ci COMMENT '页面内容草稿',
  `page_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '页面类型',
  `is_default` int(11) NOT NULL COMMENT '是否默认模板',
  `priority` int(11) DEFAULT '0' COMMENT '排序优先级',
  `status` tinyint(4) NOT NULL COMMENT '页面状态. 1-草稿,2-预览,3-已发布',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_flashsale_period`
-- ----------------------------
CREATE TABLE `ods_flashsale_period` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL COMMENT '门店id',
  `activity_id` int(11) NOT NULL COMMENT '创建时候活动编号',
  `execute_activity_id` int(11) DEFAULT NULL COMMENT '执行周期时候的活动编号',
  `activity_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动编码：周期活动相同',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '当前周期开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '当前周期结束时间',
  `current_period` int(4) DEFAULT NULL COMMENT '当前周期',
  `period_sum` int(4) DEFAULT NULL COMMENT '周期数',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `finshed` tinyint(1) NOT NULL COMMENT '是否已执行完：0否，1是',
  `disabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0否，1是',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '库存数据版本',
  PRIMARY KEY (`id`),
  KEY `f_p_time_aid_index` (`start_time`,`end_time`,`execute_activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_full_reduce_promotion`
-- ----------------------------
CREATE TABLE `ods_full_reduce_promotion` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL COMMENT 'id',
  `shop_id` int(11) NOT NULL COMMENT '门店id',
  `name` varchar(20) NOT NULL COMMENT '活动名称',
  `product_rule` varchar(3000) DEFAULT NULL,
  `discount_rule` varchar(9900) DEFAULT NULL,
  `participate_rule` varchar(200) DEFAULT NULL COMMENT '参与次数限制,json',
  `for_first_member` tinyint(4) DEFAULT NULL COMMENT '仅限新客参与:0-否 1-是',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `state` tinyint(4) NOT NULL COMMENT '优惠券状态 0:未开始，1:进行中，2:已结束',
  `del_flag` tinyint(4) DEFAULT '0' COMMENT '逻辑删除标志 0:未删除，1:已删除',
  `discount_rule_updated_at` timestamp NULL DEFAULT NULL COMMENT '优惠券规则修改时间',
  `is_long_term` tinyint(1) DEFAULT '0' COMMENT '是否是长期活动',
  `is_include_all_product` tinyint(1) DEFAULT '0' COMMENT '是否包括全部商品',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='满减满赠';

-- ----------------------------
--  Table structure for `ods_full_reduce_promotion_order`
-- ----------------------------
CREATE TABLE `ods_full_reduce_promotion_order` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL COMMENT 'id',
  `shop_id` int(11) NOT NULL COMMENT '门店id',
  `member_id` int(11) NOT NULL COMMENT '会员id',
  `promotion_id` bigint(20) NOT NULL COMMENT '活动id:full_reduce_promotion表主键',
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `total_amount` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '订单总金额，不含优惠金额',
  `paid_amount` decimal(20,4) NOT NULL DEFAULT '0.0000' COMMENT '实付金额, 支付后更新',
  `is_paid` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否支付',
  `is_refund` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否退款',
  `is_closed` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否关闭',
  `apply_discount_rule` varchar(1000) DEFAULT NULL COMMENT '适用的优惠规则,json',
  `total_price` decimal(20,4) NOT NULL COMMENT '商品总价格',
  `discount_amount` decimal(20,4) NOT NULL COMMENT '优惠金额',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `fpo_oid_pid_index` (`order_id`,`promotion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动记录';

-- ----------------------------
--  Table structure for `ods_full_reduce_promotion_order_item`
-- ----------------------------
CREATE TABLE `ods_full_reduce_promotion_order_item` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL COMMENT 'id',
  `full_reduce_promotion_order_id` bigint(20) NOT NULL COMMENT 'full_reduce_promotion_order主键',
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `product_name` varchar(255) NOT NULL COMMENT '商品名称',
  `sku_id` int(11) NOT NULL COMMENT '商品SKU id',
  `retail_price` decimal(20,4) NOT NULL COMMENT '售价',
  `quantity` int(11) NOT NULL COMMENT '件数',
  `total_price` decimal(20,4) NOT NULL COMMENT '商品总价格',
  `discount_amount` decimal(20,4) NOT NULL COMMENT '优惠金额',
  PRIMARY KEY (`id`),
  KEY `fpoi_oid_pid_index` (`full_reduce_promotion_order_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动商品项记录';

-- ----------------------------
--  Table structure for `ods_live_order_item`
-- ----------------------------
CREATE TABLE `ods_live_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `shop_id` int(11) NOT NULL COMMENT '店铺id',
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `live_room_id` int(11) NOT NULL COMMENT '直播间id',
  `product_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1-实物商品；2-虚拟商品；3-电子卡券',
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `sku_id` int(11) NOT NULL COMMENT 'SKUid',
  `quantity` int(11) NOT NULL COMMENT '商品数量',
  `retail_price` decimal(20,2) DEFAULT NULL COMMENT '售价',
  `total_amount` decimal(20,2) DEFAULT NULL COMMENT '总价',
  `actual_amount` decimal(20,2) DEFAULT NULL COMMENT '优惠后的总价',
  `discount_amount` decimal(20,2) DEFAULT NULL COMMENT '优惠总金额',
  `member_id` int(11) NOT NULL COMMENT '会员id',
  `member_name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会员姓名',
  `member_phone` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会员手机',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单状态, 0待付款， 1已付款',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `live_order_item_shop_id_idx` (`shop_id`),
  KEY `live_order_item_live_room_id_idx` (`live_room_id`),
  KEY `live_order_item_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='直播商品和直播间关联表';

-- ----------------------------
--  Table structure for `ods_log_stream`
-- ----------------------------
CREATE TABLE `ods_log_stream` (
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺',
  `main_shop_id` int(11) DEFAULT NULL COMMENT '总店',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `goods_id` int(11) DEFAULT NULL COMMENT '商品id',
  `taro_env` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道',
  `promotion_type` int(11) DEFAULT NULL COMMENT '活动类型',
  `promotion_id` int(11) DEFAULT NULL COMMENT '活动id',
  `device_id` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '唯一id 用作计算未登录用户的记录',
  `open_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `device_model` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备型号',
  `device_brand` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备名称',
  `system_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统名称',
  `system_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '系统版本',
  `app_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'app_version',
  `event_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件名称',
  `url` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'url',
  `query` text COLLATE utf8mb4_unicode_ci COMMENT 'query参数',
  `keyword` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关键字',
  `quantity` int(11) DEFAULT NULL COMMENT '数量',
  `event_time` timestamp NULL DEFAULT NULL COMMENT '事件发生时间',
  `live_room_id` int(11) DEFAULT NULL COMMENT '直播间id',
  `source` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源',
  `user_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户姓名',
  `user_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户手机号',
  `goods_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品名称',
  `coupon_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优惠劵名称',
  `uuid` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件id',
  `begin_time` bigint(20) DEFAULT NULL COMMENT '浏览开始时间',
  `end_time` bigint(20) DEFAULT NULL COMMENT '浏览结束时间',
  KEY `idx_promotionid_eventtime` (`promotion_id`,`event_time`),
  KEY `idx_eventtime_shopid` (`event_time`,`shop_id`),
  KEY `idx_goodsid_eventtime` (`goods_id`,`event_time`),
  KEY `idx_userid_eventtime_goodsid` (`user_id`,`event_time`,`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_member_base`
-- ----------------------------
CREATE TABLE `ods_member_base` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL COMMENT 'user表主键',
  `main_shop_id` int(11) DEFAULT '0' COMMENT '连锁总店/单店ID',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `nickname` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `gender` tinyint(4) DEFAULT '0' COMMENT '性别 0: 未知 1: 男 2: 女',
  `wechat` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信号',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `province` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
  `city` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_member_extend`
-- ----------------------------
CREATE TABLE `ods_member_extend` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_member_id` int(11) NOT NULL COMMENT 'shop_member主键',
  `balance_amount` decimal(20,2) DEFAULT NULL COMMENT '储值余额',
  `gift_amount` decimal(20,2) DEFAULT NULL COMMENT '赠金余额',
  `points` decimal(20,2) DEFAULT NULL COMMENT '积分',
  `debt_amount` decimal(20,2) DEFAULT NULL COMMENT '欠款',
  `consume_times` int(11) DEFAULT NULL COMMENT '消费次数',
  `consume_amount` decimal(20,2) DEFAULT NULL COMMENT '消费金额',
  `last_consume_date` datetime DEFAULT NULL COMMENT '上次消费时间',
  `last_consume_services` text COLLATE utf8mb4_unicode_ci COMMENT '上次消费服务',
  `last_service_staff` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上次服务员工',
  `referrer` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '介绍人',
  `follower` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跟踪员工',
  `custom1` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义字段1',
  `custom2` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义字段2',
  `custom3` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义字段3',
  `custom4` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义字段4',
  `custom5` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义字段5',
  `custom6` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义字段6',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_order_consignee`
-- ----------------------------
CREATE TABLE `ods_order_consignee` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `phone` varchar(11) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人手机',
  `country_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家区号',
  `country` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `province` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货省份',
  `city` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货城市',
  `district` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货区/县',
  `address` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货/提货地址',
  `pickup_address_id` int(11) DEFAULT NULL COMMENT '自提地址id',
  `pickup_time` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自提时间',
  `edit_count` tinyint(4) NOT NULL COMMENT '收货信息修改次数',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_order_item`
-- ----------------------------
CREATE TABLE `ods_order_item` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `product_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1-实物商品；2-虚拟商品；3-电子卡券',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品描述',
  `has_sku` bit(1) NOT NULL DEFAULT b'0' COMMENT 'SKUid',
  `sku_id` int(11) NOT NULL COMMENT 'SKUid',
  `sku_text` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SKU的文本',
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片地址',
  `quantity` int(11) NOT NULL COMMENT '数量',
  `weight` decimal(20,2) NOT NULL COMMENT '重量',
  `marking_price` decimal(20,2) DEFAULT NULL COMMENT '划线价',
  `retail_price` decimal(20,2) DEFAULT NULL COMMENT '售价',
  `actual_amount` decimal(20,2) DEFAULT NULL COMMENT '实付',
  `total_amount` decimal(20,2) DEFAULT NULL COMMENT '总价',
  `discount_amount` decimal(20,2) DEFAULT NULL COMMENT '折扣',
  `ship_cost_type` tinyint(4) DEFAULT NULL COMMENT '0-统一邮费；1-运费模版',
  `ship_cost` decimal(20,2) DEFAULT NULL COMMENT '运费',
  `ship_cost_tpl_id` int(11) DEFAULT NULL COMMENT '运费模版id',
  `deduction_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1-拍下减库存；2-支付减库存',
  `product_remark` text COLLATE utf8mb4_unicode_ci COMMENT '商品购买备注',
  `product_restriction` text COLLATE utf8mb4_unicode_ci COMMENT '电子卡券使用限制',
  `show_voucher` bit(1) DEFAULT b'0' COMMENT '虚拟商品是否展示电子凭证',
  `allow_refund` bit(1) DEFAULT b'0' COMMENT '是否支持退款',
  `allow_express_delivery` bit(1) DEFAULT b'0' COMMENT '是否支持快递发货',
  `allow_city_delivery` bit(1) DEFAULT b'0' COMMENT '是否支持同城配送',
  `allow_self_pickup` bit(1) DEFAULT b'0' COMMENT '是否支持用户自提',
  `is_giveaway` bit(1) DEFAULT b'0' COMMENT '是否是赠品',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `o_i_oid_pid_index` (`order_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_order_promotion`
-- ----------------------------
CREATE TABLE `ods_order_promotion` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` bigint(20) NOT NULL,
  `order_id` int(11) NOT NULL COMMENT '订单ID',
  `promotion_type` tinyint(4) NOT NULL COMMENT '活动类型',
  `promotion_id` int(11) NOT NULL COMMENT '活动ID',
  `promotion_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `discount_amount` decimal(20,2) NOT NULL COMMENT '折扣金额',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `po_oid_pid_index` (`created_at`,`order_id`,`promotion_id`),
  KEY `o_p_promotion_id_index` (`promotion_id`),
  KEY `o_p_order_id_index` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_page_category`
-- ----------------------------
CREATE TABLE `ods_page_category` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '页面分类名称',
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '页面分类描述',
  `sort_first` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '一级排序规则枚举, SortType',
  `sort_second` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '二级规则规则枚举, SortType',
  `is_default` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否默认页面分类',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '分类状态. 1-草稿,2-预览,3-已发布',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_page_template`
-- ----------------------------
CREATE TABLE `ods_page_template` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `name` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '页面名称',
  `cover` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面',
  `description` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '页面内容',
  `preview` text COLLATE utf8mb4_unicode_ci COMMENT '页面预览内容',
  `draft` text COLLATE utf8mb4_unicode_ci COMMENT '页面内容草稿',
  `page_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '页面类型',
  `is_default` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否默认模板',
  `priority` int(11) DEFAULT '0' COMMENT '排序优先级',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '页面状态. 1-草稿,2-预览,3-已发布',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_pickup_address`
-- ----------------------------
CREATE TABLE `ods_pickup_address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `shop_id` int(11) NOT NULL COMMENT '店铺ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '自提点名称',
  `province` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省',
  `city` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '市',
  `district` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区',
  `address` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `address_image` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '自提点照片',
  `pickup_time` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '自提时间',
  `time_slice_unit` tinyint(4) NOT NULL COMMENT '自提时间划分的单位',
  `need_book` tinyint(4) NOT NULL COMMENT '是否需要买家预约自提时间',
  `book_limit` int(11) DEFAULT NULL COMMENT '预约限制',
  `book_in_advance` tinyint(4) DEFAULT NULL COMMENT '提前预约(1:需要提前，0:无需提前)',
  `advance_time` int(11) DEFAULT NULL COMMENT '预约时间',
  `advance_time_unit` tinyint(4) DEFAULT NULL COMMENT '预约时间单位',
  `enabled` tinyint(4) NOT NULL COMMENT '自提点状态',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product`
-- ----------------------------
CREATE TABLE `ods_product` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `serial` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产品序列号',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品描述',
  `share_desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信分享描述',
  `detail` text COLLATE utf8mb4_unicode_ci COMMENT '商品详情',
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主图',
  `video` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频',
  `has_sku` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否多规格,0-否；1-是',
  `product_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1-实物商品；2-虚拟商品；3-电子卡券',
  `marking_price` decimal(20,2) DEFAULT NULL COMMENT '划线价',
  `weight` decimal(20,2) DEFAULT NULL COMMENT '重量',
  `shop_id` int(11) NOT NULL COMMENT '所属商家id',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1上架，2下架',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product_group`
-- ----------------------------
CREATE TABLE `ods_product_group` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) DEFAULT NULL COMMENT '预制时值为null',
  `name` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品分组名称, 最多30个字符',
  `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品分组简介',
  `detail` text COLLATE utf8mb4_unicode_ci COMMENT '商品分组描述(富文本)',
  `is_default` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否为系统预制分组，0-否；1-是',
  `sort_first` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '一级排序规则枚举, SortType',
  `sort_second` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '二级规则规则枚举, SortType',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product_re_group`
-- ----------------------------
CREATE TABLE `ods_product_re_group` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product_re_spec`
-- ----------------------------
CREATE TABLE `ods_product_re_spec` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `sku_id` int(11) NOT NULL,
  `spec_id` int(11) NOT NULL,
  `spec_value_id` int(11) NOT NULL,
  `spec_seq` int(11) NOT NULL DEFAULT '0' COMMENT '规格排序值 groupby 排序',
  `spec_value_seq` int(11) NOT NULL DEFAULT '0' COMMENT '规格值排序值 groupby 排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product_setting`
-- ----------------------------
CREATE TABLE `ods_product_setting` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `deduction_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1-拍下减库存；2-支付减库存',
  `on_shelf_time` timestamp NULL DEFAULT NULL COMMENT '上架时间',
  `on_shelf_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1-立即上架；2-自定义上架时间；3-暂不售卖',
  `priority` int(11) NOT NULL DEFAULT '1' COMMENT '权重',
  `shop_id` int(11) NOT NULL COMMENT '所属商家id',
  `allow_refund` bit(1) DEFAULT b'0' COMMENT '是否可申请退款,0-否；1-是',
  `ship_cost_type` tinyint(4) DEFAULT NULL COMMENT '1-统一邮费；2-运费模版',
  `ship_cost` decimal(20,2) DEFAULT NULL COMMENT '运费',
  `ship_cost_tpl_id` int(11) DEFAULT NULL COMMENT '运费模版id',
  `allow_exp_delivery` bit(1) DEFAULT b'0' COMMENT '快递发货id',
  `allow_pick_up` bit(1) DEFAULT b'0' COMMENT '上门自提id',
  `allow_local_delivery` bit(1) DEFAULT b'0' COMMENT '是否支持同城配送',
  `not_show_ivt` bit(1) DEFAULT b'0' COMMENT '详情页是否显示库存',
  `image_spec_id` int(11) DEFAULT NULL COMMENT '详情页规格图配置id',
  `show_voucher` bit(1) DEFAULT b'0' COMMENT '是否展示电子凭证',
  `sku_has_sort` bit(1) DEFAULT b'0' COMMENT '是否规格已经排序',
  `shelf_life` json DEFAULT NULL COMMENT '有效期设置',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product_sku`
-- ----------------------------
CREATE TABLE `ods_product_sku` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `default_sku` bit(1) NOT NULL DEFAULT b'0',
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `inventory` int(11) DEFAULT NULL COMMENT '库存',
  `warehouse_inventory` int(11) DEFAULT NULL COMMENT '线下库存',
  `sold_count` int(11) DEFAULT NULL COMMENT '销售量',
  `purchase_price` decimal(20,2) DEFAULT NULL COMMENT '成本价',
  `retail_price` decimal(20,2) DEFAULT NULL COMMENT '售价',
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '库存数据版本',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product_sku_extend`
-- ----------------------------
CREATE TABLE `ods_product_sku_extend` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `activity_inventory` int(4) DEFAULT NULL COMMENT '活动库存',
  `inventory` int(4) DEFAULT NULL COMMENT '库存',
  `warehouse_inventory` int(11) DEFAULT NULL COMMENT '线下库存',
  `sold_count` int(4) DEFAULT NULL COMMENT '销售量',
  `purchase_price` decimal(20,2) DEFAULT NULL COMMENT '成本价',
  `retail_price` decimal(20,2) DEFAULT NULL COMMENT '售价',
  `buy_limit` int(4) DEFAULT NULL COMMENT '限购数',
  `promotion_id` int(11) NOT NULL COMMENT '商品活动id ',
  `product_id` int(11) NOT NULL COMMENT '商品id',
  `product_sku_id` int(11) NOT NULL COMMENT '商品SKU id',
  `promotion_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '活动类型',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '库存数据版本',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product_spec`
-- ----------------------------
CREATE TABLE `ods_product_spec` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `seq` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_product_spec_value`
-- ----------------------------
CREATE TABLE `ods_product_spec_value` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL,
  `spec_id` int(11) NOT NULL,
  `spec_value` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `seq` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_refund`
-- ----------------------------
CREATE TABLE `ods_refund` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_id` int(11) NOT NULL COMMENT '店铺ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `status` tinyint(4) NOT NULL COMMENT '退款退货状态: 0-暂未处理，1-拒绝退款，等待买家处理，2-同意退货，等待买家退货，\n                                                      3-买家已退货，待确认收货，4-拒绝收货，等待买家处理，5-确认收货并退款，退款中，\n                                                      6-退款关闭，7-退款成功',
  `refund_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款单号',
  `transaction_refund_no` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款单号',
  `reject_reason` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家拒绝原因',
  `refund_type` tinyint(4) NOT NULL COMMENT '0-未发货仅退款，1-发货-仅退款，2-发货-退款退货',
  `freight_status` tinyint(4) NOT NULL COMMENT '1-未收到货，2-已收到货',
  `refund_reason` tinyint(4) NOT NULL COMMENT '退款原因',
  `refund_reason_desc` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款原因文本',
  `refund_desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款说明',
  `attachments` text COLLATE utf8mb4_unicode_ci COMMENT '附加图片',
  `refund_amount` decimal(7,2) NOT NULL COMMENT '退款金额',
  `closed_cause` enum('NONE','TIME_OUT','MEMBER_CANCEL','SHOP_CANCEL') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NONE' COMMENT '退款关闭原因',
  `refund_direction` enum('UNKOWN','WECHAT','ALIPAY','UNIONPAY','CCBPAY') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款去向',
  `transaction_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '退款是否成功',
  `failed_cause` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款失败原因',
  `failed_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款失败编码',
  `refund_operator` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '店家操作人',
  `refunded_at` timestamp NULL DEFAULT NULL COMMENT '退款时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_refund_item`
-- ----------------------------
CREATE TABLE `ods_refund_item` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `refund_id` int(11) NOT NULL COMMENT '退款ID',
  `order_item_id` bigint(20) NOT NULL COMMENT '订单项目ID',
  `amount` decimal(20,2) NOT NULL COMMENT '退款金额',
  `quantity` int(11) NOT NULL COMMENT '数量',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `sku_text` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规格',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `sku_id` int(11) NOT NULL COMMENT 'skuID',
  `image` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品图片',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_sdk_log_stream`
-- ----------------------------
CREATE TABLE `ods_sdk_log_stream` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` int(11) DEFAULT NULL,
  `userName` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deviceId` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shopId` int(11) DEFAULT NULL,
  `shopName` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mainShopId` int(11) DEFAULT NULL,
  `taroEnv` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `openId` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deviceModel` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `deviceBrand` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `systemName` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `systemVersion` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `appVersion` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `eventName` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `heartbeat` bigint(20) DEFAULT NULL,
  `url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `query` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `keyword` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `goodsId` int(11) DEFAULT NULL,
  `goodsName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `skuId` int(11) DEFAULT NULL,
  `couponTemplateId` int(11) DEFAULT NULL,
  `activityType` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activityId` int(11) DEFAULT NULL,
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_shop`
-- ----------------------------
CREATE TABLE `ods_shop` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `main_shop_id` int(11) DEFAULT '0' COMMENT '总部ID(自关联ID)',
  `group_id` int(11) DEFAULT '0' COMMENT '门店分组',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺名称',
  `abbr` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简称',
  `business_scope` tinyint(4) NOT NULL COMMENT '主营范围',
  `management_mode` tinyint(4) DEFAULT '0' COMMENT '经营模式(1: 电商, 2: 品牌商, 3: 单门店经营, 4: 多门店连锁, 5: 其他',
  `logo` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺logo',
  `wx_qr_code` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺公众号二维码',
  `country` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `province` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `district` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区县',
  `address` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `longitude` double DEFAULT NULL COMMENT '经度',
  `latitude` double DEFAULT NULL COMMENT '纬度',
  `phone1` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺电话一',
  `phone2` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺电话二',
  `open_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业开始时间',
  `close_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业结束时间',
  `status` tinyint(4) DEFAULT NULL COMMENT '营业状态(1: 营业中, 2: 休息中)',
  `access_status` tinyint(4) DEFAULT NULL COMMENT '访问状态: 1: 激活, 2: 未激活, 3: 过期, 4:暂停登录',
  `profile` varchar(1500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '店铺简介',
  `story` text COLLATE utf8mb4_unicode_ci COMMENT '品牌故事',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_shop_group`
-- ----------------------------
CREATE TABLE `ods_shop_group` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `main_shop_id` int(11) NOT NULL COMMENT '主店id',
  `group_name` varchar(50) NOT NULL COMMENT '分组名称',
  `group_mode` tinyint(4) NOT NULL COMMENT '分组模式',
  `support_shop_price` tinyint(4) NOT NULL DEFAULT '0' COMMENT '支持门店价',
  `support_cross_consume` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否支持跨店核销',
  `consume_pickup_product` tinyint(4) NOT NULL DEFAULT '0' COMMENT '核销自提商品',
  `consume_virtual_product` tinyint(4) NOT NULL DEFAULT '0' COMMENT '核销虚拟商品',
  `consume_electronic_card` tinyint(4) NOT NULL DEFAULT '0' COMMENT '核销电子卡券',
  `consume_coupon` tinyint(4) NOT NULL DEFAULT '0' COMMENT '核销优惠卷',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
--  Table structure for `ods_shop_member`
-- ----------------------------
CREATE TABLE `ods_shop_member` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `member_id` int(11) NOT NULL COMMENT 'member_base表主键',
  `shop_id` int(11) NOT NULL COMMENT '所属门店ID',
  `main_shop_id` int(11) NOT NULL COMMENT '连锁总店/单店ID',
  `payment_password` varchar(6) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '支付密码',
  `source` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '来源',
  `remark` varchar(700) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '备注',
  `disabled` tinyint(4) DEFAULT '0' COMMENT '0:活跃 1:已删除',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_social_account`
-- ----------------------------
CREATE TABLE `ods_social_account` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `shop_member_id` int(11) NOT NULL COMMENT 'shop_member表主键',
  `union_id` varchar(48) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '微信 unionid',
  `open_id` varchar(48) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '微信 openid',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
--  Table structure for `ods_user`
-- ----------------------------
CREATE TABLE `ods_user` (
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(11) NOT NULL,
  `country_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区号',
  `phone` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号码',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
