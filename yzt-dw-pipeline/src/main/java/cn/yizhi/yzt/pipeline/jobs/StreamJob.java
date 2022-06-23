package cn.yizhi.yzt.pipeline.jobs;


import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.config.SqlReader;
import cn.yizhi.yzt.pipeline.jdbc.Ignore;
import cn.yizhi.yzt.pipeline.jdbc.sink.JdbcSink;
import cn.yizhi.yzt.pipeline.kafka.DefaultKafkaSerializationSchema;
import cn.yizhi.yzt.pipeline.kafka.KafkaPojoDeserializationSchema;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.StateBackendLoader;
import org.apache.flink.shaded.guava30.com.google.common.base.CaseFormat;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableConfig;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stream Job runner
 */
public abstract class StreamJob implements FlinkJob {
    protected static Logger logger = LoggerFactory.getLogger(StreamJob.class);

    protected StreamExecutionEnvironment env;
    protected StreamTableEnvironment tableEnv;
    protected ServerConfig serverConfig;
    private SqlReader sqlReader;

    protected String jobDescription;

    @Override
    public void submitJob(String jobName, final ServerConfig serverConfig) throws Exception {
        // Checking input parameters
        this.serverConfig = serverConfig;

        // set up the execution environment
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        // make parameters available in the web interface
        env.getConfig().setGlobalJobParameters(serverConfig.getParams());
        env.setParallelism(serverConfig.getParallelism());

        //失败重启策略
        env.setRestartStrategy(RestartStrategies.failureRateRestart(100, Time.minutes(3), Time.seconds(15)));

        this.configCheckpoint();
        this.configTableEnv();

        this.runJob(jobName);
    }

    /**
     * 参考 https://nightlies.apache.org/flink/flink-docs-release-1.15/zh/docs/dev/datastream/fault-tolerance/checkpointing/
     */
    private void configCheckpoint() throws Exception {
        //开启checkpoint, 并设置消费的模式为仅有一次
        env.enableCheckpointing(20000, CheckpointingMode.EXACTLY_ONCE);

        CheckpointConfig checkpointConfig = env.getCheckpointConfig();

        // checkpoint之间的最小间隔时间
        checkpointConfig.setMinPauseBetweenCheckpoints(1000);

        // 最大并行checkpoint操作
        checkpointConfig.setMaxConcurrentCheckpoints(2);

        // 允许在有更近 savepoint 时回退到 checkpoint
        // env.getCheckpointConfig().setPreferCheckpointForRecovery(true);

        // job取消时，不删除checkpoint数据
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        checkpointConfig.setTolerableCheckpointFailureNumber(3);

        StateBackend stateBackend = StateBackendLoader.loadStateBackendFromConfig(
            serverConfig.checkpointStateConfig(),
            StreamJob.class.getClassLoader(),
            logger
        );
        env.setStateBackend(stateBackend);
    }

    private void configTableEnv() {
        EnvironmentSettings bsSettings = EnvironmentSettings.newInstance()
            .inStreamingMode()
            .build();

        this.tableEnv = StreamTableEnvironment.create(env, bsSettings);

        //用等参join的时候需设置数据保存时间，防止数据无限增长
        TableConfig tConfig = tableEnv.getConfig();

        //设置上海时区
        tConfig.setLocalTimeZone(ZoneId.of("Asia/Shanghai"));
        //tConfig.setIdleStateRetentionTime(Time.hours(serverConfig.getTableConfigMinTime()), Time.hours(serverConfig.getTableConfigMaxTime()));
    }

    protected <T> DataStream<T> createStreamFromKafka(String topic, Class<T> pojoClass) {
        this.env.registerType(pojoClass);

        //接受的消息包含元数据
        FlinkKafkaConsumer<T> flinkConsumer = new FlinkKafkaConsumer<T>(topic,
            new KafkaPojoDeserializationSchema<T>(pojoClass),
            this.serverConfig.buildKafkaConsumerConfig());

        //指定时间消费kafka数据
        if (serverConfig.getStartTimeStamp() != null && serverConfig.getStartTimeStamp() != 0) {
            flinkConsumer.setStartFromTimestamp(serverConfig.getStartTimeStamp());
        } else {
            //不指定，默认从最新时间开始消费
            flinkConsumer.setStartFromLatest();
        }


        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, pojoClass.getSimpleName());

