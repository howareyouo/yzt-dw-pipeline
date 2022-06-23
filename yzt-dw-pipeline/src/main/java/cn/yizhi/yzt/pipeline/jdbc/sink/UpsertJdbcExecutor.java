//package cn.yizhi.yzt.pipeline.jdbc.sink;
//
//import cn.yizhi.yzt.pipeline.jdbc.JdbcNamedStatementBuilder;
//import cn.yizhi.yzt.pipeline.jdbc.NamedParameterStatement;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.Nonnull;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//import static org.apache.flink.util.Preconditions.checkNotNull;
//
//
//
///**
// * @author zjzhang
// */
//public final class UpsertJdbcExecutor<R, K, V> implements JdbcBatchStatementExecutor<R> {
//
//    private static final Logger LOG = LoggerFactory.getLogger(UpsertJdbcExecutor.class);
//
//    private final String deleteSQL;
//    private final String insertSQL;
//
//    private final JdbcNamedStatementBuilder<V> deleteSetter;
//    private final JdbcNamedStatementBuilder<V> insertSetter;
//
//    private final Function<R, K> keyExtractor;
//    private final Function<R, V> valueMapper;
//
//    private final Map<K, V> batch;
//
//    private transient NamedParameterStatement deleteStatement;
//    private transient NamedParameterStatement insertStatement;
//
//    public UpsertJdbcExecutor(
//            @Nonnull String deleteSQL,
//            @Nonnull String insertSQL,
//            @Nonnull JdbcNamedStatementBuilder<V> deleteSetter,
//            @Nonnull JdbcNamedStatementBuilder<V> insertSetter,
//            @Nonnull Function<R, K> keyExtractor,
//            @Nonnull Function<R, V> valueExtractor) {
//
//        System.out.println("删除的sql："+deleteSQL);
//        System.out.println("添加的sql："+insertSQL);
//
//        this.deleteSQL = checkNotNull(deleteSQL);
//        this.insertSQL = checkNotNull(insertSQL);
//        this.deleteSetter = checkNotNull(deleteSetter);
//        this.insertSetter = checkNotNull(insertSetter);
//
//        this.keyExtractor = checkNotNull(keyExtractor);
//        this.valueMapper = checkNotNull(valueExtractor);
//        this.batch = new HashMap<>();
//    }
//
//    @Override
//    public void prepareStatements(Connection connection) throws SQLException {
//        if (!deleteSQL.isEmpty()) {
//            deleteStatement = new NamedParameterStatement(connection, deleteSQL, false);
//        }
//
//        insertStatement = new NamedParameterStatement(connection, insertSQL, false);
//    }
//
//    @Override
//    public void addToBatch(R record) {
//        batch.put(keyExtractor.apply(record), valueMapper.apply(record));
//    }
//
//    @Override
//    public void executeBatch() throws SQLException {
//        if (!batch.isEmpty()) {
//            for (Map.Entry<K, V> entry : batch.entrySet()) {
//                processOneRowInBatch(entry.getKey(), entry.getValue());
//            }
//
//            if (deleteStatement != null) {
//                deleteStatement.executeBatch();
//            }
//
//            insertStatement.executeBatch();
//            batch.clear();
//        }
//    }
//
//    private void processOneRowInBatch(K pk, V row) throws SQLException {
//        if (deleteStatement != null) {
//            deleteSetter.accept(deleteStatement, row);
//            deleteStatement.addBatch();
//        }
//
//        insertSetter.accept(insertStatement, row);
//        insertStatement.addBatch();
//    }
//
//    @Override
//    public void closeStatements() throws SQLException {
//        if (deleteStatement != null) {
//            deleteStatement.close();
//        }
//
//        if (insertStatement != null) {
//            insertStatement.close();
//        }
//    }
//}
package cn.yizhi.yzt.pipeline.jdbc.sink;

import cn.yizhi.yzt.pipeline.jdbc.JdbcPojoStatementBuilder;
import cn.yizhi.yzt.pipeline.jdbc.NamedParameterStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.apache.flink.util.Preconditions.checkNotNull;



/**
 * @author zjzhang
 */
public final class UpsertJdbcExecutor<R, K, V> implements JdbcBatchStatementExecutor<R> {

    private static final Logger LOG = LoggerFactory.getLogger(UpsertJdbcExecutor.class);

    private final String upsertSQL;

    private final JdbcPojoStatementBuilder<V> upsertSetter;

    private final Function<R, K> keyExtractor;
    private final Function<R, V> valueMapper;

    private final Map<K, V> batch;

    private transient NamedParameterStatement upsertStatement;

    public UpsertJdbcExecutor(
            @Nonnull String upsertSQL,
            @Nonnull JdbcPojoStatementBuilder<V> upsertSetter,
            @Nonnull Function<R, K> keyExtractor,
            @Nonnull Function<R, V> valueExtractor) {

        this.upsertSQL = checkNotNull(upsertSQL);
        this.upsertSetter = checkNotNull(upsertSetter);

        this.keyExtractor = checkNotNull(keyExtractor);
        this.valueMapper = checkNotNull(valueExtractor);
        this.batch = new HashMap<>();
    }

    @Override
    public void prepareStatements(Connection connection) throws SQLException {
        upsertStatement = new NamedParameterStatement(connection, upsertSQL, false);;
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
            upsertStatement.executeBatch();
            batch.clear();
        }
    }

    private void processOneRowInBatch(K pk, V row) throws SQLException {
        upsertSetter.accept(upsertStatement, row);
        upsertStatement.addBatch();

    }

    @Override
    public void closeStatements() throws SQLException {
        if (upsertStatement != null) {
            upsertStatement.close();
        }
    }
}

