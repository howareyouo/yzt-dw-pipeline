# yzt-dw-pipeline

yzt dw pipeline

## How to build and package:

gradle clean shadowJar


# 运行参数：

--kafka.bootstrapServers 192.168.100.22:9092,192.168.100.23:9092,192.168.100.24:9092 --kafka.replay true 
--jobName factStreamJob 
--jdbc.url jdbc:mysql://am-bp1n03jt2h11phya2131910o.ads.aliyuncs.com:3306/yzt_dw_db_dev?characterEncoding=utf8 
--jdbc.user yzt_dev_user_dev 
--jdbc.password nt1RPxaaba09cs9naoCtpRMC

# 重跑历史数据操作流程
* 删除指标表指定重跑日期后的数据（sql参考delete-history-data.sql） 
* 停止计算指标的相关job
* 重跑job，指定重跑时间点（--flink.startTimeStamp xxx 单位毫秒），不需要保留state数据
