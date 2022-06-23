package cn.yizhi.yzt.pipeline.jobs.dataPrepare;

import cn.yizhi.yzt.pipeline.util.Beans;
import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcTableOutputFormat;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactProductHistory;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactProductV1;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.types.Row;

public class FactProductBatchJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {
        DataSet<Row> factProductRow = JdbcDataSourceBuilder.buildDataSource(env,
                serverConfig,
                FactProductHistory.class,
                "fact_product",
                null)
                .name("ds-fact_product");


        OutputFormat<FactProductV1> factProductV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactProductV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_PRODUCT)
                        .finish();

        factProductRow
                .map(new MapFunction<Row, FactProductV1>() {
                    @Override
                    public FactProductV1 map(Row value) throws Exception {
                        FactProductHistory factCouponV1 = PojoTypes.of(FactProductHistory.class).fromRow(value);
                        return convertToFactProductV1(factCouponV1);
                    }
                })
                .filter(new FilterFunction<FactProductV1>() {
                    @Override
                    public boolean filter(FactProductV1 value) throws Exception {
                        return value.getRowDate() != null && value.getShopId() != null && value.getChannel() != null
                                && value.getProductId() != null && value.getPv() != 0
                                && TimeUtil.convertStringDayToLong(value.getRowDate()) <= TimeUtil.convertStringDayToLong(serverConfig.getEndDay());
                    }
                })
                .output(factProductV1Oft);
    }

    private static FactProductV1 convertToFactProductV1(FactProductHistory factProductHistory) {

        return Beans.copyProperties(factProductHistory, FactProductV1.class);
    }
}
