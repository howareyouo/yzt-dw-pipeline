alter  TABLE  ods_order_item add column   `parent_id` bigint(20) DEFAULT '0' COMMENT '组合商品具体sku子项的父ID';



