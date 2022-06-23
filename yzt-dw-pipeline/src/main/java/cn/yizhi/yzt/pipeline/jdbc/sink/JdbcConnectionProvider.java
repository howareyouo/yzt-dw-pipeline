package cn.yizhi.yzt.pipeline.jdbc.sink;

import org.apache.flink.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;


public class JdbcConnectionProvider implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcConnectionProvider.class);

    private static final long serialVersionUID = 1L;

    private final JdbcConnectionOptions jdbcOptions;

    private transient volatile Connection connection;

    public JdbcConnectionProvider(JdbcConnectionOptions jdbcOptions) {
        this.jdbcOptions = jdbcOptions;
    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        if (connection == null) {
            synchronized (this) {
                if (connection == null) {
                    Class.forName(jdbcOptions.getDriverName());
                    if (jdbcOptions.getUsername().isPresent()) {
                        connection = DriverManager.getConnection(jdbcOptions.getDbURL(), jdbcOptions.getUsername().get(), jdbcOptions.getPassword().orElse(null));
                    } else {
                        connection = DriverManager.getConnection(jdbcOptions.getDbURL());
                    }
                }
            }
        }
        return connection;
    }

    public Connection reestablishConnection() throws SQLException, ClassNotFoundException {
        try {
            connection.close();
        } catch (SQLException e) {
            LOG.info("JDBC connection close failed.", e);
        } finally {
            connection = null;
        }
        connection = getConnection();
        return connection;
    }

    public static class JdbcConnectionOptions implements Serializable {

        private static final long serialVersionUID = 1L;

        protected final String url;
        protected final String driverName;
        @Nullable
        protected final String username;
        @Nullable
        protected final String password;

        protected JdbcConnectionOptions(String url, String driverName, String username, String password) {
            this.url = Preconditions.checkNotNull(url, "jdbc url is empty");
            this.driverName = Preconditions.checkNotNull(driverName, "driver name is empty");
            this.username = username;
            this.password = password;
        }

        public String getDbURL() {
            return url;
        }

        public String getDriverName() {
            return driverName;
        }

        public Optional<String> getUsername() {
            return Optional.ofNullable(username);
        }

        public Optional<String> getPassword() {
            return Optional.ofNullable(password);
        }

    }

    public static class JdbcConnectionOptionsBuilder {
        private String url;
        private String driverName;
        private String username;
        private String password;

        public JdbcConnectionOptionsBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public JdbcConnectionOptionsBuilder withDriverName(String driverName) {
            this.driverName = driverName;
            return this;
        }

        public JdbcConnectionOptionsBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public JdbcConnectionOptionsBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public JdbcConnectionOptions build() {
            return new JdbcConnectionOptions(url, driverName, username, password);
        }
    }
}

