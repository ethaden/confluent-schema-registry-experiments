package io.confluent.ethaden.examples.schemaregistry.schemamigration.schemav2;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.avro.Conversion;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import models.avro.Measurement;

public class ConsumerV2 {

    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String KAFKA_TOPIC = "measurements";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

    private static Properties settings(String groupId) {
        final Properties settings = new Properties();
        settings.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        settings.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        settings.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 100);
        settings.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        settings.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        settings.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        settings.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        settings.put(KafkaAvroDeserializerConfig.AUTO_REGISTER_SCHEMAS, false);
        // Adding support for our custom AVRO logical type
        settings.put(KafkaAvroSerializerConfig.AVRO_USE_LOGICAL_TYPE_CONVERTERS_CONFIG, true);
        // Always use the latest version of the schema from Schema Registry
        // But use only schema versions where the metadata field "application.major.version" is equal to "2"
        settings.put(KafkaAvroDeserializerConfig.USE_LATEST_WITH_METADATA, "application.major.version=2");
        return settings;
    }

    public static void main(String[] args) {
        Random ran = new Random();
        String groupId = new String("Consumer-"+ran.nextInt(100000));
        LOGGER.info("Starting consumer with schema v2: "+groupId);

        try (KafkaConsumer<String, Measurement> consumer = new KafkaConsumer<>(settings(groupId))) {
            // Subscribe to our topic
            LOGGER.info("Subscribing to topic " + KAFKA_TOPIC);
            consumer.subscribe(List.of(KAFKA_TOPIC));
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    final var records = consumer.poll(POLL_TIMEOUT);
                    int count = records.count();
                    if (count!=0) {
                        LOGGER.warn("Poll return {} records", count);
                    }
                    for (var record : records) {
                        LOGGER.warn("Fetch record key={} value={}", record.key(), record.value());
                        SpecificMeasurement measurement = MeasurementConverter.fromAvro(record.value());
                        System.out.println(measurement);
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