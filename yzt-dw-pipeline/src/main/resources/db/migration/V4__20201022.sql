ALTER TABLE ods_coupon_use_record ADD KEY `ods_coupon_use_record_coupon_template_id_index` (`coupon_template_id`);
ALTER TABLE ods_coupon_use_record ADD KEY `ods_coupon_use_record_shop_id_index` (`shop_id`);
ALTER TABLE fact_member_promotion ADD KEY `fact_member_promotion_shop_id_index` (`shop_id`);
ALTER TABLE fact_member_first_order ADD KEY `fact_member_first_order_shop_id_index` (`shop_id`);
ALTER TABLE ods_shop_member ADD KEY `ods_shop_member_shop_id_index` (`shop_id`);