package io.confluent.ethaden.examples.schemaregistry.schemamigration.schemav2decimal;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import models.avro.Measurement;
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
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;
import java.util.random.RandomGenerator;

public class ProducerV2Decimal {

    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DRIVER_ID = "ProducerV2Decimal";
    private static final String TOPIC = "measurements";
    private int count = 0;

    private Properties settings() {
        final Properties settings = new Properties();
        settings.put(ProducerConfig.CLIENT_ID_CONFIG, DRIVER_ID);
        settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        settings.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        settings.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, false);
        settings.put(KafkaAvroSerializerConfig.AVRO_REMOVE_JAVA_PROPS_CONFIG, true);
        // Always use the latest version of the schema from Schema Registry
        // But use only schema versions where the metadata field "application.major.version" is equal to "2"
        settings.put(KafkaAvroSerializerConfig.USE_LATEST_WITH_METADATA, "application.major.version=2");
        settings.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
        return settings;
    }
    
    private Properties avroSettings() {
        final Properties settings = settings();
        settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        return settings;
    }
    
    public static void main(String[] args) {
        ProducerV2Decimal produce = new ProducerV2Decimal();
        produce.sendAvroProducer(10);
    }

    void sendAvroProducer(int nb) {
        LOGGER.info("Starting Arvo Producer");
        Random rand = new Random();
        try (KafkaProducer<String, Measurement> producer = new KafkaProducer<>(avroSettings())) {
            for (int i=0; i < nb; i++) {
                String key = Integer.toString(count);
                
                BigDecimal randValue = new BigDecimal(rand.nextDouble(100));
                BigDecimal valueWithTwoDecimals = randValue.setScale(2, RoundingMode.HALF_UP);
                System.out.println("Sending decimal "+valueWithTwoDecimals);
                SpecificMeasurementDecimal measurement = new SpecificMeasurementDecimal("This is message " + key,
                    valueWithTwoDecimals, "°C");
                // Create DTO from business object. This wouldn't actually be necessary here as these have identical fields in this example.
                // But in general, separating these is a good idea
                Measurement avroMeasurement = MeasurementConverterDecimal.toAvro(measurement);
                ProducerRecord<String, Measurement> producerRecord = new ProducerRecord<>(TOPIC, key, avroMeasurement);
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
