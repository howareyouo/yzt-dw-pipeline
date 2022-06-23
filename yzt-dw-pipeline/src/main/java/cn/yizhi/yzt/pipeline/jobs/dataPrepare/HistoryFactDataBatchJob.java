package cn.yizhi.yzt.pipeline.jobs.dataPrepare;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jobs.FlinkJob;
import org.apache.flink.api.java.ExecutionEnvironment;

public class HistoryFactDataBatchJob implements FlinkJob {

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

        FactShopHistoryBatchJob.run(env, serverConfig);
        FactCouponBatchJob.run(env,serverConfig);
        //FactFlashSaleBatchJob.run(env, serverConfig);
        FactFullReduceBatchJob.run(env,serverConfig);
        FactProductBatchJob.run(env,serverConfig);

        env.execute(jobName);
    }
}
