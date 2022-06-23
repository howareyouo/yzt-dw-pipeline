package cn.yizhi.yzt.pipeline.config;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zjzhang
 */
public class SqlReader {
    private String[] sqlPath;

    private final Map<String, String> sqlMap;

    public SqlReader(String ...filePaths) {
        this.sqlPath = filePaths;

        for(String sqlFile: sqlPath) {
            if (!(sqlFile.endsWith(".yaml") || sqlFile.endsWith(".yml"))) {
                throw new IllegalArgumentException("Invalid SQL config file name: " + sqlFile);
            }
        }

        sqlMap = new HashMap<>();
    }

    public synchronized void addFiles(String ...filePaths) {
        String[] newSqlPath = Arrays.copyOf(this.sqlPath, this.sqlPath.length + filePaths.length);
        for(int i = this.sqlPath.length; i< this.sqlPath.length + filePaths.length; i++) {
            newSqlPath[i] = filePaths[i-this.sqlPath.length];
        }

        this.sqlPath = newSqlPath;
    }


    public synchronized void loadSql() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        for(String sqlFile: sqlPath) {
            InputStream in = SqlReader.class.getClassLoader().getResourceAsStream(sqlFile);
            assert in != null;
           Map<String, String> sqls = mapper.readValue(in, new TypeReference<Map<String,String>>() {});

           sqls.forEach((key, value) -> {
               this.sqlMap.merge(key, value, (v1,v2) -> v2);
           });
        }
    }

    public String getByName(String queryName) {
        return this.sqlMap.getOrDefault(queryName, "");
    }

    public int querySize() {
        return this.sqlMap.size();
    }

}
