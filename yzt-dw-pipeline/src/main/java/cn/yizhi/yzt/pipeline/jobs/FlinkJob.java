package cn.yizhi.yzt.pipeline.jobs;

import cn.yizhi.yzt.pipeline.config.ServerConfig;

/**
 * @author zjzhang
 */
public interface FlinkJob {

    void submitJob(String jobName, final ServerConfig serverConfig) throws Exception;

    /**
     * @return Job Name, 默认返回camel case的 job class name
     */
    default String getJobName() {
        String jobClassName = this.getClass().getSimpleName();
        return FlinkJob.jobNameFromClass(jobClassName);
    }

    /**
     * @return Job的描述
     */
    default String getJobDescription() {
        return getJobName();
    }

    /**
     * 从class名称转换Job name
     *
     * @param className Class#getName() or getSimpleName()
     */
    static String jobNameFromClass(String className) {
        // 如果 className是含有package路径的名称
        if (className.contains(".")) {
            return className;
        }

        char[] nameChar = className.toCharArray();
        nameChar[0] = Character.toLowerCase(nameChar[0]);

        return new String(nameChar);
    }

    /**
     * 从jobName转换到className
     *
     * @param jobName
     */
    static String classNameFromJobName(String jobName) {
        // 如果 jobName是含有package路径的名称
        if (jobName.contains(".")) {
            return jobName;
        }

        char[] nameChar = jobName.toCharArray();
        nameChar[0] = Character.toUpperCase(nameChar[0]);

        return new String(nameChar);

    }
}
