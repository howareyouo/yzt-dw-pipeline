package cn.yizhi.yzt.pipeline.kafka;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class DefaultKafkaSerializationSchema<T> implements KafkaSerializationSchema<T> {

    private static final long serialVersionUID = 1509391548173891955L;
    private final static Logger log = LoggerFactory.getLogger(DefaultKafkaSerializationSchema.class);
    private String topic;
    private ObjectMapper mapper;

    public DefaultKafkaSerializationSchema(String topic) {
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(T element, @Nullable Long timestamp) {
        byte[] b = null;
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        try {
            if (element != null) {
                b = mapper.writeValueAsBytes(element);
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return new ProducerRecord<byte[], byte[]>(topic, b);
    }
}
