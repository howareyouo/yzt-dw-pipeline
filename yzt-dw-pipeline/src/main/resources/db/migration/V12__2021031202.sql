ALTER TABLE `mg_fact_member_union_v1`
    MODIFY COLUMN `shop_id` int (11) DEFAULT NULL COMMENT '店铺id';

ALTER TABLE `mg_fact_member_union_v1`
    MODIFY COLUMN `main_shop_id` int (11) DEFAULT NULL COMMENT '主店id';

ALTER TABLE `mg_fact_member_shop_visits_v1`
    MODIFY COLUMN `shop_id` int (11) DEFAULT '0' COMMENT '店铺ID';

ALTER TABLE `mg_fact_member_coupon_daily_v1`
    MODIFY COLUMN `shop_id` int (11) NOT NULL DEFAULT '0' COMMENT '店铺ID';


