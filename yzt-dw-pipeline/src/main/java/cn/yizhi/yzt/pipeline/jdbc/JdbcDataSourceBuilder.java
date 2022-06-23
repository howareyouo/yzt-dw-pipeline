package cn.yizhi.yzt.pipeline.jdbc;

import cn.yizhi.yzt.pipeline.config.ServerConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.connector.jdbc.JdbcInputFormat;
import org.apache.flink.connector.jdbc.split.JdbcParameterValuesProvider;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author zjzhang
 * @date 2020/6/19
 */
public class JdbcDataSourceBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataSourceBuilder.class);

    private PojoTypes<?> pojoTypes;
    private String jdbcTableName;

    // 必须为数值类型的key， 用做inputsplit的分区
    private String partitionKey;

    private ServerConfig serverConfig;

    private JdbcInputFormat inputFormat;

    public JdbcDataSourceBuilder(ServerConfig serverConfig, Class<?> pojoClass, String jdbcTableName) {
        this.serverConfig = serverConfig;
        this.pojoTypes = PojoTypes.of(pojoClass);
        this.jdbcTableName = jdbcTableName;
    }

    public String buildSelectSql() {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT ");

        for (Map.Entry<String, TypeInformation<?>> nextItem : this.pojoTypes.getFieldTypes().entrySet()) {
            buf.append(nextItem.getKey()).append(",");
        }

        buf.deleteCharAt(buf.lastIndexOf(","))
                .append(" FROM ")
                .append(this.jdbcTableName);

        return buf.toString();
    }

//    public Serializable[][] buildNumericValueProvider(String partitionKey, Serializable[] queryParams){
//        Serializable[][] splittedParams = new Serializable[this.serverConfig.getParallelism()][];
//
//        int leftovers = queryParams.length % this.serverConfig.getParallelism();
//        int slotSize = queryParams.length / this.serverConfig.getParallelism() + (leftovers > 0 ? 1 : 0);
//
//        for(int i=0; i< serverConfig.getParallelism(); i++) {
//            for(int j=0; j< slotSize; j++) {
//                splittedParams[i][j] = queryParams[i * slotSize + j];
//            }
//        }
//
//        return splittedParams;
//    }

    public JdbcDataSourceBuilder buildInputFormat(String query, JdbcParameterValuesProvider valuesProvider) {
        RowTypeInfo rowTypeInfo = this.pojoTypes.toRowTypeInfo();
        JdbcInputFormat.JdbcInputFormatBuilder builder = JdbcInputFormat.buildJdbcInputFormat()
                .setDrivername("com.mysql.cj.jdbc.Driver")
                .setDBUrl(serverConfig.getJdbcDBUrl())
                .setUsername(serverConfig.getJdbcUsername())
                .setPassword(serverConfig.getJdbcPassword())
                .setRowTypeInfo(rowTypeInfo)
                .setQuery(query);

        if (valuesProvider != null) {
            builder.setParametersProvider(valuesProvider);
        };

        this.inputFormat = builder.finish();

        return this;
    }

    public DataSource<Row> build(ExecutionEnvironment env){
        return env.createInput(this.inputFormat);
    }

    public static DataSource<Row> buildDataSource(ExecutionEnvironment env, ServerConfig serverConfig,
                                                  Class<?> pojoClass,
                                                  String jdbcTableName,
                                                  JdbcParameterValuesProvider valuesProvider) {
        JdbcDataSourceBuilder dsBuilder = new JdbcDataSourceBuilder(serverConfig, pojoClass, jdbcTableName);

        String selectSql = dsBuilder.buildSelectSql();

        LOG.info("SQL for datasource: {}", selectSql);


        return dsBuilder.buildInputFormat(selectSql, valuesProvider)
                .build(env);
    }
}
