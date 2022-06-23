package cn.yizhi.yzt.pipeline.config;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.runtime.state.StateBackendLoader;
import org.apache.flink.streaming.connectors.kafka.config.StartupMode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.Serializable;
import java.util.Objects;
import java.util.Properties;

public class ServerConfig implements Serializable {
    static final long serialVersionUID = 4676L;

    /**
     * flink Job name
     */
    public static String jobName = "jobName";

    /**
     * kafka bootstrap servers
     */
    public static String bootstrapServers = "kafka.bootstrapServers";

    /**
     * kafka 事务超时时间，单位：毫秒
     */
    public static String kafkaTransactionTimeout = "kafka.transactionTimeout";


    /**
     * zookeeper connects
     */
    public static String zookeeperConnects = "zookeeper.connectors";

    /**
     * 是否replay
     */
    public static String kafkaStartupMode = "kafka.startupMode";


    public static String parallelism = "flink.parallelism";
    public static String flinkStateStore = "flink.stateStore.path";

    public static String kafkadbTopicPrefix = "kafka.dbTopicPrefix";

    public static String kafkaAppId = "kafka.appId";

    /**
     * JDBC Connector username
     */
    public static String jdbcUsername = "jdbc.user";

    /**
     * JDBC Connector password
     */
    public static String jdbcPassword = "jdbc.password";

    /**
     * JDBC Connector database url, e.g., jdbc:mysql://localhost:3306/flink-test
     */
    public static String jdbcDBUrl = "jdbc.url";

    //mysql维度重新加载时间
    private static String reloadInterval = "jdbc.reloadInterval";

    /**
     * 批量写入DB的记录数量
     */
    public static String jdbcBatchSize = "jdbc.batchSize";
    /**
     * 每间隔固定时间刷新一次缓存的数据到DB， 单位: ms
     */
    public static String jdbcBatchInterval = "jdbc.batchInterval";

    /**
     * table config min_time
     */
    public static String tableConfigMinTime = "tableConfig.minTime";

    /**
     * table config max_time
     */
    public static String tableConfigMaxTime = "tableConfig.maxTime";


    //数据迁移时间配置
    public static String endHour = "endHour";
    public static String endDay = "endDay";
    public static String endMonth = "endMonth";


    //指定起始时间消费kafka数据
    public static String startTimeStamp = "flink.startTimeStamp";

    //会员高级筛选是否计算数据
    public static String memberGroupOpenCalculate = "memberGroup.openCalculate";

    private ParameterTool params;

    public ServerConfig(ParameterTool params) {
        this.params = params;
    }

    public ParameterTool getParams() {
        return this.params;
    }

    public String getJobName() {
        String flinkJobName = params.get(jobName);
        Objects.requireNonNull(flinkJobName, "jobName is required!");

        return flinkJobName;
    }

    public String getBootstrapServers() {
        return params.get(bootstrapServers, "localhost:9092");
    }

    public String getKafkaStartupMode() {
        return params.get(kafkaStartupMode, StartupMode.GROUP_OFFSETS.name());
    }

    public String getZookeeperConnects() {
        return params.get(zookeeperConnects, "localhost:2181");
    }

    public Integer getReloadInterval() {
        return params.getInt(reloadInterval);
    }

    public int getParallelism() {
        return params.getInt(parallelism, 1);
    }

    public String getFlinkStateStore() {
        return params.get(flinkStateStore, "file:///data/checkpoints");
    }

    public String getKafkadbTopicPrefix() {
        return params.get(kafkadbTopicPrefix, "yzt-mysql.");
    }

    public int getKafkaTransactionTimeout() {
        return params.getInt(kafkaTransactionTimeout, 5 * 60000);
    }

    public String getKafkaAppId() {
        return params.get(kafkaAppId, "flink-db-pipeline");
    }

    public String getJdbcDBUrl() {
        String dbUrl = params.get(jdbcDBUrl);
        Objects.requireNonNull(dbUrl, "JDBC DB URL is required!");
        return dbUrl;
    }

    public String getJdbcUsername() {
        return params.get(jdbcUsername);
    }

    public String getJdbcPassword() {
        return params.get(jdbcPassword);
    }

    public Integer getJdbcBatchSize() {
        return params.getInt(jdbcBatchSize, 300);
    }

    public Integer getJdbcBatchInterval() {
        return params.getInt(jdbcBatchInterval, 3000);
    }

    public Integer getTableConfigMinTime() {
        return params.getInt(tableConfigMinTime, 12);
    }

    public Integer getTableConfigMaxTime() {
        return params.getInt(tableConfigMaxTime, 24);
    }

    public String getEndHour() {
        return params.get(endHour);
    }

    public String getEndDay() {
        return params.get(endDay);
    }

    public String getEndMonth() {
        return params.get(endMonth);
    }

    public Long getStartTimeStamp() {
        return params.getLong(startTimeStamp, 0);
    }


    public Properties buildKafkaConsumerConfig() {
        final Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.getJobName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "5000");
//        props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, "cn.yizhi.yzt.pipeline.kafka.KafkaInterceptor");
//        props.put(FlinkKafkaConsumer.KEY_POLL_TIMEOUT, this.params.get(FlinkKafkaConsumer.KEY_POLL_TIMEOUT, "1000"));

        return props;
    }

    public Properties buildKafkaProducerConfig() {
        final Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
        //props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, "cn.yizhi.yzt.pipeline.kafka.KafkaProducerInterceptor");
        props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, this.getKafkaTransactionTimeout());
        // 设置了retries参数，可以在Kafka的Partition发生leader切换时，Flink不重启，而是做5次尝试：
        props.put(ProducerConfig.RETRIES_CONFIG, "5");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        return props;
    }

    public Configuration checkpointStateConfig() {
        // set state backend via flink-conf.yaml
        Configuration stateConfig = new Configuration();
        stateConfig.setString(StateBackendOptions.STATE_BACKEND, StateBackendLoader.ROCKSDB_STATE_BACKEND_NAME);
        stateConfig.setString(CheckpointingOptions.CHECKPOINTS_DIRECTORY, this.getFlinkStateStore());
        stateConfig.setBoolean(CheckpointingOptions.INCREMENTAL_CHECKPOINTS, true);

        return stateConfig;
    }

    public String getMemberGroupOpenCalculate() {
        return params.get(memberGroupOpenCalculate, "true");
    }


}
