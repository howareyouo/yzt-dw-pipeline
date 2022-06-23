package cn.yizhi.yzt.pipeline.kafka;

import cn.yizhi.yzt.pipeline.util.TimeUtil;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonGenerator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonParser;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.ObjectCodec;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.*;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.sql.Date;

import static org.apache.flink.api.java.typeutils.TypeExtractor.getForClass;

/**
 * @author zjzhang
 */
public class KafkaPojoDeserializationSchema<T> implements KafkaDeserializationSchema<T> {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;


    private static final long serialVersionUID = 1509391548173891955L;

    private ObjectMapper mapper;
    private Class<T> pojoClass;

    public KafkaPojoDeserializationSchema(Class<T> pojoClass) {
        this.pojoClass = pojoClass;
    }

    @Override
    public T deserialize(ConsumerRecord<byte[], byte[]> record) {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        //需要判断数据是否为空
        if (record.value() != null) {
            try {
                return mapper.readValue(record.value(), this.pojoClass);
            } catch (Exception e) {
                System.out.println("JSON deserialization failed: " + new String(record.value()));
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return getForClass(this.pojoClass);
    }


    public static class DateDeserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
            // Get reference to ObjectCodec
            ObjectCodec codec = jsonParser.getCodec();

            // Parse "object" node into Jackson's tree model
            JsonNode node = codec.readTree(jsonParser);

            return toDate(node.asInt());
        }

        private Date toDate(int value) {
            return new java.sql.Date(value * MILLIS_PER_DAY);
        }

    }


    public static class CouponTypeDeserializer extends JsonDeserializer<Integer> {

        @Override
        public Integer deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException, JsonProcessingException {
            // Get reference to ObjectCodec
            ObjectCodec codec = jsonParser.getCodec();

            // Parse "object" node into Jackson's tree model
            JsonNode node = codec.readTree(jsonParser);

            String couponType = node.asText();
            Integer couponTypeValue = null;
            try {
                couponTypeValue = 1;
            } catch (Exception e) {
            }
            return couponTypeValue;
        }
    }


    public static class Dateserialize extends JsonSerializer<Date> {

        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (date != null)
                jsonGenerator.writeNumber(TimeUtil.toDays(date));
        }
    }


}
