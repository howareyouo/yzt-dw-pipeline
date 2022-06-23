package cn.yizhi.yzt.pipeline.jobs.dimension;


import cn.yizhi.yzt.pipeline.config.ServerConfig;

import cn.yizhi.yzt.pipeline.jobs.FlinkJob;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DimBatchJob定义维度表加工的流程
 */
public class DimBatchJob implements FlinkJob {
    private static Logger logger = LoggerFactory.getLogger(DimBatchJob.class);

    @Override
    public void submitJob(String jobName, ServerConfig serverConfig) throws Exception {

        // set up the execution environment
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        // make parameters available in the web interface
        env.getConfig().setGlobalJobParameters(serverConfig.getParams());
        env.setParallelism(serverConfig.getParallelism());


        run(env, serverConfig, jobName);
    }


    private void run(ExecutionEnvironment env, ServerConfig serverConfig, String jobName) throws Exception {

        DimShopJob.run(env, serverConfig);
        DimMemberJob.run(env, serverConfig);
        DimProductJob.run(env, serverConfig);
        PromotionJob.run(env, serverConfig);

        env.execute(jobName);
    }
}
