package cn.yizhi.yzt.pipeline.jdbc;

import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcBatchStatementExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.apache.flink.util.Preconditions.checkNotNull;


/**
 * {@link JdbcBatchStatementExecutor} that update the Type 2 Slowly Change Dimension(SCD) table in data warehouse.
 */
public final class InsertAndUpdateJdbcExecutor<R, K, V> implements JdbcBatchStatementExecutor<R> {

    private static final Logger LOG = LoggerFactory.getLogger(org.apache.flink.connector.jdbc.internal.executor.InsertOrUpdateJdbcExecutor.class);

    private final String insertSQL;
    private final String updateSQL;

    private final JdbcPojoStatementBuilder<V> insertSetter;
    private final JdbcPojoStatementBuilder<V> updateSetter;

    private final Function<R, K> keyExtractor;
    private final Function<R, V> valueMapper;

    private final Map<K, V> batch;

    private transient NamedParameterStatement insertStatement;
    private transient NamedParameterStatement updateStatement;

    public InsertAndUpdateJdbcExecutor(
            @Nonnull String insertSQL,
            @Nonnull String updateSQL,
            @Nonnull JdbcPojoStatementBuilder<V> insertSetter,
            @Nonnull JdbcPojoStatementBuilder<V> updateSetter,
            @Nonnull Function<R, K> keyExtractor,
            @Nonnull Function<R, V> valueExtractor) {
        this.insertSQL = checkNotNull(insertSQL);
        this.updateSQL = checkNotNull(updateSQL);
        this.insertSetter = checkNotNull(insertSetter);
        this.updateSetter = checkNotNull(updateSetter);
        this.keyExtractor = checkNotNull(keyExtractor);
        this.valueMapper = checkNotNull(valueExtractor);
        this.batch = new HashMap<>();
    }

    @Override
    public void prepareStatements(Connection connection) throws SQLException {
        insertStatement = new NamedParameterStatement(connection, insertSQL, false);
        updateStatement = new NamedParameterStatement(connection, updateSQL, false);
    }

    @Override
    public void addToBatch(R record) {
        batch.put(keyExtractor.apply(record), valueMapper.apply(record));
    }

    @Override
    public void executeBatch() throws SQLException {
        if (!batch.isEmpty()) {
            for (Map.Entry<K, V> entry : batch.entrySet()) {
                processOneRowInBatch(entry.getKey(), entry.getValue());
            }
            insertStatement.executeBatch();
            updateStatement.executeBatch();
            batch.clear();
        }
    }

    private void processOneRowInBatch(K pk, V row) throws SQLException {
        insertSetter.accept(insertStatement, row);
        insertStatement.addBatch();
        // and then update the last row
        updateSetter.accept(updateStatement, row);
        updateStatement.addBatch();
    }

//    private boolean exist(K pk) throws SQLException {
//        existSetter.accept(existStatement, pk);
//        try (ResultSet resultSet = existStatement.executeQuery()) {
//            return resultSet.next();
//        }
//    }

    @Override
    public void closeStatements() throws SQLException {
        for (NamedParameterStatement s : Arrays.asList(insertStatement, updateStatement)) {
            if (s != null) {
                s.close();
            }
        }
    }
}
