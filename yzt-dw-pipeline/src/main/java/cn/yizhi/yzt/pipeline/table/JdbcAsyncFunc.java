package cn.yizhi.yzt.pipeline.table;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jdbc.NamedParameterStatement;
import cn.yizhi.yzt.pipeline.jdbc.PojoTypes;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcConnectionProvider;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.curator5.com.google.common.util.concurrent.ListenableFuture;
import org.apache.flink.shaded.curator5.com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.flink.shaded.curator5.com.google.common.util.concurrent.MoreExecutors;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author zjzhang
 */
public class JdbcAsyncFunc<L, R, OUT> extends RichAsyncFunction<L, OUT> {
    private static final Logger log = LoggerFactory.getLogger(JdbcAsyncFunc.class);

    private static final int CONNECTION_CHECK_TIMEOUT_SECONDS = 60;

    private transient JdbcConnectionProvider connectionProvider;
    private transient Connection connection;
    private NamedParameterStatement statement;

    private String jdbcTableName;
    private Class<R> rightTableClass;
    private Class<OUT> outputClass;
    private PojoTypes<R> pojoTypes;
    private ServerConfig config;
    private ExecutorService executor;
    private ListeningExecutorService listenableExecutor;

    private WhereClauseBuilder<L> whereClauseBuilder;
    private ResultBuilder<L, R, OUT> resultBuilder;

    public JdbcAsyncFunc(ServerConfig config, String jdbcTable, Class<R> rightTableClass, Class<OUT> outputClass) {
        this.jdbcTableName = jdbcTable;
        this.outputClass = outputClass;
        this.rightTableClass = rightTableClass;
        this.pojoTypes = PojoTypes.of(rightTableClass);
        this.config = config;
        this.executor = Executors.newCachedThreadPool();
        this.listenableExecutor = MoreExecutors.listeningDecorator(this.executor);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        log.info("Config from Async Function: {}", parameters.toMap());

        JdbcConnectionProvider.JdbcConnectionOptions connectionOptions = new JdbcConnectionProvider.JdbcConnectionOptionsBuilder()
                .withUrl(config.getJdbcDBUrl())
                .withDriverName("com.mysql.cj.jdbc.Driver")
                .withUsername(config.getJdbcUsername())
                .withPassword(config.getJdbcPassword())
                .build();

        this.connectionProvider = new JdbcConnectionProvider(connectionOptions);

        try {
            establishConnection();
        } catch (Exception e) {
            throw new IOException("unable to open JDBC writer", e);
        }
    }

    private void establishConnection() throws Exception {
        connection = connectionProvider.getConnection();
    }

    public void prepareStatements(String sql) throws SQLException {
        statement = new NamedParameterStatement(connection, sql, false);

    }

    public void setWhereClauseBuilder(WhereClauseBuilder<L> builder) {
        this.whereClauseBuilder = builder;
    }

    public void setResultBuilder(ResultBuilder<L, R, OUT> resultBuilder) {
        this.resultBuilder = resultBuilder;
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException se) {
                log.warn("JDBC connection could not be closed: " + se.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    @Override
    public void asyncInvoke(L input, ResultFuture<OUT> resultFuture) throws Exception {
        if (this.connection == null) {
            establishConnection();
        }

        if (this.connection.isValid(CONNECTION_CHECK_TIMEOUT_SECONDS)) {
            this.connection = this.connectionProvider.reestablishConnection();
        }

        System.out.print("input: ");
        System.out.println(input);

        if (statement == null) {
            this.prepareStatements(getBaseQueryStatement(whereClauseBuilder.apply(input)));
        }

        ListenableFuture<ResultSet> asyncResult = listenableExecutor.submit(() -> statement.executeQuery());

        asyncResult.addListener(() -> {
            try {
                ResultSet resultSet = asyncResult.get();
                Collection<R> results = extractRecords(resultSet);

                resultFuture.complete(resultBuilder.apply(input, results));

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException | IOException e) {
                resultFuture.completeExceptionally(e);
            }

        }, this.executor);


    }

    @Override
    public void timeout(L input, ResultFuture<OUT> resultFuture) throws Exception {

    }

    private String getBaseQueryStatement(String whereClause) {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT ");

        for (Map.Entry<String, TypeInformation<?>> nextItem : pojoTypes.getFieldTypes().entrySet()) {
            buf.append(nextItem.getKey()).append(",");
        }

        buf.deleteCharAt(buf.lastIndexOf(","))
                .append(" FROM ")
                .append(this.jdbcTableName)
                .append(" WHERE ").append(whereClause);

        return buf.toString();
    }

    private Collection<R> extractRecords(ResultSet resultSet) throws IOException {
        Collection<R> results = new ArrayList<>();

        try {
            while (resultSet.next()) {
                Row row = new Row(pojoTypes.fieldNames().length);

                for (int pos = 0; pos < row.getArity(); pos++) {
                    row.setField(pos, resultSet.getObject(pos + 1));
                }

                results.add(pojoTypes.fromRow(row));
            }

            return results;
        } catch (SQLException se) {
            throw new IOException("Couldn't read data - " + se.getMessage(), se);
        } catch (NullPointerException npe) {
            throw new IOException("Couldn't access resultSet", npe);
        } catch (Exception e) {
            throw new IOException("Convert to row failed");
        }
    }


    public interface WhereClauseBuilder<L> extends Function<L, String>, Serializable {
    }

    public interface ResultBuilder<L, R, OUT> extends BiFunction<L,Collection<R>, Collection<OUT>>, Serializable {

    }


    public static class JdbcAsyncFuncBuilder<L, R, OUT> {
       private ServerConfig config;
       private String jdbcTable;
       private Class<R> rightTableClass;
       private Class<OUT> outputClass;
       private WhereClauseBuilder<L> clauseBuilder;
       private ResultBuilder<L, R, OUT> resultBuilder;

       public JdbcAsyncFuncBuilder<L, R, OUT> setConfig(ServerConfig config) {
           this.config = config;
           return this;
       }

       public JdbcAsyncFuncBuilder<L, R, OUT> setJdbcTable(String tableName) {
           this.jdbcTable = tableName;
           return this;
       }

        public JdbcAsyncFuncBuilder<L, R, OUT> setRightTableClass(Class<R> rightTableClass) {
            this.rightTableClass = rightTableClass;
            return this;
        }

        public JdbcAsyncFuncBuilder<L, R, OUT> setOutputClass(Class<OUT> outputClass) {
            this.outputClass = outputClass;
            return this;
        }

        public JdbcAsyncFuncBuilder<L, R, OUT> setWhereClauseBuilder(WhereClauseBuilder<L> clauseBuilder) {
            this.clauseBuilder = clauseBuilder;
            return this;
        }

        public JdbcAsyncFuncBuilder<L, R, OUT> setResultBuilder(ResultBuilder<L, R, OUT> resultBuilder) {
            this.resultBuilder = resultBuilder;
            return this;
        }

        public JdbcAsyncFunc<L, R, OUT> build() {
            JdbcAsyncFunc<L, R, OUT> func = new JdbcAsyncFunc<>(config, jdbcTable, rightTableClass, outputClass);
            func.setWhereClauseBuilder(clauseBuilder);
            func.setResultBuilder(resultBuilder);

            return func;
        }
    }

}
