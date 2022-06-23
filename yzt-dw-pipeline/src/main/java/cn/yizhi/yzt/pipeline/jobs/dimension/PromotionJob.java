package cn.yizhi.yzt.pipeline.jobs.dimension;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.JdbcType2DimTableOutputFormat;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.model.dim.DimPromotion;
import cn.yizhi.yzt.pipeline.model.ods.CouponTemplate;
import cn.yizhi.yzt.pipeline.model.ods.FlashSaleActivity;
import cn.yizhi.yzt.pipeline.model.ods.FullReducePromotion;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.types.Row;

import java.sql.Date;
import java.time.Instant;

public class PromotionJob {

    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSource<Row> odsCouponTemplate = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                CouponTemplate.class,
                "ods_coupon_template",
                null)
                .name("ds-ods-coupon-template");


        DataSource<Row> odsFlashSale = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FlashSaleActivity.class,
                "ods_flashsale_activity",
                null).name("ds-ods-flashsale");

        DataSource<Row> odsFullReduce = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FullReducePromotion.class,
                "ods_full_reduce_promotion",
                null
        ).name("ds-ods-fullreduce");

        // sink
        OutputFormat<DimPromotion> promotionOut =
                new JdbcType2DimTableOutputFormat.JdbcType2DimTableOutputFormatBuilder<>(DimPromotion.class)
                .setDrivername("com.mysql.cj.jdbc.Driver")
                .setBatchSize(serverConfig.getJdbcBatchSize())
                .setDBUrl(serverConfig.getJdbcDBUrl())
                .setUsername(serverConfig.getJdbcUsername())
                .setPassword(serverConfig.getJdbcPassword())
                .setTableName("dim_promotion")
                .finish();


        odsCouponTemplate.map(new MapFunction<Row, DimPromotion>() {
            @Override
            public DimPromotion map(Row value) throws Exception {
                CouponTemplate couponTemplate = PojoTypes.of(CouponTemplate.class).fromRow(value);

                DimPromotion promotion = new DimPromotion();
                promotion.setPromotionId(couponTemplate.getId());
                promotion.setPromotionName(couponTemplate.getCouponName());
                promotion.setPromotionType("优惠券");
                promotion.setPromotionTypeCode(1);
                promotion.setStartDate(new Date(Instant.now().toEpochMilli()));
                promotion.setEndDate(Date.valueOf("9999-12-31"));

                return promotion;
            }
        }).output(promotionOut);


        odsFlashSale.map(new MapFunction<Row, DimPromotion>() {
            @Override
            public DimPromotion map(Row value) throws Exception {
                FlashSaleActivity flashSaleActivity = PojoTypes.of(FlashSaleActivity.class).fromRow(value);

                DimPromotion promotion = new DimPromotion();
                promotion.setPromotionId(flashSaleActivity.getId());
                promotion.setPromotionName(flashSaleActivity.getActivityName());
                promotion.setPromotionType("限时抢购");
                promotion.setPromotionTypeCode(3);
                promotion.setStartDate(new Date(Instant.now().toEpochMilli()));
                promotion.setEndDate(Date.valueOf("9999-12-31"));

                return promotion;
            }
        }).output(promotionOut);


        odsFullReduce.map(new MapFunction<Row, DimPromotion>() {
            @Override
            public DimPromotion map(Row value) throws Exception {
                FullReducePromotion fullReducePromotion = PojoTypes.of(FullReducePromotion.class).fromRow(value);

                DimPromotion promotion = new DimPromotion();
                promotion.setPromotionId(fullReducePromotion.getId().intValue());
                promotion.setPromotionName(fullReducePromotion.getName());
                promotion.setPromotionType("满减满赠");
                promotion.setPromotionTypeCode(2);
                promotion.setStartDate(new Date(Instant.now().toEpochMilli()));
                promotion.setEndDate(Date.valueOf("9999-12-31"));

                return promotion;
            }
        }).output(promotionOut);

    }
}
