-- 店铺
delete
from fact_shop_day_v1
where row_time >= '2020-11-20';

delete
from fact_shop_hour_v1
where row_time >= '2020-11-20 00';

delete
from fact_shop_month_v1
where row_time >= '2020-11';

-- 秒杀
delete
from fact_flash_sale_v1
where row_date >= '2020-11-20';

delete
from fact_flash_sale_product_v1
where row_date >= '2020-11-20';

-- 优惠劵
delete
from fact_coupon_v1
where row_date >= '2020-11-20';

-- 满减
delete
from fact_full_reduce_v1
where row_date >= '2020-11-20';

delete
from fact_product_full_reduce_v1
where row_date >= '2020-11-20';

delete
from fact_user_full_reduce_v1
where row_date >= '2020-11-20';

-- 商品
delete
from fact_product_v1
where row_date >= '2020-11-20';