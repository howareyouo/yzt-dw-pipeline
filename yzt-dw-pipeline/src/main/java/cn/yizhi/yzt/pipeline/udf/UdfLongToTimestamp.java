package cn.yizhi.yzt.pipeline.udf;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.table.functions.ScalarFunction;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * @author zjzhang
 */
public class UdfLongToTimestamp extends ScalarFunction {

    public Timestamp eval(Long unixTime) {
        if (unixTime == null) {
            return null;
        }

        return new Timestamp(unixTime);
    }


    @Override
    public TypeInformation<?> getResultType(Class<?>[] signature) {
        return Types.SQL_TIMESTAMP;
    }
}
