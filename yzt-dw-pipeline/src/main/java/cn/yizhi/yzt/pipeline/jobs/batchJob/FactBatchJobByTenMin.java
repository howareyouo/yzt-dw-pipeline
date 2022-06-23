package cn.yizhi.yzt.pipeline.jobs.batchJob;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import cn.yizhi.yzt.pipeline.jobs.FlinkJob;
import org.apache.flink.api.java.ExecutionEnvironment;

/**
 * @author hucheng
 * 批处理 1个小时一批次
 * @date 2020/11/10 下午3:34
 */
public class FactBatchJobByTenMin implements FlinkJob {
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

        FactShopBatchJob.run(env, serverConfig);
        //FactFullReduceBatchJob.run(env, serverConfig);
        env.execute(jobName);
    }
}