        return this.env.addSource(flinkConsumer).name(tableName);
    }

    protected <T> void createTableFromKafka(String topic, Class<T> pojoClass, boolean withProcTime) {
        DataStream<T> stream = this.createStreamFromKafka(topic, pojoClass);

        String tableFields = getAllFields(pojoClass).stream().map(Field::getName)
            .collect(Collectors.joining(", "));

        if (withProcTime) {
            tableFields = tableFields + ", proctime.proctime";
        }

        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, pojoClass.getSimpleName());

        logger.info("table fields for {}: {}", tableName, tableFields);

        //转为table
        this.tableEnv.createTemporaryView(tableName,
            stream,
            tableFields);
    }

    //stream to table
    public <T> void streamToTable(Class<T> pojoClass, DataStream<T> stream, boolean withProcTime) {
        String tableFields = getAllFields(pojoClass).stream().map(Field::getName)
            .collect(Collectors.joining(", "));

        if (withProcTime) {
            tableFields = tableFields + ", proctime.proctime";
        }

        String tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, pojoClass.getSimpleName());

        logger.info("table fields for {}: {}", tableName, tableFields);

        //转为table
        this.tableEnv.createTemporaryView(tableName,
            stream,
            tableFields);
    }

    protected <T> void createViewFromTable(String tableName, Table table) {
        this.tableEnv.createTemporaryView(tableName, table);
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        // 忽悠 Ignore 注解 字段写入库
        fields = fields.stream().filter(a -> a.getAnnotation(Ignore.class) == null).collect(Collectors.toList());
        return fields;
    }

    protected void createTableFromJdbc(String flinkTable, String jdbcTable, Class<?> pojoClass) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ")
            .append(flinkTable)
            .append("(");

        List<Field> allFields = getAllFields(pojoClass);
        for (Field field : allFields) {
            String fieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());

            Class<?> fieldClass = field.getType();
            if (String.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" VARCHAR, ");
            } else if (Integer.class.equals(fieldClass) || int.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" INT, ");
            } else if (Long.class.equals(fieldClass) || long.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" BIGINT, ");
            } else if (Short.class.equals(fieldClass) || short.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" SMALLINT, ");
            } else if (BigDecimal.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" DECIMAL(38, 18), ");
            } else if (Boolean.class.equals(fieldClass) || boolean.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" BOOLEAN, ");
            } else if (Timestamp.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" TIMESTAMP(3), ");
            } else if (Date.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" DATE, ");
            } else if (java.sql.Time.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" TIME, ");
            } else if (Float.class.equals(fieldClass) || float.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" FLOAT, ");
            } else if (Double.class.equals(fieldClass) || double.class.equals(fieldClass)) {
                builder.append("`").append(fieldName).append("`").append(" DOUBLE, ");
            } else {
                throw new RuntimeException("不支持的类型: " + fieldClass);
            }
        }

        // 删除逗号，跳过空格
        builder.deleteCharAt(builder.length() - 2);
        builder.append(")");

        // with part
        builder.append(" WITH (")
            .append("'connector.type' = 'jdbc',")
            .append("'connector.url'='").append(this.serverConfig.getJdbcDBUrl()).append("',")
            .append("'connector.table'='").append(jdbcTable).append("',")
            .append("'connector.driver'='").append("com.mysql.cj.jdbc.Driver").append("',")
            .append("'connector.username'='").append(this.serverConfig.getJdbcUsername()).append("',")
            .append("'connector.password'='").append(this.serverConfig.getJdbcPassword()).append("')");

        logger.info("Create table: {}", builder.toString());

        tableEnv.executeSql(builder.toString());
        // this.tableEnv.sqlUpdate(builder.toString());
    }

    public void createTemporalTableFunc(String funcName, String flinkTable, String timeField, String pkField) {
        TemporalTableFunction func = this.tableEnv.from(flinkTable)
            .createTemporalTableFunction(timeField, pkField);

        this.registerUdf(funcName, func);
    }

    public <T> void appendToKafka(Table table, Class<T> pojoClass, String sinkTopic) {
        DataStream<T> ds = this.tableEnv.toAppendStream(table, pojoClass);
        //ds.print();
        toKafkaSink(ds, sinkTopic);
    }

    public <T> void retractToKafka(Table table, Class<T> pojoClass, String sinkTopic) {
        DataStream<T> ds = this.tableEnv.toRetractStream(table, pojoClass)
            .map(new MapFunction<Tuple2<Boolean, T>, T>() {
                @Override
                public T map(Tuple2<Boolean, T> value) throws Exception {
                    System.out.println("Retract value: ");
                    System.out.println(value);
                    if (value.f0) {
                        return value.f1;
                    }

                    return null;
                }
            }).filter(new FilterFunction<T>() {
                @Override
                public boolean filter(T value) throws Exception {
                    return value != null;
                }
            });
        toKafkaSink(ds, sinkTopic);
    }

    public <T> void appendToJdbc(Table table, Class<T> pojoClass, String jdbcTable) {
        DataStream<T> ds = this.tableEnv.toAppendStream(table, pojoClass);
        //        ds.print();
        toJdbcUpsertSink(ds, jdbcTable, pojoClass);
    }

    /**
     * 将table数据以upsert的形式写入jdbc data source
     *
     * @param table
     * @param pojoClass
     * @param jdbcTable
     * @param <T>
     */
    public <T> void retractToJdbc(Table table, Class<T> pojoClass, String jdbcTable) {
        DataStream<T> ds = this.tableEnv.toRetractStream(table, pojoClass)
            .map(new MapFunction<Tuple2<Boolean, T>, T>() {
                @Override
                public T map(Tuple2<Boolean, T> value) throws Exception {
                    System.out.println("Retract to jdbc: " + value.f1);

                    if (value.f0) {
                        return value.f1;
                    }

                    return null;
                }
            }).filter(new FilterFunction<T>() {
                @Override
                public boolean filter(T value) throws Exception {
                    return value != null;
                }
            });
        toJdbcUpsertSink(ds, jdbcTable, pojoClass);
    }

    public <T> void toKafkaSink(DataStream<T> ds, String sinkTopic) {
        ds.addSink(new FlinkKafkaProducer<T>(sinkTopic,
                new DefaultKafkaSerializationSchema<T>(sinkTopic),
                this.serverConfig.buildKafkaProducerConfig(),
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE))
            .name(sinkTopic);
    }

    public <T> void toJdbcUpsertSink(DataStream<T> ds, String jdbcTable, Class<T> pojoClass) {
        ds.addSink(JdbcSink.newJdbUpsertSink(this.serverConfig, jdbcTable, pojoClass))
            .name(jdbcTable);
    }


    public void registerUdf(String funcName, UserDefinedFunction udf) {
        if (udf instanceof ScalarFunction) {
            this.tableEnv.createTemporarySystemFunction(funcName, udf);
        } else if (udf instanceof TableFunction) {
            this.tableEnv.createTemporarySystemFunction(funcName, udf);
        } else if (udf instanceof AggregateFunction) {
            this.tableEnv.createTemporarySystemFunction(funcName, udf);
        } else if (udf instanceof TableAggregateFunction) {
            this.tableEnv.createTemporarySystemFunction(funcName, udf);
        }
    }


    public void runJob(String jobName) throws Exception {
        this.defineJob();
        this.env.execute(jobName);
    }

    /**
     * 注册需要用到的SQL文件，必须为yaml格式的文件，且在classpath查找路径上。
     *
     * @param sqlFile classpath内文件的路径
     */
    public void registerSql(String... sqlFile) {
        if (sqlReader == null) {
            sqlReader = new SqlReader(sqlFile);
        } else {
            sqlReader.addFiles(sqlFile);
        }

        try {
            sqlReader.loadSql();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Table sqlQuery(String queryName) {
        return this.tableEnv.sqlQuery(this.sqlReader.getByName(queryName));
    }


    /**
     * 在这里实现Job逻辑
     */
    public abstract void defineJob() throws Exception;


    @Override
    public String getJobDescription() {
        return this.jobDescription != null ? this.jobDescription : FlinkJob.super.getJobDescription();
    }
}