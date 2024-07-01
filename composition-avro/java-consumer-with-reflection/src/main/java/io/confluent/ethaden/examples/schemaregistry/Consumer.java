package io.confluent.ethaden.examples.schemaregistry;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.ReflectionAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class Consumer {

    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String GROUP_ID = "ConsumerWithReflection";
    private static final String KAFKA_TOPIC = "topic";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

    private static Properties settings() {
        final Properties settings = new Properties();
        settings.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        settings.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        settings.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 100);
        settings.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        settings.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ReflectionAvroDeserializer.class);
        //settings.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        settings.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        //settings.put("avro.reflection.allow.null", true);
        return settings;
    }

    public static void main(String[] args) {
        LOGGER.info("Starting consumer");

        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(settings())) {
            // Subscribe to our topic
            LOGGER.info("Subscribing to topic " + KAFKA_TOPIC);
            consumer.subscribe(List.of(KAFKA_TOPIC));
            consumer.seekToBeginning(consumer.assignment());
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    final var records = consumer.poll(POLL_TIMEOUT);
                    int count = records.count();
                    if (count!=0) {
                        LOGGER.warn("Poll returned {} records", count);
                    }
                    for (var record : records) {
                        LOGGER.warn("Fetch record key={} value={}", record.key(), record.value());
                    }
                } catch (RecordDeserializationException re) {
                    long offset = re.offset();
                    Throwable t = re.getCause();
                    LOGGER.error("Failed to consumer at partition={} offset={}", re.topicPartition().partition(), offset, t);
                    LOGGER.warn("Skipping offset={}", offset);
                    consumer.seek(re.topicPartition(), offset+1);
                } catch (Exception e) {
                    LOGGER.error("Failed to consumer", e);
                }
            }
        } finally {
            LOGGER.warn("Closing consumer");
        }
    }

}