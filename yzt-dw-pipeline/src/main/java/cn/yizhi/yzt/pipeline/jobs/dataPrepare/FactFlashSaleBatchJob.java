package cn.yizhi.yzt.pipeline.jobs.dataPrepare;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcTableOutputFormat;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactFlashSaleHistory;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactFlashSaleProductHistory;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactFlashSaleProductV1;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactFlashSaleV1;
import cn.yizhi.yzt.pipeline.util.Beans;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.types.Row;

public class FactFlashSaleBatchJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSet<Row> factFlashSaleRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactFlashSaleHistory.class,
                "fact_flash_sale",
                null)
                .name("ds-fact_flash_sale");

        DataSet<Row> factFlashSaleProductRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactFlashSaleProductHistory.class,
                "fact_flash_sale_product",
                null)
                .name("ds-fact-flash-sale-product");

        OutputFormat<FactFlashSaleV1> factFlashSaleV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactFlashSaleV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_FLASH_ACTIVITY)
                        .finish();

        OutputFormat<FactFlashSaleProductV1> factFlashSaleProductV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactFlashSaleProductV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_FLASH_ACTIVITY_PRODUCT)
                        .finish();


        //fact_flash_sale
        factFlashSaleRow
                .map(new MapFunction<Row, FactFlashSaleV1>() {
                    @Override
                    public FactFlashSaleV1 map(Row value) throws Exception {
                        FactFlashSaleHistory factFlashSaleHistory = PojoTypes.of(FactFlashSaleHistory.class).fromRow(value);
                        return convertToFactFlashSaleV1(factFlashSaleHistory);
                    }
                })
                .filter(new FilterFunction<FactFlashSaleV1>() {
                    @Override
                    public boolean filter(FactFlashSaleV1 value) throws Exception {
                        return value.getRowDate() != null && value.getActivityId() != null && value.getPv() != 0
                                && TimeUtil.convertStringDayToLong(value.getRowDate()) <= TimeUtil.convertStringDayToLong(serverConfig.getEndDay());
                    }
                })
                .output(factFlashSaleV1Oft);

        //fact_flash_sale_product
        factFlashSaleProductRow
                .map(new MapFunction<Row, FactFlashSaleProductV1>() {
                    @Override
                    public FactFlashSaleProductV1 map(Row value) throws Exception {
                        FactFlashSaleProductHistory factFlashSaleHistory = PojoTypes.of(FactFlashSaleProductHistory.class).fromRow(value);
                        return convertToFactFlashSaleProductV1(factFlashSaleHistory);
                    }
                })
                .filter(new FilterFunction<FactFlashSaleProductV1>() {
                    @Override
                    public boolean filter(FactFlashSaleProductV1 value) throws Exception {
                        return value.getRowDate() != null && value.getActivityId() != null
                                && value.getProductId() != null && value.getPv() != 0;
                    }
                })
                .output(factFlashSaleProductV1Oft);
    }

    private static FactFlashSaleV1 convertToFactFlashSaleV1(FactFlashSaleHistory factFlashSaleHistory) {

        return Beans.copyProperties(factFlashSaleHistory, FactFlashSaleV1.class);
    }

    private static FactFlashSaleProductV1 convertToFactFlashSaleProductV1(FactFlashSaleProductHistory factFlashSaleProductHistory) {
        FactFlashSaleProductV1 factFlashSaleProductV1 = Beans.copyProperties(factFlashSaleProductHistory, FactFlashSaleProductV1.class);
        factFlashSaleProductV1.setAppointmentCount(Integer.parseInt(factFlashSaleProductHistory.getAppointmentCount()));

        return factFlashSaleProductV1;
    }
}
