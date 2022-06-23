-- 新增 fact_shop_total 表
-- ----------------------------
-- Table structure for fact_shop_total
-- ----------------------------
CREATE TABLE `fact_shop_total` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `month` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '月',
  `shop_id` int(11) DEFAULT NULL COMMENT '店铺ID',
  `main_shop_id` int(11) DEFAULT NULL COMMENT '总店id',
  `member_count` int(11) DEFAULT NULL COMMENT '会员总数',
  `pay_amount` decimal(20,2) DEFAULT NULL COMMENT '销售额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 新增字段
ALTER TABLE  `ods_member_extend` ADD COLUMN `last_view_shop_time` timestamp NULL DEFAULT NULL COMMENT '最近浏览店铺时间';
ALTER TABLE  `ods_member_extend` ADD COLUMN `last_consignee_address` varchar(200) DEFAULT NULL COMMENT '最近收货地址';
