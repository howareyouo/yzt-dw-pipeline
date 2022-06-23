package cn.yizhi.yzt.pipeline.jobs.dimension;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.JdbcType2DimTableOutputFormat;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.model.dim.DimShop;
import cn.yizhi.yzt.pipeline.model.ods.Shop;
import cn.yizhi.yzt.pipeline.model.ods.ShopGroup;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.types.Row;

import java.sql.Date;
import java.time.Instant;

public class DimShopJob {

    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {

        DataSource<Row> odsShop = JdbcDataSourceBuilder.buildDataSource(env,
            serverConfig,
            Shop.class,
            "ods_shop",
            null).name("ds-odsShop");
        DataSource<Row> odsShopGroup = JdbcDataSourceBuilder.buildDataSource(env,
            serverConfig,
            ShopGroup.class,
            "ods_shop_group",
            null).name("ds-odsShopGroup");

        OutputFormat<DimShop> outputFormat =
            new JdbcType2DimTableOutputFormat.JdbcType2DimTableOutputFormatBuilder<>(DimShop.class)
                .setDrivername("com.mysql.cj.jdbc.Driver")
                .setBatchSize(serverConfig.getJdbcBatchSize())
                .setDBUrl(serverConfig.getJdbcDBUrl())
                .setUsername(serverConfig.getJdbcUsername())
                .setPassword(serverConfig.getJdbcPassword())
                .setTableName("dim_shop")
                .finish();

        odsShop.leftOuterJoin(odsShopGroup)
            .where("group_id")
            .equalTo("id")
            .with(new JoinFunction<Row, Row, DimShop>() {
                @Override
                public DimShop join(Row shopRow, Row groupRow) throws Exception {
                    Shop shop = PojoTypes.of(Shop.class).fromRow(shopRow);

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
                    switch (status) {
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

                    if (groupRow != null) {
                        ShopGroup group = PojoTypes.of(ShopGroup.class).fromRow(groupRow);
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
                    return dimShop;
                }
            })
            .leftOuterJoin(odsShop)
            .where("mainShopId")
            .equalTo("id")
            .with(new JoinFunction<DimShop, Row, DimShop>() {
                @Override
                public DimShop join(DimShop dimShop, Row mainShopRow) throws Exception {
                    if (mainShopRow != null) {
                        Shop mainShop = PojoTypes.of(Shop.class).fromRow(mainShopRow);
                        dimShop.setMainShopId(mainShop.getId());
                        dimShop.setMainShopName(mainShop.getName());
                    }

                    dimShop.setStartDate(new Date(Instant.now().toEpochMilli()));
                    dimShop.setEndDate(Date.valueOf("9999-12-31"));
                    return dimShop;
                }
            }).output(outputFormat);
    }
}
