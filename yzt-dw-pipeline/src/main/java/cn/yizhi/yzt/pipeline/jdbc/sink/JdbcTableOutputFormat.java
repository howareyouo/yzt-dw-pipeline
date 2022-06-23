package cn.yizhi.yzt.pipeline.jdbc.sink;


import cn.yizhi.yzt.pipeline.jdbc.JdbcPojoStatementBuilder;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.internal.JdbcOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zjzhang
 */
public class JdbcTableOutputFormat<T> extends BatchingOutputFormat<T, T, JdbcBatchStatementExecutor<T>> {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTableOutputFormat.class);

    private JdbcTableOutputFormat(String tableName, Class<T> pojoClz, JdbcConnectionProvider connectionProvider,
                                  int batchSize, int batchInterval) {
        super(
                connectionProvider,
                new JdbcExecutionOptions.Builder().withBatchIntervalMs(batchInterval).withBatchSize(batchSize).build(),
                ctx -> createRowExecutor(tableName, pojoClz, ctx),
                BatchingOutputFormat.RecordExtractor.identity());
    }

    private static <T> JdbcBatchStatementExecutor<T> createRowExecutor(String tableName, Class<T> pojoClz, RuntimeContext ctx) {

        JdbcPojoStatementBuilder<T> upsertStatementBuilder = new JdbcPojoStatementBuilder<>(pojoClz);

        PojoTypes<T> pojoTypes = PojoTypes.of(pojoClz);

        String upsertSql = getUpsertStatement(tableName, pojoTypes.fieldNames(), pojoTypes.getKeyFields());

        LOG.info("Upsert sql: {}", upsertSql);

        return new UpsertJdbcExecutor<>(
                upsertSql,
                upsertStatementBuilder,
                Function.identity(), Function.identity());
    }


    /**
     * Mysql upsert query use DUPLICATE KEY UPDATE.
     *
     * <p>NOTE: It requires Mysql's primary key to be consistent with pkFields.
     *
     * <p>We don't use REPLACE INTO, if there are other fields, we can keep their previous values.
     */
    private static String getUpsertStatement(String tableName, String[] fieldNames, String[] uniqueKeyFields) {

        String updateClause = Arrays.stream(fieldNames)
                .filter(n -> !Arrays.asList(uniqueKeyFields).contains(n))
                .map(f -> f + "= :" + f)
                .collect(Collectors.joining(", "));
        return getInsertIntoStatement(tableName, fieldNames) + " ON DUPLICATE KEY UPDATE " + updateClause;
    }

    /**
     * Get insert into statement.
     */
    private static String getInsertIntoStatement(String tableName, String[] fieldNames) {
        String columns = Arrays.stream(fieldNames)
//                .map(this.dialect::quoteIdentifier)
                .collect(Collectors.joining(", "));
        String placeholders = Arrays.stream(fieldNames)
                .map(f -> ":" + f)
                .collect(Collectors.joining(", "));

        return "INSERT INTO " + tableName +
                "(" + columns + ")" + " VALUES (" + placeholders + ")";
    }

    private static String getDeleteStatement(String tableName, String[] conditionFields) {
        if (conditionFields.length <= 0) {
            return "";
        }

        String conditionClause = Arrays.stream(conditionFields)
                .map(f -> f + "=:" + f)
                .collect(Collectors.joining(" AND "));

        return "DELETE FROM " + tableName + " WHERE " + conditionClause;
    }

    /**
     * Builder for {@link JdbcOutputFormat}.
     */
    public static class JdbcTableOutputFormatBuilder<T> {
        private String username;
        private String password;
        private String driverName;
        private String dbURL;
        private String tableName;
        private Class<T> pojoClass;
        private int batchSize = 1;
        private int batchInterval = 100;

        public JdbcTableOutputFormatBuilder(Class<T> pojoClass) {
            this.pojoClass = pojoClass;
        }

        public JdbcTableOutputFormatBuilder<T> setUsername(String username) {
            this.username = username;
            return this;
        }

        public JdbcTableOutputFormatBuilder<T> setPassword(String password) {
            this.password = password;
            return this;
        }

        public JdbcTableOutputFormatBuilder<T> setDriverName(String driverName) {
            this.driverName = driverName;
            return this;
        }

        public JdbcTableOutputFormatBuilder<T> setDBUrl(String dbURL) {
            this.dbURL = dbURL;
            return this;
        }

        public JdbcTableOutputFormatBuilder<T> setBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public JdbcTableOutputFormatBuilder<T> setBatchInterval(int batchInterval) {
            this.batchInterval = batchInterval;
            return this;
        }

        public JdbcTableOutputFormatBuilder<T> setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        /**
         * Finalizes the configuration and checks validity.
         *
         * @return Configured JdbcType2DimTableOutputFormat
         */
        public JdbcTableOutputFormat<T> finish() {
            Objects.requireNonNull(this.tableName, "tableName is required!");
            Objects.requireNonNull(this.pojoClass, "POJO class is required!");

            //JdbcDialect dialect = JdbcDialects.get(this.dbURL).orElseThrow(() -> new RuntimeException("Cannot get a usable JdbcDialet"));

            return new JdbcTableOutputFormat<>(tableName, pojoClass,
                    new JdbcConnectionProvider(buildConnectionOptions()), batchSize, batchInterval);
        }

        public JdbcConnectionProvider.JdbcConnectionOptions buildConnectionOptions() {
            if (this.username == null) {
                LOG.info("Username was not supplied.");
            }
            if (this.password == null) {
                LOG.info("Password was not supplied.");
            }

            return new JdbcConnectionProvider.JdbcConnectionOptionsBuilder()
                    .withUrl(dbURL)
                    .withDriverName(driverName)
                    .withUsername(username)
                    .withPassword(password)
                    .build();
        }
    }

}
