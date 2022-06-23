package cn.yizhi.yzt.pipeline.jdbc.sink;


import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcBatchStatementExecutor<T> {

    /**
     * Create statements from connection.
     */
    void prepareStatements(Connection connection) throws SQLException;

    void addToBatch(T record) throws SQLException;

    /**
     * Submits a batch of commands to the database for execution.
     */
    void executeBatch() throws SQLException;

    /**
     * Close JDBC related statements.
     */
    void closeStatements() throws SQLException;
}