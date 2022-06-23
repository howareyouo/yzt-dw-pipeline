-- ods 原始数据库同步。

-- yzt_member_db 库
-- 会员聚合
CREATE TABLE `ods_member_union`
(
    `id`                     INT       NOT NULL PRIMARY KEY COMMENT 'shop_member id',
    `user_id`                INT                DEFAULT NULL COMMENT 'user id',
    `country_code`           VARCHAR(10)        DEFAULT NULL COMMENT '区号',
    `phone`                  VARCHAR(15)        DEFAULT NULL COMMENT '手机号码',
    `member_id`              INT                DEFAULT NULL COMMENT 'member_base id',
    `name`                   VARCHAR(32)        DEFAULT NULL COMMENT '姓名',
    `nickname`               VARCHAR(32)        DEFAULT NULL COMMENT '昵称',
    `avatar`                 VARCHAR(300)       DEFAULT NULL COMMENT '头像',
    `gender`                 TINYINT            DEFAULT '0' COMMENT '性别 0: 未知 1: 男 2: 女',
    `wechat`                 VARCHAR(100)       DEFAULT NULL COMMENT '微信号',
    `birthday`               DATE               DEFAULT NULL COMMENT '生日',
    `province`               VARCHAR(300)       DEFAULT NULL,
    `city`                   VARCHAR(300)       DEFAULT NULL,
    `main_shop_id`           INT                DEFAULT '0' COMMENT '总店id',
    `shop_id`                INT                DEFAULT '0' COMMENT '门店id',
    `staff_id`               INT NULL COMMENT '员工id',
    `payment_password`       VARCHAR(6)         DEFAULT '' COMMENT '支付密码',
    `source`                 VARCHAR(10)        DEFAULT '' COMMENT '来源',
    `remark`                 VARCHAR(700)       DEFAULT '' COMMENT '备注',
    `disabled`               TINYINT            DEFAULT '0' COMMENT '0:活跃 1:已删除',
    `balance_amount`         DECIMAL(20, 2)     DEFAULT NULL COMMENT '储值余额',
    `gift_amount`            DECIMAL(20, 2)     DEFAULT NULL COMMENT '赠金余额',
    `points`                 DECIMAL(20, 2)     DEFAULT NULL COMMENT '积分',
    `debt_amount`            DECIMAL(20, 2)     DEFAULT NULL COMMENT '欠款',
    `consume_times`          INT                DEFAULT NULL COMMENT '消费次数',
    `consume_amount`         DECIMAL(20, 2)     DEFAULT NULL COMMENT '消费金额',
    `last_consume_date`      DATETIME           DEFAULT NULL COMMENT '上次消费时间',
    `last_consume_services`  TEXT COMMENT '上次消费服务',
    `last_service_staff`     VARCHAR(60)        DEFAULT NULL COMMENT '上次服务员工',
    `last_view_shop_time`    TIMESTAMP NULL DEFAULT NULL COMMENT '最近浏览店铺时间',
    `last_consignee_address` VARCHAR(200)       DEFAULT NULL COMMENT '最近收货地址',

    `referrer`               VARCHAR(60)        DEFAULT NULL COMMENT '介绍人',
    `follower`               VARCHAR(60)        DEFAULT NULL COMMENT '跟踪员工',
    `custom1`                VARCHAR(200)       DEFAULT NULL COMMENT '自定义字段1',
    `custom2`                VARCHAR(200)       DEFAULT NULL COMMENT '自定义字段2',
    `custom3`                VARCHAR(200)       DEFAULT NULL COMMENT '自定义字段3',
    `custom4`                VARCHAR(200)       DEFAULT NULL COMMENT '自定义字段4',
    `custom5`                VARCHAR(200)       DEFAULT NULL COMMENT '自定义字段5',
    `custom6`                VARCHAR(200)       DEFAULT NULL COMMENT '自定义字段6',
    `created_at`             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '会员union';


-- 会员标签定义
CREATE TABLE `ods_tag_definition`
(
    `id`          INT         NOT NULL PRIMARY KEY COMMENT '唯一标志，',
    `shop_id`     INT         NOT NULL COMMENT '门店id',
    `pid`         INT    DEFAULT NULL COMMENT '上级标签id',
    `type`        TINYINT     NOT NULL COMMENT '标签类型，1手动标签，2智能标签',
    `name`        VARCHAR(20) NOT NULL COMMENT '标签名称',
    `description` VARCHAR(20) NOT NULL COMMENT '标签描述',
    `category`    TINYINT     NOT NULL COMMENT '标签类别，1自定义标签，2行为标签，3属性标签',
    `filters`     TEXT COMMENT '标签规则定义 JSONArray',
    `version`     INT    DEFAULT '0' COMMENT '标签版本',
    `__deleted`   BIT(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    UNIQUE KEY `tag_definition_pk` (`shop_id`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '会员标签定义';


-- 会员标签
CREATE TABLE `ods_member_tag`
(
    `id`        INT NOT NULL PRIMARY KEY COMMENT '唯一标志，',
    `member_id` INT NOT NULL COMMENT '会员id',
    `tag_id`    INT NOT NULL COMMENT '标签id',
    `shop_id`   INT NULL COMMENT '店铺编号',
    `__deleted` BIT(1) DEFAULT b'0' COMMENT '原表数据删除标志',
    UNIQUE KEY `member_tag_pk` (`member_id`, `tag_id`),
    KEY         `member_tag_tag_definition_id_fk` (`tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '会员标签关联';

-- 群组定义
CREATE TABLE `ods_member_group`
(
    `id`          INT         NOT NULL PRIMARY KEY COMMENT '唯一标志，',
    `shop_id`     INT         NOT NULL COMMENT '门店id',
    `category`    TINYINT              DEFAULT '0' COMMENT '群组类型. 0:自定义, 1:重要客户, 2:意向客户',
    `name`        VARCHAR(10) NOT NULL COMMENT '分组名称',
    `description` VARCHAR(30) NOT NULL COMMENT '分组描述',
    `filters`     TEXT COMMENT '群组规则定义 JSONArray',
    `version`     INT                  DEFAULT '0' COMMENT '群组版本',
    `created_at`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `__deleted`   BIT(1)               DEFAULT b'0' COMMENT '原表数据删除标志',
    KEY           `member_group_shop_id_idx` (`shop_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '会员群组定义';


-- yzt_wxmp_db 库 微信用户
CREATE TABLE `ods_wx_user`
(
    `id`              INT          NOT NULL PRIMARY KEY,
    `subscribed`      TINYINT      NOT NULL DEFAULT '0' COMMENT '是否关注. 1是, 0否',
    `appid`           VARCHAR(64)  NOT NULL COMMENT '微信 appid',
    `openid`          VARCHAR(64)  NOT NULL COMMENT '微信用户的openid',
    `unionid`         VARCHAR(64)           DEFAULT NULL COMMENT '微信用户的unionid',
    `nickname`        VARCHAR(64)  NOT NULL COMMENT '微信用户的昵称',
    `sex`             TINYINT      NOT NULL DEFAULT '0' COMMENT '性别. 0未知,1男性,2女性',
    `headimgurl`      VARCHAR(256) NOT NULL COMMENT '用户头像',
    `city`            VARCHAR(64)           DEFAULT NULL COMMENT '城市',
    `province`        VARCHAR(64)           DEFAULT NULL COMMENT '省份',
    `country`         VARCHAR(64)           DEFAULT NULL COMMENT '国家',
    `language`        VARCHAR(64)           DEFAULT NULL COMMENT '语言 eg. zh_CN',
    `subscribe_time`  BIGINT(20) DEFAULT NULL COMMENT '关注时间',
    `subscribe_scene` VARCHAR(64)           DEFAULT NULL COMMENT '关注的渠道来源',
    `tagid_list`      VARCHAR(256) NOT NULL DEFAULT '' COMMENT '用户被打上的标签ID列表',
    `blacked`         TINYINT               DEFAULT '0' COMMENT '是否被加入黑名单. 1是, 0否',
    `remark`          VARCHAR(30)           DEFAULT NULL COMMENT '粉丝备注',
    `created_at`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `user_openid_idx` (`openid`),
    KEY               `user_appid_idx` (`appid`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '微信用户';


-- yzt_wxauth_db 库 微信绑定关系

CREATE TABLE `ods_wx_relation`
(
    `id`           INT         NOT NULL PRIMARY KEY,
    `main_shop_id` INT                  DEFAULT '0' COMMENT '主店id',
    `shop_id`      INT         NOT NULL COMMENT '店铺 id',
    `appid`        VARCHAR(32) NOT NULL COMMENT '微信 appid',
    `apptype`      TINYINT     NOT NULL DEFAULT '1' COMMENT '应用类型, 1:公众号 2:小程序',
    `relation`     TINYINT     NOT NULL DEFAULT '0' COMMENT '绑定类型, 0:授权  1:绑定',
    `qrcode_url`   VARCHAR(256)         DEFAULT NULL COMMENT '小程序二维码(sence值中带shop_id)',
    `created_at`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY            `wx_relation_main_shop_id_idx` (`main_shop_id`) USING BTREE,
    KEY            `wx_relation_shop_id_idx` (`shop_id`) USING BTREE,
    KEY            `wx_relation_appid_idx` (`appid`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '微信绑定关系';


CREATE TABLE `mg_fact_member_order_daily_v1`
(
    `analysis_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    `shop_id`       INT       NOT NULL COMMENT '店铺ID',
    `member_id`     INT       NOT NULL COMMENT '会员Id',
    `order_count`   INT NULL COMMENT '订单数',
    `order_amount`  DECIMAL(20, 2) NULL COMMENT '订单总额',
    `paid_count`    INT NULL COMMENT '支付数',
    `paid_amount`   DECIMAL(20, 2) NULL COMMENT '支付总额',
    `refund_count`  INT NULL COMMENT '退款数',
    `refund_amount` DECIMAL(20, 2) NULL COMMENT '退款总额',
    `order_ids`     TEXT NULL COMMENT '订单id',
    `refund_ids`    TEXT NULL COMMENT '退款订单id',
    PRIMARY KEY (`analysis_date`, `shop_id`, `member_id`),
    KEY             `fact_member_order_shop_id_idx` (`shop_id`),
    KEY             `fact_member_order_member_id_idx` (`member_id`),
    KEY             `fact_member_order_analysis_date_idx` (`analysis_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '会员订单每日分析';

CREATE TABLE `mg_fact_member_coupon_daily_v1`
(
    `analysis_date`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    `shop_id`            INT       NOT NULL COMMENT '店铺ID',
    `member_id`          INT       NOT NULL COMMENT '会员Id',
    `coupon_template_id` INT       NOT NULL COMMENT '优惠券模板id',
    `coupon_type`        TINYINT(4) NULL COMMENT '优惠券类型',
    `used_times`         INT       NOT NULL DEFAULT 0 COMMENT '核销次数',
    `share_times`        INT       NOT NULL DEFAULT 0 COMMENT '分享次数',
    `apply_times`        INT       NOT NULL DEFAULT 0 COMMENT '领取次数',
    `expired_times`      INT       NOT NULL DEFAULT 0 COMMENT '失效过期次数',
    `deprecated_times`   INT       NOT NULL DEFAULT 0 COMMENT '作废次数',
    PRIMARY KEY (`analysis_date`, `shop_id`, `member_id`, `coupon_template_id`),
    KEY                  `fact_member_coupon_shop_id_idx` (`shop_id`),
    KEY                  `fact_member_coupon_member_id_idx` (`member_id`),
    KEY                  `fact_member_coupon_coupon_template_id_idx` (`coupon_template_id`),
    KEY                  `fact_member_coupon_analysis_date_idx` (`analysis_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '优惠券模板每日分析';


CREATE TABLE `mg_fact_member_product_daily_v1`
(
    `analysis_date`   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    `shop_id`         INT       NOT NULL COMMENT '店铺ID',
    `member_id`       INT       NOT NULL COMMENT '会员id',
    `product_id`      INT       NOT NULL COMMENT '商品id',
--    `product_name`        VARCHAR(255)  NULL,
--    `product_serial`      VARCHAR(255)          DEFAULT NULL COMMENT '产品序列号',
--    `product_description` VARCHAR(255)          DEFAULT NULL COMMENT '商品描述',
--    `product_share_desc`  VARCHAR(255)          DEFAULT NULL COMMENT '微信分享描述',
    `viewed_times`    INT       NOT NULL DEFAULT 0 COMMENT '商品浏览数',
    `share_times`     INT       NOT NULL DEFAULT 0 COMMENT '分享次数',
    `order_times`     INT       NOT NULL DEFAULT 0 COMMENT '下单次数',
    `order_amount`    DECIMAL(12, 2)     DEFAULT 0 COMMENT '下单金额',
    `order_quantity`  INT       NOT NULL DEFAULT 0 COMMENT '购买件数',
    `paid_times`      INT       NOT NULL DEFAULT 0 COMMENT '付款订单数',
    `paid_amount`     DECIMAL(12, 2)     DEFAULT 0 COMMENT '付款金额',
    `refund_times`    INT       NOT NULL DEFAULT 0 COMMENT '退款次数',
    `refund_quantity` INT       NOT NULL DEFAULT 0 COMMENT '退款数量',
    `refund_amount`   DECIMAL(12, 2)     DEFAULT 0 COMMENT '退款金额',
    `cart_add_date`   TIMESTAMP NULL DEFAULT NULL COMMENT '加入购物车时间',
    `cart_add_times`  INT                DEFAULT NULL COMMENT '加入购物车次数',
    `group_ids`       VARCHAR(300)       DEFAULT '' COMMENT '商品分组id数组',
    PRIMARY KEY (`analysis_date`, `shop_id`, `member_id`, `product_id`),
    KEY               `fact_member_product_shop_id_idx` (`shop_id`),
    KEY               `fact_member_product_member_id_idx` (`member_id`),
    KEY               `fact_member_product_product_id_idx` (`product_id`),
    KEY               `fact_member_product_analysis_date_idx` (`analysis_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '会员商品每日分析';


CREATE TABLE `mg_fact_member_shop_visits_v1`
(
    `uuid`            VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'UUID 更新标识',
    `shop_id`         INT                  DEFAULT NULL COMMENT '店铺ID',
    `member_id`       INT         NOT NULL COMMENT '会员编号',
    `device_id`       VARCHAR(200)         DEFAULT NULL COMMENT '唯一id 用户的设备记录',
    `visits_start`    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问起始',
    `visits_end`      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问结束',
    `visits_duration` INT                  DEFAULT 0 COMMENT '访问时长(秒)',
    `created_at`      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
    KEY               `fact_member_shop_shop_id_idx` (`shop_id`),
    KEY               `fact_member_shop_member_id_idx` (`member_id`),
    KEY               `fact_member_shop_analysis_date_idx` (`created_at`),
    KEY               `fact_member_shop_device_idx` (`device_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT '会员店铺访问记录';

--  会员基础信息维度信息表
CREATE TABLE `mg_fact_member_union_v1`
(
    `id`                     INT NOT NULL COMMENT '会员id' PRIMARY KEY,
    `main_shop_id`           INT NULL COMMENT '主店id',
    `shop_id`                INT NOT NULL COMMENT '店铺id',

    -- user
    `country_code`           VARCHAR(10) NULL COMMENT '区号',
    `phone`                  VARCHAR(15) NULL COMMENT '手机号码',

    -- member_base
    `member_id`              INT            DEFAULT NULL COMMENT 'member_base id',
    `user_id`                INT NULL COMMENT 'user表id',
    `name`                   VARCHAR(32) NULL COMMENT '姓名',
    `nickname`               VARCHAR(32)    DEFAULT NULL COMMENT '昵称',
    `avatar`                 VARCHAR(300)   DEFAULT NULL COMMENT '头像',
    `gender`                 TINYINT        DEFAULT 0 COMMENT '性别 0: 未知 1: 男 2: 女',
    `wechat`                 VARCHAR(100)   DEFAULT NULL COMMENT '微信号',
    `birthday`               DATE           DEFAULT NULL COMMENT '生日',
    `province`               VARCHAR(300)   DEFAULT NULL COMMENT '省份',
    `city`                   VARCHAR(100)   DEFAULT NULL COMMENT '城市',

    -- shop_member
    `staff_id`               INT NULL COMMENT '员工id',
    `payment_password`       VARCHAR(20)    DEFAULT '' COMMENT '支付密码',
    `source`                 VARCHAR(20)    DEFAULT '' COMMENT '来源',
    `remark`                 VARCHAR(700)   DEFAULT '' COMMENT '备注',
    `disabled`               TINYINT        DEFAULT 0 COMMENT '0:活跃 1:已删除',
    `created_at`             TIMESTAMP NULL DEFAULT NULL COMMENT '建档时间',
    `updated_at`             TIMESTAMP NULL DEFAULT NULL COMMENT '更新时间',

    -- member_extend
    `balance_amount`         DECIMAL(20, 2) DEFAULT 0 COMMENT '储值余额',
    `gift_amount`            DECIMAL(20, 2) DEFAULT 0 COMMENT '赠金余额',
    `points`                 DECIMAL(20, 2) DEFAULT 0 COMMENT '积分',
    `debt_amount`            DECIMAL(20, 2) DEFAULT 0 COMMENT '欠款',
    `consume_times`          INT            DEFAULT 0 COMMENT '消费次数',
    `consume_amount`         DECIMAL(20, 2) DEFAULT 0 COMMENT '消费金额',
    `last_consume_date`      TIMESTAMP NULL COMMENT '上次消费时间',
    `last_consume_services`  TEXT COMMENT '上次消费服务',
    `last_consignee_address` VARCHAR(200) NULL COMMENT '最近收货地址',
    `last_service_staff`     VARCHAR(60)    DEFAULT NULL COMMENT '上次服务员工',
    `last_view_shop_time`    TIMESTAMP NULL COMMENT '最近浏览店铺时间',
    `referrer`               VARCHAR(60)    DEFAULT NULL COMMENT '介绍人',
    `follower`               VARCHAR(60)    DEFAULT NULL COMMENT '跟踪员工',
    `custom1`                VARCHAR(200)   DEFAULT NULL COMMENT '自定义字段1',
    `custom2`                VARCHAR(200)   DEFAULT NULL COMMENT '自定义字段2',
    `custom3`                VARCHAR(200)   DEFAULT NULL COMMENT '自定义字段3',
    `custom4`                VARCHAR(200)   DEFAULT NULL COMMENT '自定义字段4',
    `custom5`                VARCHAR(200)   DEFAULT NULL COMMENT '自定义字段5',
    `custom6`                VARCHAR(200)   DEFAULT NULL COMMENT '自定义字段6',

    -- 统计数据
    `subscribed`             TINYINT        DEFAULT 0 COMMENT '公众号绑定(关注)状态. 0:未绑定, 1:已绑定',
    `openid`                 VARCHAR(64)    DEFAULT '' COMMENT '绑定微信的openid',
    `appid`                  VARCHAR(64)    DEFAULT '' COMMENT '绑定微信的appid',

    `avg_consume_amount`     DECIMAL(20, 2) DEFAULT 0 COMMENT '客单价',
    `purchase_rate`          DECIMAL(20, 2) DEFAULT 0 COMMENT '消费频率',
    `first_order_time`       TIMESTAMP NULL COMMENT '首次下单时间',
    `last_order_time`        TIMESTAMP NULL COMMENT '最近下单时间',
    `order_count`            INT            DEFAULT 0 COMMENT '伊智通产生总订单数',
    `total_order_amount`     DECIMAL(20, 2) DEFAULT 0 COMMENT '伊智通产生总订单额',
    `refund_amount`          DECIMAL(20, 2) DEFAULT 0 COMMENT '累计退款金额',
    `refund_count`           INT            DEFAULT 0 COMMENT '累计退款单数',
    KEY                      `ods_member_base_shop_id_idx` (`shop_id`),
    KEY                      `ods_member_base_main_shop_id_idx` (`main_shop_id`),
    KEY                      `ods_member_base_phone_idx` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='会员基础信息';


CREATE TABLE IF NOT EXISTS `mg_fact_member_group_population`
(
    `member_group_id` INT NOT NULL COMMENT '群组id',
    `member_id`       INT NOT NULL COMMENT '会员id',
    `shop_id`         INT NOT NULL,
    `__deleted`       BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`member_group_id`, `member_id`, `shop_id`),
    KEY `mg_fact_member_group_population_shop_id_idx` (`shop_id`),
    KEY `mg_fact_member_group_population_member_group_id_idx` (`member_group_id`, `member_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='会员群组人员表';


-- 脚本更新
ALTER TABLE ods_product
    ADD sold_count INT NOT NULL DEFAULT 0 COMMENT '商品总销量，包含已删除sku销量' AFTER status;

ALTER TABLE ods_social_account
    ADD COLUMN `member_base_id` INT NULL DEFAULT 0 COMMENT 'member_base主键';

ALTER TABLE ods_social_account
    ADD COLUMN `user_id` INT DEFAULT 0 COMMENT 'user主键';

ALTER TABLE ods_social_account
    ADD COLUMN `shop_id` INT DEFAULT 0 COMMENT '店铺id' AFTER id;

ALTER TABLE ods_social_account
    ADD COLUMN `appid` VARCHAR(48) DEFAULT '' COMMENT '公众号/小程序appid';

ALTER TABLE ods_social_account
    ADD INDEX `ods_social_account_shop_member_id_idx` (`shop_member_id`) USING BTREE;

ALTER TABLE ods_social_account
    ADD INDEX `ods_social_account_open_id_idx` (`open_id`) USING BTREE;

ALTER TABLE ods_log_stream
    ADD COLUMN `coupon_type` TINYINT(4) NULL DEFAULT 0 COMMENT '优惠券类型：类型 0 满减劵    1 折扣劵   2 随机金额优惠劵  3 包邮劵';


alter  TABLE  ods_wx_relation add column `__deleted` bit(1) DEFAULT b'0' COMMENT '原表数据删除标志';


