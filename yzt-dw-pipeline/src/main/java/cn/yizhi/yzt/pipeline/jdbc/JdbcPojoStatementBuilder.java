package cn.yizhi.yzt.pipeline.jdbc;

import org.apache.flink.shaded.guava30.com.google.common.base.CaseFormat;
import org.apache.flink.util.function.BiConsumerWithException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @param <IN> a Pojo
 * @author zjzhang
 */
public class JdbcPojoStatementBuilder<IN> implements BiConsumerWithException<NamedParameterStatement, IN, SQLException> {
    private static final Logger logger = LoggerFactory.getLogger(JdbcPojoStatementBuilder.class);
    private Class<IN> pojoClz;

    public JdbcPojoStatementBuilder(Class<IN> pojoClz) {
        this.pojoClz = pojoClz;
    }

    @Override
    public void accept(NamedParameterStatement preparedStatement, IN in) throws SQLException {
        List<Field> pojoFields = getAllFields(pojoClz);

        try {
            for (Field field : pojoFields) {
                String jdbcName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
                boolean isAccessible = field.isAccessible();
                if (!isAccessible) {
                    field.setAccessible(true);
                }
                try {
                    preparedStatement.setObject(jdbcName, field.get(in));
                } catch (IllegalArgumentException e) {
                    logger.debug("Set prepared statement field failed: {}", e.getMessage());
                    logger.debug("POJO: {}", in);
                    logger.debug("SQL: {}", preparedStatement.getRawQuery());
                    continue;
                }
                field.setAccessible(isAccessible);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }
}
