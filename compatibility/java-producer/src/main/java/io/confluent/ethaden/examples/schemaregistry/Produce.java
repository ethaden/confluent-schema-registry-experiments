package io.confluent.ethaden.examples.schemaregistry.schemamigration;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import models.avro.SimpleValue;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Produce {

    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DRIVER_ID = "Producer";
    private static final String TOPIC = "topic";
    private int count = 0;

    private Properties settings() {
        final Properties settings = new Properties();
        settings.put(ProducerConfig.CLIENT_ID_CONFIG, DRIVER_ID);
        settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        settings.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        settings.put(ProducerConfig.BATCH_SIZE_CONFIG, 30);
        return settings;
    }
    
    private Properties avroSettings() {
        final Properties settings = settings();
        settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        return settings;
    }
    
    public static void main(String[] args) {
        Produce produce = new Produce();
        produce.sendAvroProducer(10);
    }

    void sendAvroProducer(int nb) {
        LOGGER.info("Starting Arvo Producer");
        try (KafkaProducer<String, SimpleValue> producer = new KafkaProducer<>(avroSettings())) {
            for (int i=0; i < nb; i++) {
                String key = Integer.toString(count);
                SimpleValue value = SimpleValue.newBuilder()
                        .setTheName("This is message " + key)
                        .setTheValue("Some value")
                        .setTheNewName("And another, new name")
                        .build();
                ProducerRecord<String, SimpleValue> producerRecord = new ProducerRecord<>(TOPIC, key, value);
                LOGGER.info("Sending message {}", count);
                producer.send(producerRecord, (RecordMetadata recordMetadata, Exception exception) -> {
                    if (exception == null) {
                        System.out.println("Record written to offset " +
                                recordMetadata.offset() + " timestamp " +
                                recordMetadata.timestamp());
                    } else {
                        System.err.println("An error occurred");
                        exception.printStackTrace(System.err);
                    }
              });
                count++;
            }
            LOGGER.info("Producer flush");
            producer.flush();
        } finally {
            LOGGER.info("Closing producer");
        }
    }
}
