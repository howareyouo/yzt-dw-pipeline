-- 活动日志数据
create TABLE `fact_promotion_log_v1`
(
    `shop_id`          int(11)                                NOT NULL DEFAULT '0' COMMENT '店铺ID',
    `channel`          varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '渠道',
    `promotion_type`   int(11)                                         DEFAULT 0 COMMENT '活动类型',
    `promotion_id`     int(11)                                         DEFAULT 0 COMMENT '活动id',
    `pv`               int(11)                                         DEFAULT 0 COMMENT '浏览次数',
    `uv`               int(11)                                         DEFAULT 0 COMMENT '浏览人数',
    `share_count`      int(11)                                         DEFAULT 0 COMMENT '分享次数',
    `share_user_count` int(11)                                         DEFAULT 0 COMMENT '分享人数',
    PRIMARY KEY (`shop_id`, `promotion_type`, `promotion_id`, `channel`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;