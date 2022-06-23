package cn.yizhi.yzt.pipeline.jobs;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author zjzhang
 */
public class JobLoader {
    private static final Logger logger = LoggerFactory.getLogger(JobLoader.class);

    private static final String rootPackage = "cn.yizhi.yzt.pipeline";
    private static final String jobInterfaceName = "cn.yizhi.yzt.pipeline.jobs.FlinkJob";

    private ServerConfig serverConfig;


    public JobLoader(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends FlinkJob> loadJobClass(String jobName) {
        ClassGraph cg = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(rootPackage);

        String jobClassName = FlinkJob.classNameFromJobName(jobName);
        logger.info("Job classname: {}", jobClassName);

        try (ScanResult scanResult = cg.scan()) {
            ClassInfoList allJobClasses = scanResult.getClassesImplementing(jobInterfaceName);
            // print all job in one dot file
            logger.info(allJobClasses.generateGraphVizDotFile());

            List<Class<?>> loadedClasses = allJobClasses.filter((classInfo) -> {
               return classInfo.getSimpleName().equals(jobClassName) || classInfo.getName().equals(jobClassName);
            }).loadClasses();

            if (loadedClasses.size() != 1) {
                logger.info("Loaded classes: {}", loadedClasses);
                throw new RuntimeException("Loaded more than ONE job class with a single name");
            }

            return (Class<? extends FlinkJob>) loadedClasses.get(0);
        }
    }

    public void run(String jobName) throws Exception {
        Class<? extends FlinkJob> jobClass = this.loadJobClass(jobName);

        FlinkJob job = jobClass.getDeclaredConstructor().newInstance();

        logger.info("Running job {}", jobName);
        logger.info("Job Description {}", job.getJobDescription());

        job.submitJob(jobClass.getName(), this.serverConfig);
    }
}
