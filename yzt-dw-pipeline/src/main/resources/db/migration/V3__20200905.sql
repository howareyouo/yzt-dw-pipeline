-- ----------------------------
-- Table structure for fact_member_first_member
-- 店铺下会员第一次支付订单数据
-- ----------------------------
create TABLE `fact_member_first_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id`        bigint      NOT NULL    COMMENT '订单id',
  `main_shop_id`    int(11)                 COMMENT '连锁店id',
  `shop_id`         int(11)                 COMMENT '店铺id',
  `union_no`        varchar(50) NOT NULL    COMMENT '父订单号',
  `order_no`        varchar(50) NOT NULL    COMMENT '订单号',
  `transaction_no`  varchar(64) NULL        COMMENT '支付流水号',
  `payment_method`  varchar(20) NULL        COMMENT '支付方式',
  `source`          varchar(20) NOT NULL    COMMENT '订单来源',
  `promotion_type`  int(11)     NULL        COMMENT '活动类型',
  `promotion_id`    int(11)     NULL        COMMENT '活动id',
  `member_id`       int(11)     NOT NULL    COMMENT '会员id',
  `paid_at`         TIMESTAMP   NULL        COMMENT '订单支付时间',
  `created_at`      TIMESTAMP   NULL        COMMENT '订单创建时间',
  PRIMARY KEY (`id`),
  KEY `fm_shop_id_index` (`shop_id`),
  KEY `fm_member_id_index` (`member_id`),
  KEY `fm_promotion_id_index` (`promotion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- 直播表默认值修改
-- ----------------------------
alter table fact_live_room alter column order_count set default 0;
alter table fact_live_room alter column product_count set default 0;
alter table fact_live_room alter column revived_coupon_count set default 0;
alter table fact_live_room alter column pv set default 0;
alter table fact_live_room alter column uv set default 0;
alter table fact_live_room alter column uv_by_share set default 0;
alter table fact_live_room alter column pay_number set default 0;
alter table fact_live_room alter column new_number set default 0;