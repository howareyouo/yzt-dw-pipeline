package cn.yizhi.yzt.pipeline.jobs.dataPrepare;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcTableOutputFormat;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactFullReduceHistory;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactProductFullReduceHistory;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactUserFullReduceHistory;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactFullReduceV1;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactProductFullReduceV1;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactUserFullReduceV1;
import cn.yizhi.yzt.pipeline.util.Beans;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.types.Row;

public class FactFullReduceBatchJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSet<Row> factFullReduceHistoryRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactFullReduceHistory.class,
                "fact_full_reduce",
                null)
                .name("ds-fact_full_reduce");

        DataSet<Row> factProductFullReduceRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactProductFullReduceHistory.class,
                "fact_product_full_reduce",
                null)
                .name("ds-fact_product_full_reduce");

        DataSet<Row> factUserFullReduceRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactUserFullReduceHistory.class,
                "fact_user_full_reduce",
                null)
                .name("ds-fact_user_full_reduce");


        OutputFormat<FactFullReduceV1> factFullReduceV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactFullReduceV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_FULL_REDUCE)
                        .finish();


        OutputFormat<FactUserFullReduceV1> factUserFullReduceV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactUserFullReduceV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_USER_FULL_REDUCE)
                        .finish();

        OutputFormat<FactProductFullReduceV1> factProductFullReduceV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactProductFullReduceV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_PRODUCT_FULL_REDUCE)
                        .finish();


        factFullReduceHistoryRow
                .map(new MapFunction<Row, FactFullReduceV1>() {
                    @Override
                    public FactFullReduceV1 map(Row value) throws Exception {
                        FactFullReduceHistory factFullReduceHistory = PojoTypes.of(FactFullReduceHistory.class).fromRow(value);
                        return convertToFactFullReduceV1(factFullReduceHistory);
                    }
                })
                .filter(new FilterFunction<FactFullReduceV1>() {
                    @Override
                    public boolean filter(FactFullReduceV1 value) throws Exception {
                        return value.getRowDate() != null && value.getPromotionId() != null
                                && value.getShopId() != null && value.getPv() != 0
                                && TimeUtil.convertStringDayToLong(value.getRowDate()) <= TimeUtil.convertStringDayToLong(serverConfig.getEndDay());
                    }
                })
                .output(factFullReduceV1Oft);


        factProductFullReduceRow
                .map(new MapFunction<Row, FactProductFullReduceV1>() {
                    @Override
                    public FactProductFullReduceV1 map(Row value) throws Exception {
                        FactProductFullReduceHistory factProductFullReduceHistory = PojoTypes.of(FactProductFullReduceHistory.class).fromRow(value);
                        return convertToFactUserFullReduceV1(factProductFullReduceHistory);
                    }
                })
                .filter(new FilterFunction<FactProductFullReduceV1>() {
                    @Override
                    public boolean filter(FactProductFullReduceV1 value) throws Exception {
                        return  value.getRowDate() != null && value.getFullReduceId() != null
                                && value.getShopId() != null && value.getProductId() != null
                                && value.getPv() != 0
                                && TimeUtil.convertStringDayToLong(value.getRowDate()) <= TimeUtil.convertStringDayToLong(serverConfig.getEndDay());
                    }
                })
                .output(factProductFullReduceV1Oft);

        factUserFullReduceRow
                .map(new MapFunction<Row, FactUserFullReduceV1>() {
                    @Override
                    public FactUserFullReduceV1 map(Row value) throws Exception {
                        FactUserFullReduceHistory factUserFullReduceHistory = PojoTypes.of(FactUserFullReduceHistory.class).fromRow(value);
                        return convertToFactUserFullReduceV1(factUserFullReduceHistory);
                    }
                })
                .filter(new FilterFunction<FactUserFullReduceV1>() {
                    @Override
                    public boolean filter(FactUserFullReduceV1 value) throws Exception {
                        return value.getRowDate() != null && value.getFullReduceId() != null && value.getShopId() != null
                                && value.getMemberId() != null;
                    }
                })
                .output(factUserFullReduceV1Oft);
    }

    private static FactFullReduceV1 convertToFactFullReduceV1(FactFullReduceHistory factFlashSaleHistory) {

        return Beans.copyProperties(factFlashSaleHistory, FactFullReduceV1.class);
    }

    private static FactUserFullReduceV1 convertToFactUserFullReduceV1(FactUserFullReduceHistory factUserFullReduceHistory) {

        return Beans.copyProperties(factUserFullReduceHistory, FactUserFullReduceV1.class);
    }

    private static FactProductFullReduceV1 convertToFactUserFullReduceV1(FactProductFullReduceHistory factProductFullReduceHistory) {

        return Beans.copyProperties(factProductFullReduceHistory, FactProductFullReduceV1.class);
    }
}
