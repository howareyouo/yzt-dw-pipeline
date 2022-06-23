package cn.yizhi.yzt.pipeline.jdbc;


import cn.yizhi.yzt.pipeline.jdbc.sink.BatchingOutputFormat;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcBatchStatementExecutor;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcConnectionProvider;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.internal.JdbcOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * JDBC outputFormat for Type 2 Slowly Change Dimension(SCD) table
 */
public class JdbcType2DimTableOutputFormat<T> extends BatchingOutputFormat<T, T, JdbcBatchStatementExecutor<T>> {
    private static final long serialVersionUID = 1999L;

    private static final String START_DATE_FIELD = "start_date";
    private static final String END_DATE_FIELD = "end_date";

    private static final Logger LOG = LoggerFactory.getLogger(JdbcType2DimTableOutputFormat.class);

    private JdbcType2DimTableOutputFormat(String tableName, Class<T> pojoClz, JdbcConnectionProvider connectionProvider,
                                          int batchSize, int batchInterval) {
        super(
                connectionProvider,
                new JdbcExecutionOptions.Builder().withBatchSize(batchSize).withBatchIntervalMs(batchInterval).build(),
                ctx -> createRowExecutor(tableName, pojoClz, ctx),
                BatchingOutputFormat.RecordExtractor.identity());
    }

    private static <T> JdbcBatchStatementExecutor<T> createRowExecutor(String tableName, Class<T> pojoClz, RuntimeContext ctx) {

        JdbcPojoStatementBuilder<T> insertStatementBuilder = new JdbcPojoStatementBuilder<>(pojoClz);
        JdbcPojoStatementBuilder<T> updateStatementBuilder = new JdbcPojoStatementBuilder<>(pojoClz);

        PojoTypes<T> pojoTypes = PojoTypes.of(pojoClz);

        List<String> fields = new ArrayList<>(Arrays.asList(pojoTypes.fieldNames()));
        fields.removeIf(field -> Arrays.asList(pojoTypes.getPkFields()).contains(field));

        String insertSql = getUpsertStatement(tableName, fields.toArray(new String[0]), pojoTypes.getKeyFields());
        String updateSql = getUpdateStatement(tableName, pojoTypes.getKeyFields());

        LOG.info("insert sql: {}", insertSql);
        LOG.info("Update sql: {}", updateSql);

        return new InsertAndUpdateJdbcExecutor<>(
                insertSql, updateSql,
                insertStatementBuilder,
                updateStatementBuilder,
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
        List<String> uniqueFieldList = new ArrayList<>(Arrays.asList(uniqueKeyFields));
        if (!uniqueFieldList.contains(START_DATE_FIELD)) {
            uniqueFieldList.add(START_DATE_FIELD);
        }

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

    /**
     * Get update one row statement by condition fields, default not use limit 1,
     * because limit 1 is a sql dialect.
     */
    private static String getUpdateStatement(String tableName, String[] conditionFields) {
        String setClause = END_DATE_FIELD + " = current_date()";

        String conditionClause = Arrays.stream(conditionFields)
                .map(f -> f + "=:" + f)
                .collect(Collectors.joining(" AND "));
        return "UPDATE " + tableName +
                " SET " + setClause +
                " WHERE " + conditionClause + " AND " + START_DATE_FIELD + " < current_date() " +
                " AND " + END_DATE_FIELD + " = '9999-12-31' ";
    }

    private static String getDeleteStatement(String tableName, String[] conditionFields) {
        String conditionClause = Arrays.stream(conditionFields)
                .map(f -> f + "=:" + f)
                .collect(Collectors.joining(" AND "));
        conditionClause = conditionClause + " AND " + START_DATE_FIELD + "=current_date()";

        return "DELETE FROM " + tableName + " WHERE " + conditionClause;
    }


//    public static JdbcType2DimTableOutputFormatBuilder buildJdbcOutputFormat() {
//        return new JdbcType2DimTableOutputFormatBuilder();
//    }

    /**
     * Builder for {@link JdbcOutputFormat}.
     */
    public static class JdbcType2DimTableOutputFormatBuilder<T> {
        private String username;
        private String password;
        private String drivername;
        private String dbURL;
        private String tableName;
        private Class<T> pojoClass;
        private int batchSize = JdbcExecutionOptions.DEFAULT_SIZE;
        private int batchInterval = 3000;

//        private int[] typesArray;

        public JdbcType2DimTableOutputFormatBuilder(Class<T> pojoClass) {
            this.pojoClass = pojoClass;
        }

        public JdbcType2DimTableOutputFormatBuilder<T> setUsername(String username) {
            this.username = username;
            return this;
        }

        public JdbcType2DimTableOutputFormatBuilder<T> setPassword(String password) {
            this.password = password;
            return this;
        }

        public JdbcType2DimTableOutputFormatBuilder<T> setDrivername(String drivername) {
            this.drivername = drivername;
            return this;
        }

        public JdbcType2DimTableOutputFormatBuilder<T> setDBUrl(String dbURL) {
            this.dbURL = dbURL;
            return this;
        }

        public JdbcType2DimTableOutputFormatBuilder<T> setBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public JdbcType2DimTableOutputFormatBuilder<T> setTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public JdbcType2DimTableOutputFormatBuilder<T> setBatchInterval(int batchInterval) {
            this.batchInterval = batchInterval;
            return this;
        }

        /**
         * Finalizes the configuration and checks validity.
         *
         * @return Configured JdbcType2DimTableOutputFormat
         */
        public JdbcType2DimTableOutputFormat<T> finish() {
            Objects.requireNonNull(this.tableName, "tableName is required!");
            Objects.requireNonNull(this.pojoClass, "POJO class is required!");

            return new JdbcType2DimTableOutputFormat<T>(tableName, pojoClass,
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
                    .withDriverName(drivername)
                    .withUsername(username)
                    .withPassword(password)
                    .build();
        }
    }
}
