package cn.yizhi.yzt.pipeline.jobs.dimension;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.JdbcType2DimTableOutputFormat;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.model.dim.DimCoupon;
import cn.yizhi.yzt.pipeline.model.ods.CouponTemplate;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.types.Row;

import java.sql.Date;
import java.time.Instant;

/**
 * @author hucheng
 * @date 2020/8/20 15:25
 */
public class DimCouponJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSource<Row> odsCouponTemplate = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                CouponTemplate.class,
                "ods_coupon_template",
                null).name("ds-odsCouponTemplate");

        //sink
        OutputFormat<DimCoupon> outputFormat =
                new JdbcType2DimTableOutputFormat.JdbcType2DimTableOutputFormatBuilder<>(DimCoupon.class)
                        .setDrivername("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName("dim_coupon")
                        .finish();

        //数据清洗
        odsCouponTemplate.map(new MapFunction<Row, DimCoupon>() {
            @Override
            public DimCoupon map(Row row) throws Exception {
                CouponTemplate value = PojoTypes.of(CouponTemplate.class).fromRow(row);

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
        }).output(outputFormat);
    }
}
