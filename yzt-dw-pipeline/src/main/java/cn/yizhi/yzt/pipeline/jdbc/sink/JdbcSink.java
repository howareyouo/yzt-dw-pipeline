package cn.yizhi.yzt.pipeline.jdbc.sink;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jdbc.JdbcType2DimTableOutputFormat;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.shaded.guava30.com.google.common.base.Preconditions;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author zjzhang
 */
public class JdbcSink<T> extends RichSinkFunction<T> implements CheckpointedFunction {
    private final BatchingOutputFormat<T,T, JdbcBatchStatementExecutor<T>> outputFormat;

    public JdbcSink(@Nonnull BatchingOutputFormat<T,T, JdbcBatchStatementExecutor<T>> outputFormat) {
        this.outputFormat = Preconditions.checkNotNull(outputFormat);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        RuntimeContext ctx = getRuntimeContext();
        outputFormat.setRuntimeContext(ctx);
        outputFormat.open(ctx.getIndexOfThisSubtask(), ctx.getNumberOfParallelSubtasks());
    }

    @Override
    public void invoke(T value, Context context) throws IOException {
        outputFormat.writeRecord(value);
    }

    @Override
    public void initializeState(FunctionInitializationContext context) {
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        outputFormat.flush();
    }

    @Override
    public void close() {
        outputFormat.close();
    }

    public static <T> JdbcSink<T> newJdbUpsertSink(ServerConfig serverConfig, String jdbcTableName, Class<T> pojoClass) {
        JdbcTableOutputFormat<T> outputFormat = new JdbcTableOutputFormat.JdbcTableOutputFormatBuilder<>(pojoClass)
               .setDriverName("com.mysql.cj.jdbc.Driver")
               .setBatchSize(serverConfig.getJdbcBatchSize())
               .setBatchInterval(serverConfig.getJdbcBatchInterval())
               .setDBUrl(serverConfig.getJdbcDBUrl())
               .setUsername(serverConfig.getJdbcUsername())
               .setPassword(serverConfig.getJdbcPassword())
               .setTableName(jdbcTableName)
               .finish();

        return new JdbcSink<>(outputFormat);
    }

    public static <T> JdbcSink<T> newJdbDimTableUpsertSink(ServerConfig serverConfig, String jdbcTableName, Class<T> pojoClass) {
        JdbcType2DimTableOutputFormat<T> outputFormat = new JdbcType2DimTableOutputFormat.JdbcType2DimTableOutputFormatBuilder<>(pojoClass)
                .setDrivername("com.mysql.cj.jdbc.Driver")
                .setBatchSize(serverConfig.getJdbcBatchSize())
                .setBatchInterval(serverConfig.getJdbcBatchInterval())
                .setDBUrl(serverConfig.getJdbcDBUrl())
                .setUsername(serverConfig.getJdbcUsername())
                .setPassword(serverConfig.getJdbcPassword())
                .setTableName(jdbcTableName)
                .finish();

        return new JdbcSink<>(outputFormat);
    }
}
