package cn.yizhi.yzt.pipeline.kafka;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KafkaInterceptor implements ConsumerInterceptor<byte[], byte[]> {
    private static Logger logger = LoggerFactory.getLogger(KafkaInterceptor.class);

    @Override
    public ConsumerRecords<byte[], byte[]> onConsume(ConsumerRecords<byte[], byte[]> records){
//        Iterator iterator = records.iterator();
//        while(iterator.hasNext()) {
//            ConsumerRecords<byte[], byte[]> record = (ConsumerRecords<byte[], byte[]>) iterator.next();
//
        System.out.println("收到Kafka message, count:  " + records.count());
        records.partitions().forEach(p -> {
            records.records(p.topic()).forEach(consumerRecord -> {
//                if(consumerRecord.value() == null) {
//                    throw new IllegalArgumentException("Empty kafka message");
//                }
                System.out.println("record value: " + new String(consumerRecord.value()));
            });
        });

        return records;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets){
//        org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase
    }

    /**
     * This is called when interceptor is closed
     */
    @Override
    public void close(){
    }

    @Override
    public void configure(Map<String, ?> configs){}
}
