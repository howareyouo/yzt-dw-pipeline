package cn.yizhi.yzt.pipeline.jobs.dimstream;

import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcSink;
import cn.yizhi.yzt.pipeline.jobs.StreamJob;
import cn.yizhi.yzt.pipeline.config.SourceTopics;
import cn.yizhi.yzt.pipeline.model.dim.*;
import cn.yizhi.yzt.pipeline.model.ods.*;
import cn.yizhi.yzt.pipeline.model.dim.*;
import cn.yizhi.yzt.pipeline.model.ods.*;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;

public class DimStreamJob extends StreamJob {
    // interval join的时间范围
    static final int LOWER_BOUND = -5;
    static final int UPPER_BOUND = 5;

    @Override
    public void defineJob() throws Exception {
        processDimShop();
        processMember();
        processProduct();
        processProductSku();
        processFullReduce();
        processCoupon();
        processFlashSale();
    }


    public void processDimShop() {
       DataStream<Shop> shopDs = this.createStreamFromKafka(SourceTopics.TOPIC_SHOP, Shop.class)
               .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Shop>() {

                   @Override
                   public long extractAscendingTimestamp(Shop element) {
                       return (element.getUpdatedAt() != null) ?
                               element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                   }
               });

       DataStream<ShopGroup> shopGroupDs = this.createStreamFromKafka(SourceTopics.TOPIC_SHOP_GROUP, ShopGroup.class)
               .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ShopGroup>() {

                   @Override
                   public long extractAscendingTimestamp(ShopGroup element) {
                       // shopGroup没有时间戳，以processingTime代替。
                       return Instant.now().toEpochMilli();
                   }
               });

       shopDs.keyBy(Shop::getGroupId)
               .intervalJoin(shopGroupDs.keyBy(ShopGroup::getId))
               .between(Time.seconds(LOWER_BOUND), Time.seconds(UPPER_BOUND))
               .process(new ProcessJoinFunction<Shop, ShopGroup, DimShop>() {
                   @Override
                   public void processElement(Shop shop, ShopGroup group, Context ctx, Collector<DimShop> out) throws Exception {
                       DimShop dimShop = new DimShop();
                       dimShop.setShopId(shop.getId());
                       dimShop.setShopName(shop.getName());
                       dimShop.setAbbr(shop.getAbbr());
                       dimShop.setBusinessScope("未知");
                       dimShop.setLogo(shop.getLogo());
                       dimShop.setWxQrCode(shop.getWxQrCode());

                       String mgMode;
                       switch (shop.getManagementMode()) {
                           case 1:
                               mgMode = "电商";
                               break;
                           case 2:
                               mgMode = "品牌商";
                               break;
                           case 3:
                               mgMode = "单店经营";
                               break;
                           case 4:
                               mgMode = "多门店连锁";
                               break;
                           case 5:
                           default:
                               mgMode = "其他";
                       }

                       dimShop.setManagementMode(mgMode);

                       int status = shop.getStatus() == null ? 0 : shop.getStatus();
                       switch (status){
                           case 1:
                               dimShop.setStatus("营业中");
                               break;
                           case 2:
                               dimShop.setStatus("休息中");
                               break;
                           default:
                               dimShop.setStatus("未知");
                       }

                       int accessStatus = shop.getAccessStatus() == null ? 0 : shop.getAccessStatus();
                       switch (accessStatus) {
                           case 1:
                               dimShop.setAccessStatus("已激活");
                               break;
                           case 2:
                               dimShop.setAccessStatus("未激活");
                               break;
                           case 3:
                               dimShop.setAccessStatus("已过期");
                               break;
                           case 4:
                               dimShop.setAccessStatus("暂停登陆");
                               break;
                           default:
                               dimShop.setAccessStatus("未知状态");
                       }

                       if (group != null) {
                           dimShop.setGroupId(group.getId());
                           dimShop.setGroupName(group.getGroupName());
                           if (group.getGroupMode() == 1) {
                               dimShop.setGroupMode("直营");
                           } else if (group.getGroupMode() == 2) {
                               dimShop.setGroupMode("加盟");
                           } else if (group.getGroupMode() == 3) {
                               dimShop.setGroupMode("混合");
                           } else {
                               dimShop.setGroupMode("未知");
                           }

                           dimShop.setSupportShopPrice(group.getSupportShopPrice() != 0);
                       }

                       dimShop.setStartDate(new Date(Instant.now().toEpochMilli()));
                       dimShop.setEndDate(Date.valueOf("9999-12-31"));

                       out.collect(dimShop);
                   }
               }).returns(DimShop.class)
               .addSink(JdbcSink.newJdbDimTableUpsertSink(serverConfig, "dim_shop", DimShop.class))
                .name("sink-dim-shop");


    }

    private void processProduct() {
        DataStream<Product> productDs = this.createStreamFromKafka(SourceTopics.TOPIC_PRODUCT, Product.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Product>() {

                    @Override
                    public long extractAscendingTimestamp(Product element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        DataStream<ProductSetting> productSettingDs = this.createStreamFromKafka(SourceTopics.TOPIC_PRODUCT_SETTING, ProductSetting.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ProductSetting>() {

                    @Override
                    public long extractAscendingTimestamp(ProductSetting element) {
                        // ProductSetting没有时间戳，以processingTime代替。
                        return Instant.now().toEpochMilli();
                    }
                });

        productDs.keyBy("id")
                .intervalJoin(productSettingDs.keyBy("productId"))
                .between(Time.seconds(LOWER_BOUND), Time.seconds(UPPER_BOUND))
                .process(new ProcessJoinFunction<Product, ProductSetting, DimProduct>() {
                    @Override
                    public void processElement(Product product, ProductSetting productSetting, Context ctx, Collector<DimProduct> out) throws Exception {
                        DimProduct prod = new DimProduct();
                        prod.setShopId(product.getShopId());
                        prod.setProductId(product.getId());
                        prod.setProductSerial(product.getSerial());
                        prod.setProductName(product.getName());
                        prod.setImage(product.getImage());
                        prod.setProductDesc(product.getDescription());
                        prod.setShareDesc(product.getShareDesc());
                        prod.setDetail(product.getDetail());
                        prod.setVideo(product.getVideo());

                        String productType;
                        switch (product.getProductType()) {
                            case 1:
                                productType = "实物商品";
                                break;
                            case 2:
                                productType = "虚拟商品";
                                break;
                            case 3:
                                productType = "电子卡券";
                                break;
                            default:
                                productType = "未知类型";
                        }
                        prod.setProductType(productType);
                        prod.setMarkingPrice(product.getMarkingPrice());
                        prod.setWeight(product.getWeight());


                        if (productSetting != null) {

                            prod.setShipCost(productSetting.getShipCost());
                            prod.setShipCostType(productSetting.getShipCostType() == 1 ? "统一运费" :
                                    (productSetting.getShipCostType() == 2 ? "运费模板" : "未知"));

                            boolean isOffShelf = product.getStatus() == 2
                                    || (product.getStatus() == 1
                                    && (productSetting.getOnShelfTime() == null
                                    || productSetting.getOnShelfTime().after(Timestamp.from(Instant.now()))));

                            prod.setOnShelfStatus(!isOffShelf);

                            prod.setAllowRefund(productSetting.isAllowRefund());
                            prod.setAllowExpDelivery(productSetting.isAllowExpDelivery());
                            prod.setAllowPickup(productSetting.isAllowPickUp());
                        }

                        prod.setCreatedAt(product.getCreatedAt());
                        prod.setStartDate(new Date(Instant.now().toEpochMilli()));
                        prod.setEndDate(Date.valueOf("9999-12-31"));

                        out.collect(prod);
                    }
                }).returns(DimProduct.class)
                .addSink(JdbcSink.newJdbDimTableUpsertSink(serverConfig, "dim_product", DimProduct.class))
                .name("sink-dim-product");
    }

    private void processProductSku() {
        DataStream<ProductSku> productSkuDs = this.createStreamFromKafka(SourceTopics.TOPIC_PRODUCT_SKU, ProductSku.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ProductSku>() {

                    @Override
                    public long extractAscendingTimestamp(ProductSku element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        productSkuDs.map(new MapFunction<ProductSku, DimProductSKu>() {
            @Override
            public DimProductSKu map(ProductSku sku) throws Exception {
                DimProductSKu in = new DimProductSKu();
                in.setSkuSpec("默认");
                in.setSkuId(sku.getId());

                in.setShopId(0); //由批处理更新
                in.setSkuSpec(""); //由批处理更新

                in.setProductId(sku.getProductId());
                in.setSkuImage(sku.getImage());
                in.setDefaultSku(sku.getDefaultSku());
                in.setCreatedAt(sku.getCreatedAt());
                in.setPurchasePrice(sku.getPurchasePrice());
                in.setRetailPrice(sku.getRetailPrice());
                in.setStartDate(new Date(Instant.now().toEpochMilli()));
                in.setEndDate(Date.valueOf("9999-12-31"));

                return in;
            }
        }).returns(DimProductSKu.class)
        .addSink(JdbcSink.newJdbDimTableUpsertSink(serverConfig, "dim_product_sku", DimProductSKu.class))
        .name("sink-dim-product-sku");
    }

    private void processMember() {
        DataStream<ShopMember> shopMemberDs = this.createStreamFromKafka(SourceTopics.TOPIC_SHOP_MEMBER, ShopMember.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<ShopMember>() {

                    @Override
                    public long extractAscendingTimestamp(ShopMember element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        DataStream<MemberBase> memberBaseDs = this.createStreamFromKafka(SourceTopics.TOPIC_MEMBER_BASE, MemberBase.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<MemberBase>() {

                    @Override
                    public long extractAscendingTimestamp(MemberBase element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });


        DataStream<User> userDs = this.createStreamFromKafka(SourceTopics.TOPIC_USER, User.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<User>() {

                    @Override
                    public long extractAscendingTimestamp(User element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });


        shopMemberDs.keyBy("memberId")
                .intervalJoin(memberBaseDs.keyBy("id"))
                .between(Time.seconds(LOWER_BOUND), Time.seconds(UPPER_BOUND))
                .process(new ProcessJoinFunction<ShopMember, MemberBase, DimMember>() {
                    @Override
                    public void processElement(ShopMember shopMember, MemberBase memberBase, Context ctx, Collector<DimMember> out) throws Exception {
                        DimMember dimMember = new DimMember();

                        dimMember.setMemberId(shopMember.getId());
                        dimMember.setMemberBaseId(shopMember.getMemberId());
                        dimMember.setShopId(shopMember.getShopId());
                        dimMember.setMainShopId(shopMember.getMainShopId());
                        dimMember.setRemark(shopMember.getRemark());
                        dimMember.setSource(shopMember.getSource());
                        dimMember.setDisabled(shopMember.getDisabled() == 1);
                        dimMember.setCreatedAt(shopMember.getCreatedAt());

                        dimMember.setUserId(memberBase.getUserId());
                        dimMember.setName(memberBase.getName());
                        dimMember.setNickname(memberBase.getNickname());
                        dimMember.setAvatar(memberBase.getAvatar());
                        dimMember.setGender(memberBase.getGender() == 0 ? "未知" : (memberBase.getGender() == 1 ? "男" : "女"));
                        dimMember.setWechat(memberBase.getWechat());
                        dimMember.setProvince(memberBase.getProvince());
                        dimMember.setCity(memberBase.getCity());
                        dimMember.setBirthday(memberBase.getBirthday());

                        out.collect(dimMember);
                    }
                }).returns(DimMember.class)
                .keyBy("userId")
                .intervalJoin(userDs.keyBy("id"))
                .between(Time.seconds(LOWER_BOUND), Time.seconds(UPPER_BOUND))
                .process(new ProcessJoinFunction<DimMember, User, DimMember>() {
                    @Override
                    public void processElement(DimMember member, User user, Context ctx, Collector<DimMember> out) throws Exception {
                        member.setPhone(user.getPhone());
                        member.setCountryCode(user.getCountryCode());
                        member.setStartDate(new Date(Instant.now().toEpochMilli()));
                        member.setEndDate(Date.valueOf("9999-12-31"));

                        out.collect(member);
                    }
                }).returns(DimMember.class)
                .addSink(JdbcSink.newJdbDimTableUpsertSink(serverConfig, "dim_member", DimMember.class))
                .name("sink-dim-member");
    }

    /**
     * 满减活动维度
     */
    private void processFullReduce(){
        SingleOutputStreamOperator<FullReducePromotion> fullReduceStream = this.createStreamFromKafka(SourceTopics.TOPIC_FULL_REDUCE_PROMOTION, FullReducePromotion.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<FullReducePromotion>() {
                    @Override
                    public long extractAscendingTimestamp(FullReducePromotion element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        fullReduceStream.map(new MapFunction<FullReducePromotion, DimFullReduce>() {
            @Override
            public DimFullReduce map(FullReducePromotion value) throws Exception {
                DimFullReduce dimFullReduce = new DimFullReduce();

                dimFullReduce.setFullReduceId(value.getId());
                dimFullReduce.setShopId(value.getShopId());
                dimFullReduce.setName(value.getName());
                dimFullReduce.setProductRule(value.getProductRule());
                dimFullReduce.setDiscountRule(value.getDiscountRule());
                dimFullReduce.setParticipateRule(value.getParticipateRule());
                dimFullReduce.setForFirstMember(value.getForFirstMember() == 0 ? "否" : "是");
                dimFullReduce.setStartTime(value.getStartTime());
                dimFullReduce.setEndTime(value.getEndTime());
                String state;
                switch (value.getState()){
                    case 0:
                        state = "未开始";
                        break;
                    case 1:
                        state = "进行中";
                        break;
                    case 2:
                        state = "已结束";
                        break;
                    default:
                       state =  "未知";
                       break;
                }
                dimFullReduce.setState(state);
                dimFullReduce.setDelFlag(value.getDelFlag() == 0 ? "未删除": "已删除");
                dimFullReduce.setDiscountRuleUpdatedAt(value.getDiscountRuleUpdatedAt());
                dimFullReduce.setIsLongTerm(value.getIsLongTerm());
                dimFullReduce.setIsIncludeAllProduct(value.getIsIncludeAllProduct());
                dimFullReduce.setCreatedAt(value.getCreatedAt());
                dimFullReduce.setStartDate(new Date(Instant.now().toEpochMilli()));
                dimFullReduce.setEndDate(Date.valueOf("9999-12-31"));
                return dimFullReduce;
            }
        }).returns(DimFullReduce.class)
          .addSink(JdbcSink.newJdbDimTableUpsertSink(serverConfig, "dim_full_reduce", DimFullReduce.class))
          .name("sink-dim-full-reduce");
    }

    /**
     * 优惠劵维度
     */
    private void processCoupon(){
        SingleOutputStreamOperator<CouponTemplate> couponTemplateDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_COUPON_TEMPLATE, CouponTemplate.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<CouponTemplate>() {
                    @Override
                    public long extractAscendingTimestamp(CouponTemplate element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        couponTemplateDataStream.map(new MapFunction<CouponTemplate, DimCoupon>() {
            @Override
            public DimCoupon map(CouponTemplate value) throws Exception {
                DimCoupon dimCoupon = new DimCoupon();

                dimCoupon.setCouponId(value.getId());
                dimCoupon.setShopId(value.getShopId());
                dimCoupon.setCouponName(value.getCouponName());
                dimCoupon.setDescription(value.getDescription());
                String couponType;
                switch (value.getCouponState()){
                    case 0:
                        couponType = "满减劵";
                        break;
                    case 1:
                        couponType = "折扣劵";
                        break;
                    case 2:
                        couponType = "随机金额优惠劵";
                        break;
                    case 3:
                        couponType = "包邮劵";
                        break;
                    default:
                        couponType =  "未知";
                        break;
                }
                dimCoupon.setCouponType(couponType);
                dimCoupon.setIssuedQuantity(value.getIssuedQuantity());
                dimCoupon.setIssuedAmount(value.getIssuedAmount());
                dimCoupon.setStartTime(value.getStartTime());
                dimCoupon.setEndTime(value.getEndTime());
                String couponState;
                switch (value.getCouponState()){
                    case 0:
                        couponState = "未开始";
                        break;
                    case 1:
                        couponState = "进行中";
                        break;
                    case 2:
                        couponState = "已结束";
                        break;
                    case 3:
                        couponState = "停止发劵";
                        break;
                    default:
                        couponState =  "未知";
                        break;
                }
                dimCoupon.setCouponState(couponState);
                dimCoupon.setIsVisible(value.getIsVisible() == 0 ? "可见" : "不可见");
                dimCoupon.setIsRecoverIssued(value.getIsRecoverIssued() == 0 ? "不恢复投放" : "恢复投放");
                dimCoupon.setPriority(value.getPriority());
                dimCoupon.setUseRule(value.getUseRule());
                dimCoupon.setDelFlag(value.getDelFlag() == 0 ? "未删除" : "已删除");
                dimCoupon.setCreatedAt(value.getCreatedAt());
                dimCoupon.setStartDate(new Date(Instant.now().toEpochMilli()));
                dimCoupon.setEndDate(Date.valueOf("9999-12-31"));

                return dimCoupon;
            }
        }).returns(DimCoupon.class)
        .addSink(JdbcSink.newJdbDimTableUpsertSink(serverConfig,"dim_coupon", DimCoupon.class))
        .name("sink-dim-coupon");
    }

    /**
     * 秒杀维度
     */
    private void processFlashSale(){
        SingleOutputStreamOperator<FlashSaleActivity> flashSaleDataStream = this.createStreamFromKafka(SourceTopics.TOPIC_FLASHSALE_ACTIVITY, FlashSaleActivity.class)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<FlashSaleActivity>() {
                    @Override
                    public long extractAscendingTimestamp(FlashSaleActivity element) {
                        return (element.getUpdatedAt() != null) ?
                                element.getUpdatedAt().getTime() : element.getCreatedAt().getTime();
                    }
                });

        flashSaleDataStream.map(new MapFunction<FlashSaleActivity, DimFlashSale>() {
            @Override
            public DimFlashSale map(FlashSaleActivity value) throws Exception {
                DimFlashSale dimFlashSale = new DimFlashSale();

                dimFlashSale.setFlashSaleId(value.getId());
                dimFlashSale.setShopId(value.getShopId());
                dimFlashSale.setActivityImg(value.getActivityImg());
                dimFlashSale.setActivityCode(value.getActivityCode());
                dimFlashSale.setActivityName(value.getActivityName());
                dimFlashSale.setActivityRule(value.getActivityRule() == 0 ? "固定时间活动" : "周期活动");
                dimFlashSale.setStartTime(value.getStartTime());
                dimFlashSale.setEndTime(value.getEndTime());
                dimFlashSale.setCurrentPeriod(value.getCurrentPeriod());
                dimFlashSale.setPeriodSum(value.getPeriodSum());
                String activityState;
                switch (value.getActivityRule()){
                    case 0:
                        activityState = "未开始";
                        break;
                    case 1:
                        activityState = "预热中";
                        break;
                    case 2:
                        activityState = "进行中";
                        break;
                    case 3:
                        activityState = "已结束";
                        break;
                    default:
                        activityState =  "未知";
                        break;
                }
                dimFlashSale.setActivityState(activityState);
                dimFlashSale.setCronRule(value.getCronRule());
                dimFlashSale.setTagId(value.getTagId());
                dimFlashSale.setTagName(value.getTagName());
                dimFlashSale.setPresetNum(value.getPresetNum());
                dimFlashSale.setOnlyNew(value.getOnlyNew());
                dimFlashSale.setBuyLimit(value.getBuyLimit());
                dimFlashSale.setPreheatHour(value.getPreheatHour());
                dimFlashSale.setRemindAheadMinute(value.getRemindAheadMinute());
                dimFlashSale.setLockInventorySetting(value.getLockInventorySetting());
                dimFlashSale.setBarrageSetting(value.getBarrageSetting());
                dimFlashSale.setJoinedShow(value.getJoinedShow() == 0 ? "不显示" : "显示");
                dimFlashSale.setBuyedShow(value.getBuyedShow() == 0 ? "不显示" : "显示");
                dimFlashSale.setPopularShow(value.getPopularShow() == 0 ? "不显示" : "显示");
                dimFlashSale.setIndexTemplateId(value.getIndexTemplateId());
                dimFlashSale.setRushTemplateId(value.getRushTemplateId());
                dimFlashSale.setMemberTags(value.getMemberTags());
                dimFlashSale.setCreatedAt(value.getCreatedAt());
                dimFlashSale.setDisabled(value.getDisabled());
                dimFlashSale.setStartDate(new Date(Instant.now().toEpochMilli()));
                dimFlashSale.setEndDate(Date.valueOf("9999-12-31"));

                return dimFlashSale;
            }
        }).returns(DimFlashSale.class)
        .addSink(JdbcSink.newJdbDimTableUpsertSink(serverConfig,"dim_flash_sale",DimFlashSale.class))
        .name("sink-dim-flash-sale");
    }

}
