-- 拼团订单操作日志
DROP TABLE IF EXISTS `ods_group_order_opt_log`;
CREATE TABLE `ods_group_order_opt_log`
(
    `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    `id`         int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `created_at` datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` int(10) unsigned NOT NULL COMMENT '创建人id(系统操作为 0)',
    `shop_id`    int(10) unsigned NOT NULL COMMENT '店铺id',
    `action`     tinyint(4)       NOT NULL COMMENT '操作类型 1-拼团失败自动退款 2-自动退款失败 11-关闭未付款订单 12-同意退款 13-退款失败 14-拒绝退款',
    `order_id`   int(10) unsigned NOT NULL COMMENT '订单id',
    `order_no`   varchar(32)      NOT NULL COMMENT '订单号',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_shop_id` (`shop_id`) USING BTREE,
    KEY `idx_action` (`action`),
    KEY `idx_order_id` (`order_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='拼团订单操作日志';

-- 拼团分组
DROP TABLE IF EXISTS `ods_group_package`;
CREATE TABLE `ods_group_package`
(
    `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    `id`         int(10) unsigned    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `created_at` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
    `shop_id`    int(10) unsigned    NOT NULL COMMENT '店铺id',
    `name`       varchar(32)         NOT NULL COMMENT '分组名称',
    `order_type` varchar(16)         NOT NULL DEFAULT '1,2' COMMENT '排序条件 1-销量 2-排序号 3-发布时间 多个条件用英文逗号分割',
    `content`    text                NOT NULL COMMENT '页面内容',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_shop_id` (`shop_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='拼团分组';

-- 拼团活动
DROP TABLE IF EXISTS `ods_group_promotion`;
CREATE TABLE `ods_group_promotion`
(
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  `shop_id` int(10) unsigned NOT NULL COMMENT '店铺id',
  `product_id` int(10) unsigned NOT NULL COMMENT '商品id',
  `threshold` int(10) unsigned NOT NULL COMMENT '成团人数',
  `sort` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '排序号',
  `name` varchar(255) NOT NULL COMMENT '活动名称',
  `image` varchar(255) NOT NULL COMMENT '活动商品图片',
  `status` tinyint(4) NOT NULL COMMENT '活动状态 1-未开始 2-预热中 3-进行中 4-已结束',
  `actually_start_at` timestamp NULL DEFAULT NULL COMMENT '实际开始时间(含预热时间)',
  `actually_end_at` timestamp NULL DEFAULT NULL COMMENT '实际结束时间',
  `order_expires_in` int(10) unsigned NOT NULL COMMENT '订单超时时间(分钟)',
  `start_at` timestamp NULL DEFAULT NULL COMMENT '开始时间',
  `end_at` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `preheat_hour` int(10) unsigned DEFAULT NULL COMMENT '预热时间(小时) (为NULL表示没有开启预热)',
  `notify_minute` int(10) unsigned DEFAULT NULL COMMENT '开团提醒时间(分钟)',
  `expires_in` int(10) unsigned NOT NULL DEFAULT '24' COMMENT '开团有效期(小时)',
  `buy_limit` int(10) unsigned DEFAULT NULL COMMENT '限购数(为NULL表示没有开启)',
  `customer_limit` int(10) unsigned DEFAULT NULL COMMENT '至少包含的新客数(为NULL表示没有开启)',
  `customer_tip` varchar(32) NOT NULL COMMENT '老客参团提醒',
  `enable_auto_created` tinyint(4) NOT NULL COMMENT '是否开启模拟成团 0-否 1-是',
  `auto_created_in` int(10) unsigned DEFAULT NULL COMMENT '模拟成团时间(小时)',
  `auto_created_limit` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '模拟成团需要的新客数',
  `display_group_list` tinyint(4) unsigned NOT NULL DEFAULT '1' COMMENT '是否显示参团 0-否 1-是',
  `group_robot` int(10) unsigned DEFAULT NULL COMMENT '机器人开团数(为NULL表示没有开启)',
  `enabled_refund` tinyint(4) NOT NULL COMMENT '是否允许成团前退款 0-否 1-是',
  `template_id` int(10) unsigned NOT NULL COMMENT '详情页模板id',
  `member_tag` json NOT NULL COMMENT '客户标签id列表 [标签id]',
  `marking_price` decimal(10,2) DEFAULT NULL COMMENT '划线价',
  `min_retail_price` decimal(10,2) NOT NULL COMMENT '最低拼团价',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE,
  KEY `idx_product_id` (`product_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='拼团活动';


-- 客户活动预约记录
DROP TABLE IF EXISTS `ods_group_promotion_appointment`;
CREATE TABLE `ods_group_promotion_appointment`
(
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  `shop_id` int(10) unsigned NOT NULL COMMENT '店铺id',
  `group_promotion_id` int(10) unsigned NOT NULL COMMENT '拼团活动id',
  `member_id` int(10) unsigned NOT NULL COMMENT '客户id',
  `member_name` varchar(32) NOT NULL COMMENT '客户名称',
  `member_avatar` varchar(300) NOT NULL COMMENT '客户头像',
  `phone` varchar(32) NOT NULL COMMENT '手机号',
  `appoint_at` timestamp NULL DEFAULT NULL COMMENT '预约时间',
  `is_remind` tinyint(4) NOT NULL DEFAULT '0' COMMENT '1-已提醒',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE,
  KEY `idx_group_promotion_id` (`group_promotion_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='客户活动预约记录';


-- 拼团活动开团记录
DROP TABLE IF EXISTS `ods_group_promotion_instance`;
CREATE TABLE `ods_group_promotion_instance`
(
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  `shop_id` int(10) unsigned NOT NULL COMMENT '店铺id',
  `group_promotion_id` int(10) unsigned NOT NULL COMMENT '拼团活动id',
  `status` tinyint(4) NOT NULL COMMENT '拼团状态 1-待成团 2-拼团成功 3-拼团失败',
  `has_robot` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否机器人成团 0-否 1-是',
  `group_price` decimal(10,2) NOT NULL COMMENT '拼团价',
  `marking_price` decimal(10,2) DEFAULT NULL COMMENT '划线价',
  `threshold` int(10) unsigned NOT NULL COMMENT '成团人数',
  `expires_in` int(10) unsigned NOT NULL COMMENT '开团有效期(小时)',
  `expires_at` timestamp NULL DEFAULT NULL COMMENT '开团有效期',
  `auto_created_in` int(10) unsigned DEFAULT NULL COMMENT '模拟成团时间(小时)(为NULL表示没有开启)',
  `auto_created_limit` int(10) unsigned DEFAULT NULL COMMENT '模拟成团需要的新客数 (为NULL表示没有开启)',
  `group_robot` int(10) unsigned DEFAULT NULL COMMENT '机器人开团数(为NULL表示没有开启)',
  `enabled_refund` tinyint(4) NOT NULL COMMENT '是否允许成团前退款 0-否 1-是',
  `customer_limit` int(10) unsigned DEFAULT NULL COMMENT '至少包含的新客数(为NULL表示没有开启)',
  `order_expires_in` int(10) unsigned NOT NULL COMMENT '订单超时时间(分钟)',
  `operator` varchar(60) DEFAULT '0' COMMENT '操作人',
  `operator_user_id` int(11) DEFAULT '0' COMMENT '操作人Id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_group_promotion_id` (`group_promotion_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='拼团活动开团记录';


-- 拼团活动参与人
DROP TABLE IF EXISTS `ods_group_promotion_instance_member`;
CREATE TABLE `ods_group_promotion_instance_member`
(
  `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  `shop_id` int(10) unsigned NOT NULL COMMENT '店铺id',
  `group_promotion_instance_id` int(10) unsigned NOT NULL COMMENT '拼团活动记录id',
  `member_id` int(10) unsigned NOT NULL COMMENT '会员id(参与人id) 机器人统一为0',
  `member_photo` varchar(255) NOT NULL COMMENT '参与人头像',
  `member_name` varchar(32) NOT NULL COMMENT '参与人名字/手机号',
  `is_reserved` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否预约 0-否 1-是',
  `is_leader` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否为团长 0-否 1-是',
  `is_paid` tinyint(4) DEFAULT NULL COMMENT '是否已付款 0-否 1-是 机器人统一为1',
  `is_refund_failure` tinyint(4) DEFAULT '0' COMMENT '是否退款失败 0-否 1-是',
  `is_joind_failure` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否参团失败 0-否 1-是',
  `share_member_id` int(10) unsigned DEFAULT NULL COMMENT '分享人id(会员id)',
  `share_employee_id` int(10) unsigned DEFAULT NULL COMMENT '原始分享人id(员工id)',
  `order_id` int(10) unsigned NOT NULL COMMENT '订单id 机器人统一为0',
  `order_no` varchar(32) NOT NULL COMMENT '订单号 机器人统一为0',
  `order_status` tinyint(4) DEFAULT NULL COMMENT '订单状态,0待付款/未付款,1未发货/待发货,2已发货/待收货,3交易完成/待评价,4交易完成/已评价,\n                                                        5交易关闭/客户主动关闭,51交易关闭/商家主动关闭,52交易关闭/超时关闭,53交易关闭/支付失败,54退款完成',
  `refund_status` tinyint(4) DEFAULT NULL,
  `unionNo` varchar(32) DEFAULT NULL,
  `group_promotion_id` int(10) unsigned NOT NULL COMMENT '拼团活动id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shop_id` (`shop_id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_order_no` (`order_no`) USING BTREE,
  KEY `idx_group_promotion_instance_id` (`group_promotion_instance_id`) USING BTREE,
  KEY `group_promotion_id` (`group_promotion_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='拼团活动参与人';


-- 拼团分组配置关联记录
DROP TABLE IF EXISTS `ods_group_promotion_package`;
CREATE TABLE `ods_group_promotion_package`
(
    `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    `id`                 int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `group_package_id`   int(10) unsigned NOT NULL COMMENT '拼团分组id',
    `group_promotion_id` int(10) unsigned NOT NULL COMMENT '拼团活动id',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_group_package_id` (`group_package_id`) USING BTREE,
    KEY `idx_group_promotion_id` (`group_promotion_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='拼团分组配置关联记录';


-- 拼团一期结果表
create TABLE `fact_group_buy_v1`
(
    `promotion_id`      int(11)        DEFAULT 0 COMMENT '拼团活动id',
    `pv`                int(11)        DEFAULT 0 COMMENT '浏览次数',
    `uv`                int(11)        DEFAULT 0 COMMENT '浏览人数',
    `share_count`       int(11)        DEFAULT 0 COMMENT '分享次数',
    `share_user_count`  int(11)        DEFAULT 0 COMMENT '分享人数',
    `order_amount`      decimal(20, 2) DEFAULT '0.0' COMMENT '交易总额',
    `order_count`       int(11)        DEFAULT 0 COMMENT '订单数',
    `order_user_count`  int(11)        DEFAULT 0 COMMENT '下单人数',
    `refund_amount`     decimal(20, 2) DEFAULT '0.0' COMMENT '退款总额',
    `refund_count`      int(11)        DEFAULT 0 COMMENT '退款单数',
    `refund_user_count` int(11)        DEFAULT 0 COMMENT '退款人数',
    `grouped_count`     int(11)        DEFAULT 0 COMMENT '成团数',
    `grouping_count`    int(11)        DEFAULT 0 COMMENT '待成团数',
    `inventory`         int(11)        DEFAULT 0 COMMENT '剩余库存数',
    PRIMARY KEY (`promotion_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 增加索引
ALTER TABLE ods_group_promotion_instance ADD KEY `ods_group_promotion_instance_group_promotion_id_index` (`group_promotion_id`);
ALTER TABLE ods_order_consignee ADD KEY `order_id_index` (`order_id`);

-- 拼团交易统计表
CREATE TABLE `fact_order_group_buy_v1` (
  `group_promotion_id` int(11) NOT NULL DEFAULT '0' COMMENT '拼团groupId',
  `order_amount` decimal(20,2) DEFAULT '0.00' COMMENT '交易总额',
  `order_count` int(11) DEFAULT '0' COMMENT '订单数',
  `order_user_count` int(11) DEFAULT '0' COMMENT '下单人数',
  `refund_amount` decimal(20,2) DEFAULT '0.00' COMMENT '退款总额',
  `refund_count` int(11) DEFAULT '0' COMMENT '退款单数',
  `refund_user_count` int(11) DEFAULT '0' COMMENT '退款人数',
  PRIMARY KEY (`group_promotion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT ='拼团交易统计表';


ALTER TABLE `ods_group_promotion_instance`
    MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '拼团状态 0-开团未支付 1-待成团 2-满员成团 3-拼团失败 4-机器人成团 5-手动成团' AFTER `group_promotion_id`;

-- 活动订单字段长度修改
ALTER TABLE `ods_order_promotion` MODIFY COLUMN `promotion_id` int(11) NOT NULL COMMENT '活动ID/拼团活动的成团ID' AFTER `promotion_type`,MODIFY COLUMN `promotion_name` varchar(255)  NOT NULL COMMENT '活动名称' AFTER `promotion_id`;


-- 员工统计表
CREATE TABLE `fact_group_buy_staff_v1`
(
    `promotion_id`   int          NOT NULL DEFAULT 0 COMMENT '活动id',
    `staff_id`       int          NOT NULL DEFAULT 0 COMMENT '员工id',
    `name`           varchar(255) NOT NULL COMMENT '受益人姓名',
    `phone`          varchar(20)  NOT NULL COMMENT '受益人手机号',
    `pay_amount`     decimal(20, 2)        DEFAULT '0.0' COMMENT '订单总金额',
    `order_number`   int                   DEFAULT 0 COMMENT '订单数',
    `refund_number`  int                   DEFAULT 0 COMMENT '退款订单数',
    `refund_amount`  decimal(20, 2)        DEFAULT '0.0' COMMENT '退款订单金额',
    `new_user_count` int                   DEFAULT 0 COMMENT '拓新客数',
    PRIMARY KEY (promotion_id,`staff_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '拼团活动员工统计表';


-- 拼团活动订单统计表
CREATE TABLE `fact_group_buy_order_v1`
(
    `promotion_id`              int NOT NULL   DEFAULT 0 COMMENT '活动id',
    `pay_amount`                decimal(20, 2) DEFAULT '0.0' COMMENT '订单总金额',
    `order_number`              int            DEFAULT 0 COMMENT '订单数',
    `refund_number`             int            DEFAULT 0 COMMENT '退款订单数',
    `refund_amount`             decimal(20, 2) DEFAULT '0.0' COMMENT '退款订单金额',
    `new_user_count`            int            DEFAULT 0 COMMENT '拓新客数',
    `full_grouped_count`        int            DEFAULT 0 COMMENT '满团数',
    `grouped_count`             int            DEFAULT 0 COMMENT '成团数，包括手动和机器人成团',
    `grouped_failed_count`      int            DEFAULT 0 COMMENT '成团失败数',
    `inventory`                 int            DEFAULT 0 COMMENT '剩余库存数',
    PRIMARY KEY (`promotion_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '拼团活动订单统计表';

-- 拼团看板表
CREATE TABLE `ods_gb_dashboard`
(
    `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    `id`                    int       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '看板id',
    `shop_id`               int                NOT NULL COMMENT '店铺id',
    `name`                  varchar(32)        NOT NULL COMMENT '看板名称',
    `logo_address`          varchar(200)       DEFAULT "" COMMENT '看板logo图片地址',
    `theme_id`              varchar(32)        DEFAULT ""    COMMENT '看板主题，由前端提供主题的ID',
    `count_staff_order_in`   tinyint           DEFAULT 0    COMMENT '员工参团规则：0：员工参团不算满图案 1：员工参团算满团',
    `created_at`            timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `dashboard_shop_id_idx` (`shop_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '看板表';

-- 拼团员工分组表
CREATE TABLE `ods_gb_staff_group`
(
    `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    `id`             int       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '分组id',
    `dashboard_id`   int       NOT NULL COMMENT '看板id',
    `name`           varchar(32)        DEFAULT "" COMMENT '分组名称',
    `created_at`     timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `dashboard_id` (`dashboard_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '看板表';


-- 看板员工分组和员工关联表
CREATE TABLE IF NOT EXISTS `ods_gb_staff_group_relation`
(
    `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    `id`              INT    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `dashboard_id`    INT              NOT NULL COMMENT '看板id',
    `staff_group_id`  INT              NOT NULL COMMENT '员工分组id',
    `staff_id`        INT                 NOT NULL COMMENT '员工id',
    `created_at`      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT staff_group_id_staff_id_idx UNIQUE (`staff_group_id`, `staff_id`),
    KEY `gb_staff_group_relation_staff_id_idx` (`staff_id`),
    KEY `gb_staff_group_relation_dashboard_id_idx` (`dashboard_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '看板员工分组和员工关联表';

-- 看板与活动关联表
CREATE TABLE IF NOT EXISTS `ods_gb_dashboard_promotion`
(
    `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    `id`           INT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `dashboard_id` INT       NOT NULL COMMENT '表 gb_dashboard id',
    `promotion_id` INT       NOT NULL COMMENT '表group_promotion的id',
    `created_at`   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT dashboard_id_promotion_id_idx UNIQUE (`dashboard_id`, `promotion_id`),
    INDEX `gb_dashboard_promotion_promotion_id_idx` (`promotion_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '看板与活动关联表';

ALTER TABLE ods_group_promotion_instance_member ADD is_new_member TINYINT DEFAULT 0 COMMENT '是否是新客户';
ALTER TABLE ods_group_promotion_instance_member ADD actual_order_amount decimal(20,2) DEFAULT 0 COMMENT '订单实付金额';
ALTER TABLE ods_group_promotion_instance_member ADD staff_nickname varchar(60) DEFAULT '' COMMENT '受益人（员工)的昵称';
ALTER TABLE ods_group_promotion_instance_member ADD staff_phone varchar(14) DEFAULT '' COMMENT '受益人（员工)的手机号';
ALTER TABLE ods_group_promotion_instance_member ADD staff_id INT DEFAULT 0 COMMENT '受益人（员工)的ID';
ALTER TABLE ods_group_promotion_instance_member ADD member_phone varchar(15) NOT NULL DEFAULT '' COMMENT '参与人手机号';

ALTER TABLE ods_group_promotion_instance_member ADD INDEX group_promotion_instance_member_staff_id (`staff_id`);


ALTER TABLE `ods_refund` MODIFY COLUMN `refund_amount` decimal(20, 2) NOT NULL COMMENT '退款金额' AFTER `attachments`;


-- shop_member增加staff_id字段
ALTER TABLE `ods_shop_member` ADD `staff_id` int(11) DEFAULT '0' COMMENT '员工id' AFTER `source`;

-- ods_log_stream 增加索引
ALTER  TABLE  `ods_log_stream` ADD KEY `idx_shopid_eventname` (`shop_id`,`event_name`);

-- 增加字段
ALTER TABLE ods_group_promotion_instance ADD has_refund tinyint(4) DEFAULT '0' COMMENT '是否存在退款成功的订单 0-否 1-是';

-- ods_group_promotion_instance 增加索引
ALTER  TABLE  `ods_group_promotion_instance` ADD KEY `idx_created` (`created_at`);

-- 订单受益人表
CREATE TABLE IF NOT EXISTS `ods_order_beneficiary`
(
    `id`               int(11)                                NOT NULL AUTO_INCREMENT,
    `shop_id`          int(11)                                NOT NULL DEFAULT '0' COMMENT '门店id',
    `order_no`         varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '商城订单号',
    `beneficiary_id`   int(11)                                NOT NULL DEFAULT '0' COMMENT '受益人的id',
    `beneficiary_type` tinyint(4)                             NOT NULL DEFAULT '0' COMMENT '受益人的类型，默认0；1表示店铺员工；2表示C端商城用户',
    `is_deleted`       tinyint(1)                             NOT NULL DEFAULT '0' COMMENT '1 表示删除，0 表示未删除',
    `created_at`       timestamp                              NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       timestamp                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `order_no_idx` (`order_no`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='订单和受益人的关联表';


-- 添加员工表
CREATE TABLE `ods_staff`
(
    `__deleted`        bit(1)               DEFAULT b'0' COMMENT '原表数据删除标志',
    `id`               int(11)     NOT NULL AUTO_INCREMENT,
    `account_id`       int(11)     NOT NULL COMMENT '账号id',
    `shop_id`          int(11)     NOT NULL COMMENT '门店id',
    `role_id`          int(11)     NOT NULL COMMENT '角色id',
    `region_code`      int(11)     NOT NULL DEFAULT '86' COMMENT '区号',
    `phone`            varchar(11) NOT NULL COMMENT '手机号码（联系方式）',
    `staff_no`         varchar(64)          DEFAULT NULL COMMENT '工号',
    `manager_flag`     tinyint(4)  NOT NULL COMMENT '管理者标志，0非管理者，1门店创建者，2授权管理者',
    `created_at`       timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `name`             varchar(60)          DEFAULT '' COMMENT '员工昵称',
    `real_name`        varchar(60)          DEFAULT '' COMMENT '真实名称',
    `post_name`        varchar(60)          DEFAULT '' COMMENT '岗位名称',
    `post_id`          int(11)              DEFAULT NULL COMMENT '岗位Id',
    `role_name`        varchar(11)          DEFAULT '' COMMENT '角色名称',
    `status`           tinyint(1)           DEFAULT '1' COMMENT '状态: 0未知,1启用,1停用',
    `operator`         varchar(60)          DEFAULT '0' COMMENT '操作人',
    `operator_user_id` int(11)              DEFAULT '0' COMMENT '操作人Id',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 545
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ods_staff 增加索引
ALTER  TABLE  `ods_staff` ADD KEY `shop_status` (`shop_id`,`status`);