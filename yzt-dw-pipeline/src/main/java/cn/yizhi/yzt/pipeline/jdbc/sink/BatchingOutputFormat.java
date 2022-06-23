package cn.yizhi.yzt.pipeline.jdbc.sink;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.io.RichOutputFormat;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.util.concurrent.ExecutorThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.apache.flink.util.Preconditions.checkNotNull;


/**
 * A JDBC outputFormat that supports batching records before writing records to database.
 */
public class BatchingOutputFormat<In, JdbcIn, JdbcExec extends JdbcBatchStatementExecutor<JdbcIn>>
    extends RichOutputFormat<In> implements Flushable {
    protected final int CONNECTION_CHECK_TIMEOUT_SECONDS = 60;

    private static final long serialVersionUID = 1L;
    //    public static final int DEFAULT_FLUSH_MAX_SIZE = 5000;
    //    public static final long DEFAULT_FLUSH_INTERVAL_MILLS = 0L;

    /**
     * An interface to extract a value from given argument.
     *
     * @param <F> The type of given argument
     * @param <T> The type of the return value
     */
    public interface RecordExtractor<F, T> extends Function<F, T>, Serializable {
        static <T> RecordExtractor<T, T> identity() {
            return x -> x;
        }
    }

    /**
     * A factory for creating {@link JdbcBatchStatementExecutor} instance.
     *
     * @param <T> The type of instance.
     */
    public interface StatementExecutorFactory<T extends JdbcBatchStatementExecutor<?>> extends Function<RuntimeContext, T>, Serializable {
    }


    private static final Logger LOG = LoggerFactory.getLogger(BatchingOutputFormat.class);

    protected transient Connection connection;
    protected final JdbcConnectionProvider connectionProvider;

    private final JdbcExecutionOptions executionOptions;
    private final StatementExecutorFactory<JdbcExec> statementExecutorFactory;
    private final RecordExtractor<In, JdbcIn> jdbcRecordExtractor;

    private transient JdbcExec jdbcStatementExecutor;
    private transient int batchCount = 0;
    private transient volatile boolean closed = false;

    private transient ScheduledExecutorService scheduler;
    private transient ScheduledFuture<?> scheduledFuture;
    private transient volatile Exception flushException;

    public BatchingOutputFormat(
        @Nonnull JdbcConnectionProvider connectionProvider,
        @Nonnull JdbcExecutionOptions executionOptions,
        @Nonnull StatementExecutorFactory<JdbcExec> statementExecutorFactory,
        @Nonnull RecordExtractor<In, JdbcIn> recordExtractor) {
        this.connectionProvider = connectionProvider;
        this.executionOptions = checkNotNull(executionOptions);
        this.statementExecutorFactory = checkNotNull(statementExecutorFactory);
        this.jdbcRecordExtractor = checkNotNull(recordExtractor);
    }


    @Override
    public void configure(Configuration parameters) {
    }

    protected void establishConnection() throws Exception {
        connection = connectionProvider.getConnection();
    }


    /**
     * Connects to the target database and initializes the prepared statement.
     *
     * @param taskNumber The number of the parallel instance.
     */
    @Override
    public void open(int taskNumber, int numTasks) throws IOException {
        try {
            establishConnection();
        } catch (Exception e) {
            throw new IOException("unable to open JDBC writer", e);
        }

        jdbcStatementExecutor = createAndOpenStatementExecutor(statementExecutorFactory);
        if (executionOptions.getBatchIntervalMs() != 0 && executionOptions.getBatchSize() != 1) {
            this.scheduler = Executors.newScheduledThreadPool(1, new ExecutorThreadFactory("jdbc-upsert-output-format"));
            this.scheduledFuture = this.scheduler.scheduleWithFixedDelay(() -> {
                synchronized (BatchingOutputFormat.this) {
                    if (!closed) {
                        try {
                            flush();
                        } catch (Exception e) {
                            flushException = e;
                        }
                    }
                }
            }, executionOptions.getBatchIntervalMs(), executionOptions.getBatchIntervalMs(), TimeUnit.MILLISECONDS);
        }
    }

    private JdbcExec createAndOpenStatementExecutor(BatchingOutputFormat.StatementExecutorFactory<JdbcExec> statementExecutorFactory) throws IOException {
        JdbcExec exec = statementExecutorFactory.apply(getRuntimeContext());
        try {
            exec.prepareStatements(connection);
        } catch (SQLException e) {
            throw new IOException("unable to open JDBC writer", e);
        }
        return exec;
    }

    private void checkFlushException() {
        if (flushException != null) {
            throw new RuntimeException("Writing records to JDBC failed.", flushException);
        }
    }

    @Override
    public final synchronized void writeRecord(In record) throws IOException {
        checkFlushException();

        try {
            addToBatch(record, jdbcRecordExtractor.apply(record));
            batchCount++;
            if (batchCount >= executionOptions.getBatchSize()) {
                flush();
            }
        } catch (Exception e) {
            throw new IOException("Writing records to JDBC failed.", e);
        }
    }

    protected void addToBatch(In original, JdbcIn extracted) throws SQLException {
        jdbcStatementExecutor.addToBatch(extracted);
    }

    @Override
    public synchronized void flush() throws IOException {
        checkFlushException();

        for (int i = 1; i <= executionOptions.getMaxRetries(); i++) {
            try {
                attemptFlush();
                batchCount = 0;
                break;
            } catch (SQLException e) {
                LOG.error("JDBC executeBatch error, retry times = {}", i, e);
                if (i >= executionOptions.getMaxRetries()) {
                    throw new IOException(e);
                }
                try {
                    if (!connection.isValid(CONNECTION_CHECK_TIMEOUT_SECONDS)) {
                        connection = connectionProvider.reestablishConnection();
                        jdbcStatementExecutor.closeStatements();
                        jdbcStatementExecutor.prepareStatements(connection);
                    }
                } catch (Exception excpetion) {
                    LOG.error("JDBC connection is not valid, and reestablish connection failed.", excpetion);
                    throw new IOException("Reestablish JDBC connection failed", excpetion);
                }
                try {
                    Thread.sleep(1000 * i);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException("unable to flush; interrupted while doing another attempt", e);
                }
            }
        }
    }

    protected void attemptFlush() throws SQLException {
        jdbcStatementExecutor.executeBatch();
    }

    /**
     * Executes prepared statement and closes all resources of this instance.
     */
    @Override
    public synchronized void close() {
        if (!closed) {
            closed = true;

            checkFlushException();

            if (this.scheduledFuture != null) {
                scheduledFuture.cancel(false);
                this.scheduler.shutdown();
            }

            if (batchCount > 0) {
                try {
                    flush();
                } catch (Exception e) {
                    throw new RuntimeException("Writing records to JDBC failed.", e);
                }
            }

            try {
                if (jdbcStatementExecutor != null) {
                    jdbcStatementExecutor.closeStatements();
                }
            } catch (SQLException e) {
                LOG.warn("Close JDBC writer failed.", e);
            }
        }

        closeDbConnection();
    }

    private void closeDbConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException se) {
                LOG.warn("JDBC connection could not be closed: " + se.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
