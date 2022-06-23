package cn.yizhi.yzt.pipeline;


import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jobs.JobLoader;
import org.apache.flink.api.java.utils.ParameterTool;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;


public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        InputStream in = Application.class.getResourceAsStream("/job-config.properties");
        ParameterTool fileParams = ParameterTool.fromPropertiesFile(in);

        //命令行参数优先
        ParameterTool cmdParams = ParameterTool.fromArgs(args);

        final ParameterTool allParams = fileParams.mergeWith(cmdParams);

        ServerConfig serverConfig = new ServerConfig(allParams);

        // 检查数据库状态
        checkSchemas(serverConfig);

        JobLoader jobLoader = new JobLoader(serverConfig);

        /*  JobName支持两种形式：
            1. 使用完整的job class路径，如：cn.yizhi.yzt.pipeline.jobs.factstream.OdsLogStreamJob
            2. 使用job class的名字，但是首字母小写，如 odsLogStreamJob， 等同于上面的cn.yizhi.yzt.pipeline.jobs.factstream.OdsLogStreamJob
         */
        jobLoader.run(serverConfig.getJobName());
    }

    private static void checkSchemas(ServerConfig serverConfig) {
        Flyway flyway = Flyway.configure()
                .dataSource(serverConfig.getJdbcDBUrl(), serverConfig.getJdbcUsername(), serverConfig.getJdbcPassword())
                .baselineDescription("bigdata_init")
                .load();
        try {
            //flyway.validate();
            //flyway.baseline();
            flyway.migrate();
        } catch (FlywayException e) {
            logger.warn("DW schema changed", e);
        }
    }

}
