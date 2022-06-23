package cn.yizhi.yzt.pipeline.udf;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.table.catalog.DataTypeFactory;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.table.types.inference.TypeInference;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * 将Date转换为Timestamp
 *
 * @author zjzhang
 */
public class UdfDateToTimestamp extends ScalarFunction {

    public Timestamp eval(Date date) {
        if (date == null) {
            return null;
        }

        return new Timestamp(date.getTime());
    }

    @Override
    public TypeInformation<?> getResultType(Class<?>[] signature) {
        return Types.SQL_TIMESTAMP;
    }

    @Override
    public TypeInference getTypeInference(DataTypeFactory typeFactory) {
        return TypeInference.newBuilder()
                .build();
    }
}
