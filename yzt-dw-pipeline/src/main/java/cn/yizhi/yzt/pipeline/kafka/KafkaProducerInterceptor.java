package cn.yizhi.yzt.pipeline.kafka;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonGenerator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zjzhang
 */
public class KafkaProducerInterceptor implements ProducerInterceptor<byte[], byte[]> {
    private static Logger logger = LoggerFactory.getLogger(KafkaProducerInterceptor.class);
   // private static final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

    private ObjectMapper mapper;
    private Map<String, JsonNode> schemaCache;

    @Override
    public ProducerRecord<byte[], byte[]> onSend(ProducerRecord<byte[], byte[]> record){
        String topic = record.topic();

        JsonNode schema = this.loadJsonSchema(topic);
        if (schema == null) {
            logger.info("No Json schema found, message is not wrapped");
            return record;
        }

        System.out.println("kafka producer: key: " + new String(record.key()) + ", topic: " + topic + ", value: " + new String(record.value()));

        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        }

        try {
            JsonNode node = mapper.readTree(record.value());

            ObjectNode wrapper = mapper.createObjectNode();

            // 因为使用kafka connect jdbc sink来写入DB ,需要按照kafka connect SinkRecord的规范来定义json schema
            wrapper.set("schema", this.loadJsonSchema(topic));
            wrapper.set("payload", node);

            byte[] message = mapper.writeValueAsBytes(wrapper);

            return new ProducerRecord<>(record.topic(), record.partition(), record.timestamp(), record.key(), message);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception){
        if (exception != null) {
            logger.error("Publish message to kafka failed", exception);
        }
    }

    /**
     * This is called when interceptor is closed
     */
    @Override
    public void close(){
        this.schemaCache.clear();
    }

    @Override
    public void configure(Map<String, ?> configs){}


    private JsonNode loadJsonSchema(String kafkaTopic) {
        if (schemaCache == null) {
            schemaCache = new HashMap<>();
        }

        return schemaCache.computeIfAbsent(kafkaTopic, topic-> {
            try {
               // return JsonLoader.fromResource("/jsonschemas/" + topic + "-schema.json");
                return null;
            } catch (Exception e){
                return null;
            }
        });

    }
}
