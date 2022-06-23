package cn.yizhi.yzt.pipeline.jobs.dimension;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.JdbcType2DimTableOutputFormat;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.model.dim.DimProduct;
import cn.yizhi.yzt.pipeline.model.dim.DimProductSKu;
import cn.yizhi.yzt.pipeline.model.ods.*;
import cn.yizhi.yzt.pipeline.model.ods.*;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DimProductJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSource<Row> odsProduct = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                Product.class,
                "ods_product",
                null)
                .name("ds-odsProduct");

        DataSource<Row> odsProductSetting = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                ProductSetting.class,
                "ods_product_setting",
                null)
                .name("ds-odsProductSetting");


        DataSource<Row> odsProductSpec = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                ProductSpec.class,
                "ods_product_spec",
                null)
                .name("ds-odsProductSpec");

        DataSource<Row> odsProductSpecValue = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                ProductSpecValue.class,
                "ods_product_spec_value",
                null)
                .name("ds-odsProductSpecValue");

        DataSource<Row> odsProductReSpec = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                ProductReSpec.class,
                "ods_product_re_spec",
                null)
                .name("ds-odsProductReSpec");

        DataSource<Row> odsProductSku = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                ProductSku.class,
                "ods_product_sku",
                null)
                .name("ds-odsProductSku");

        DataSource<Row> odsProductSkuExtend = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                ProductSkuExtend.class,
                "ods_product_sku_extend",
                null)
                .name("ds-odsProductSkuExtend");


        // sink
        OutputFormat<DimProduct> productOut =
                new JdbcType2DimTableOutputFormat.JdbcType2DimTableOutputFormatBuilder<>(DimProduct.class)
                .setDrivername("com.mysql.cj.jdbc.Driver")
                .setBatchSize(serverConfig.getJdbcBatchSize())
                .setDBUrl(serverConfig.getJdbcDBUrl())
                .setUsername(serverConfig.getJdbcUsername())
                .setPassword(serverConfig.getJdbcPassword())
                .setTableName("dim_product")
                .finish();


        odsProduct.leftOuterJoin(odsProductSetting)
                .where("id")
                .equalTo("product_id")
                .with(new JoinFunction<Row, Row, DimProduct>() {
                    @Override
                    public DimProduct join(Row productRow, Row settingRow) throws Exception {
                        Product product = PojoTypes.of(Product.class).fromRow(productRow);

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


                        if (settingRow != null) {
                            ProductSetting setting = PojoTypes.of(ProductSetting.class).fromRow(settingRow);

                            prod.setShipCost(setting.getShipCost());
                            prod.setShipCostType(setting.getShipCostType() == 1 ? "统一运费" :
                                    (setting.getShipCostType() == 2 ? "运费模板" : "未知"));

                            boolean isOffShelf = product.getStatus() == 2
                                    || (product.getStatus() == 1
                                    && (setting.getOnShelfTime() == null
                                    || setting.getOnShelfTime().after(Timestamp.from(Instant.now()))));

                            prod.setOnShelfStatus(!isOffShelf);

                            prod.setAllowRefund(setting.isAllowRefund());
                            prod.setAllowExpDelivery(setting.isAllowExpDelivery());
                            prod.setAllowPickup(setting.isAllowPickUp());
                        }

                        prod.setCreatedAt(product.getCreatedAt());
                        prod.setStartDate(new Date(Instant.now().toEpochMilli()));
                        prod.setEndDate(Date.valueOf("9999-12-31"));

                        return prod;
                    }
                }).output(productOut);


        OutputFormat<DimProductSKu> productSkuOut =
                new JdbcType2DimTableOutputFormat.JdbcType2DimTableOutputFormatBuilder<>(DimProductSKu.class)
                .setDrivername("com.mysql.cj.jdbc.Driver")
                .setBatchSize(serverConfig.getJdbcBatchSize())
                .setDBUrl(serverConfig.getJdbcDBUrl())
                .setUsername(serverConfig.getJdbcUsername())
                .setPassword(serverConfig.getJdbcPassword())
                .setTableName("dim_product_sku")
                .finish();

        odsProductSpec.join(odsProductSpecValue)
                .where("id")
                .equalTo("spec_id")
                .with(new JoinFunction<Row, Row, Row>() {
                    @Override
                    public Row join(Row specRow, Row specValueRow) throws Exception {
                        ProductSpec spec = PojoTypes.of(ProductSpec.class).fromRow(specRow);
                        ProductSpecValue specValue = PojoTypes.of(ProductSpecValue.class).fromRow(specValueRow);

                        return Row.of(
                                spec.getId(), // spec id
                                specValue.getId(), // spec value id
                                spec.getShopId(),
                                spec.getName(),
                                specValue.getSpecValue()
                        );
                    }
                }).returns(Types.ROW_NAMED(
                        new String[]{"specId", "valueId", "shopId", "specName", "specValue"},
                        Types.INT, Types.INT, Types.INT, Types.STRING, Types.STRING)
                ).join(odsProductReSpec)
                .where(0,1)
                .equalTo("spec_id", "spec_value_id")
                .with(new JoinFunction<Row, Row, Row>() {
                    @Override
                    public Row join(Row joinedSpecRow, Row reRpecRow) throws Exception {
                        // relation
                        ProductReSpec specRe = PojoTypes.of(ProductReSpec.class).fromRow(reRpecRow);

                        return Row.of(
                                joinedSpecRow.getField(0), // spec id
                                joinedSpecRow.getField(1), // spec value id
                                joinedSpecRow.getField(2), // shop id
                                joinedSpecRow.getField(3), // spec name
                                joinedSpecRow.getField(4), // spec value name
                                specRe.getSkuId()   // index 5
                        );
                    }

                }).returns(Types.ROW_NAMED(
                    new String[]{"specId", "valueId", "shopId", "specName", "specValue", "skuId"},
                    Types.INT, Types.INT, Types.INT, Types.STRING, Types.STRING, Types.INT)
                // group by skuId
                ).groupBy("skuId")
                .reduceGroup(new GroupReduceFunction<Row, DimProductSKu>() {
                    @Override
                    public void reduce(Iterable<Row> values, Collector<DimProductSKu> out) throws Exception {
                        List<String> specList = new ArrayList<>();
                        int skuId = 0;
                        int shopId = 0;
                        for(Row row: values) {
                            specList.add(String.format("%s:%s", row.getField(3), row.getField(4)));
                            if (skuId == 0) {
                                skuId = (int)row.getField(5);
                                shopId = (int)row.getField(2);
                            }
                        }
                        //使得生成的字符串保持稳定
                        Collections.sort(specList);
                        DimProductSKu sku = new DimProductSKu();
                        sku.setSkuId(skuId);
                        sku.setShopId(shopId);
                        sku.setSkuSpec(String.join("|", specList));

                        out.collect(sku);
                    }
                }).rightOuterJoin(odsProductSku)
                .where("skuId").equalTo("id")
                .with(new JoinFunction<DimProductSKu, Row, DimProductSKu>() {
                    @Override
                    public DimProductSKu join(DimProductSKu in, Row skuRow) throws Exception {
                        ProductSku sku = PojoTypes.of(ProductSku.class).fromRow(skuRow);
                        // 默认SKU无spec定义，因此使用right outer join，避免遗漏sku
                        if (in == null) {
                            in = new DimProductSKu();
                            in.setSkuSpec("默认");
                            in.setSkuId(sku.getId());
                        }

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
                }).output(productSkuOut);
    }
}
