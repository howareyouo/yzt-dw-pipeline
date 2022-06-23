package cn.yizhi.yzt.pipeline.jdbc;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class JdbcClient {
    private DataSource dataSource;

    public JdbcClient(ServerConfig config) {
        this.dataSource = setup(config);
    }

    private DataSource setup(ServerConfig config) {
        MysqlDataSource ds = new MysqlConnectionPoolDataSource();

        ds.setURL(config.getJdbcDBUrl());
        ds.setUser(config.getJdbcUsername());
        ds.setPassword(config.getJdbcPassword());

        return ds;
    }

    public <T> List<T> query(String query, Object[] params, Class<T> resultClass) {
        try {
            QueryRunner runner = new QueryRunner(dataSource);

            ResultSetHandler<List<T>> resultSetHandler = new BeanListHandler<T>(resultClass);

            return runner.query(query, resultSetHandler, params);

        }catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
