package cn.yizhi.yzt.pipeline.jobs.dataPrepare;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.Tables;
import cn.yizhi.yzt.pipeline.jdbc.JdbcDataSourceBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcTableOutputFormat;
import cn.yizhi.yzt.pipeline.model.fact.historyfact.FactCouponHistory;
import cn.yizhi.yzt.pipeline.model.fact.newfact.FactCouponV1;
import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.types.Row;

public class FactCouponBatchJob {
    public static void run(ExecutionEnvironment env, ServerConfig serverConfig) throws Exception {



        DataSet<Row> factCouponRow = JdbcDataSourceBuilder.buildDataSource(env,
                        serverConfig,
                        FactCouponHistory.class,
                        "fact_coupon",
                        null)
                .name("ds-fact_coupon");

        OutputFormat<FactCouponV1> factCouponV1Oft =
                new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(FactCouponV1.class)
                        .setDriverName("com.mysql.cj.jdbc.Driver")
                        .setBatchSize(serverConfig.getJdbcBatchSize())
                        .setBatchInterval(serverConfig.getJdbcBatchInterval())
                        .setDBUrl(serverConfig.getJdbcDBUrl())
                        .setUsername(serverConfig.getJdbcUsername())
                        .setPassword(serverConfig.getJdbcPassword())
                        .setTableName(Tables.SINK_TABLE_COUPON)
                        .finish();

        factCouponRow
                .map(new MapFunction<Row, FactCouponV1>() {
                    @Override
                    public FactCouponV1 map(Row value) throws Exception {
                        FactCouponHistory factCouponV1 = PojoTypes.of(FactCouponHistory.class).fromRow(value);
                        return convertToFactCouponV1(factCouponV1);
                    }
                })
                .filter(new FilterFunction<FactCouponV1>() {
                    @Override
                    public boolean filter(FactCouponV1 value) throws Exception {
                        return value.getRowDate() != null && value.getChannel() != null
                                && value.getCouponId() != null && value.getShopId() != null
                                && TimeUtil.convertStringDayToLong(value.getRowDate()) <= TimeUtil.convertStringDayToLong(serverConfig.getEndDay());
                    }
                })
                .output(factCouponV1Oft);

    }

    private static FactCouponV1 convertToFactCouponV1(FactCouponHistory factCouponHistory) {
        // return copyProperties(factCouponHistory, FactCouponV1.class);
        return new FactCouponV1();
    }
}
