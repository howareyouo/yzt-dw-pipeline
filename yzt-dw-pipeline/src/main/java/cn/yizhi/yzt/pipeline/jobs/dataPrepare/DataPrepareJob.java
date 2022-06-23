package cn.yizhi.yzt.pipeline.jobs.dataPrepare;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jobs.FlinkJob;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.connector.jdbc.JdbcRowOutputFormat;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DW数据初始化Job
 */
public class DataPrepareJob implements FlinkJob {
    private static Logger logger = LoggerFactory.getLogger(DataPrepareJob.class);

    private ServerConfig serverConfig;


    @Override
    public void submitJob(String jobName, ServerConfig serverConfig) throws Exception {
        this.serverConfig = serverConfig;
        // set up the execution environment
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        // make parameters available in the web interface
        env.getConfig().setGlobalJobParameters(serverConfig.getParams());
        env.setParallelism(serverConfig.getParallelism());
        //
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(10, 10000L));

        run(env, serverConfig, jobName);

    }


    public static void run(ExecutionEnvironment env, ServerConfig serverConfig, String jobName) throws Exception {

        DataSet<Row> timeData = env.fromCollection(new DateIterator("2019-01-01", "2030-01-01"), Row.class);

        /**
         *   id              INT   NOT NULL PRIMARY KEY COMMENT '同day, 格式如20200616',
         *     `day`            varchar(20)                 COMMENT '天 格式为  20200616',
         *     `day_in_month`   INT                        COMMENT '月天序数',
         *     `day_in_year`    INT                        COMMENT '年天序数',
         *     `month`          varchar(10)                 COMMENT '月',
         *     `month_in_year`  INT                        COMMENT '年月序数',
         *     `year`           INT                         COMMENT '年',
         *      half_of_year     varchar(10)                COMMENT '上半年  下半年',
         *     `quarter`        varchar(10)                 COMMENT '季度',
         *     `weekday`        varchar(16)                 COMMENT '星期数值',
         *     `weekdayInEng`   varchar(16)                 COMMENT '英文星期数值',
         *     `week`           varchar(10)                 COMMENT '这一年的第几周'
         */

        OutputFormat outputFormat = JdbcRowOutputFormat.buildJdbcOutputFormat()
                .setDrivername("com.mysql.jdbc.Driver")
                .setBatchSize(serverConfig.getJdbcBatchSize())
                .setDBUrl(serverConfig.getJdbcDBUrl())
                .setUsername(serverConfig.getJdbcUsername())
                .setPassword(serverConfig.getJdbcPassword())
                .setQuery("insert into dim_date (id, day, day_in_month, day_in_year, month, month_in_year, year, half_of_year, quarter, weekday, weekday_eng, week) " +
                        "values (?,?,?,?,?,?,?,?,?,?,?,?)")
                .finish();

        timeData.output(outputFormat);

        env.execute(jobName);
    }
}
