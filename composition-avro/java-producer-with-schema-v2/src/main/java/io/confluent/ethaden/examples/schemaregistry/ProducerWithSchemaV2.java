package io.confluent.ethaden.examples.schemaregistry;

import java.lang.invoke.MethodHandles;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.example.avro.cloudevents.CloudEventBase;
import io.confluent.example.avro.cloudevents.ExampleEventRecord1;
import io.confluent.example.avro.cloudevents.ExampleEventRecord2;
import io.confluent.example.avro.cloudevents.Severity;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProducerWithSchemaV2 {

    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DRIVER_ID = "ProducerWithSchemaV2";
    private static final String TOPIC = "topic";
    private int count = 0;

    private Properties settings() {
        final Properties settings = new Properties();
        settings.put(ProducerConfig.CLIENT_ID_CONFIG, DRIVER_ID);
        settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        settings.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        settings.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, false);
        settings.put(ProducerConfig.BATCH_SIZE_CONFIG, 30);
        return settings;
    }
    
    private Properties avroSettings() {
        final Properties settings = settings();
        settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        // Disable the default behavior of represent java strings as new type in Avro
        settings.put("avro.remove.java.properties", true);
        return settings;
    }
    
    public static void main(String[] args) {
        ProducerWithSchemaV2 produce = new ProducerWithSchemaV2();
        produce.sendAvroProducer(10);
    }

    void sendAvroProducer(int nb) {
        LOGGER.info("Starting Arvo Producer");
        try (KafkaProducer<String, CloudEventBase> producer = new KafkaProducer<>(avroSettings())) {
            // Produce both 
            for (int i=0; i < nb; i++) {
                String key = Integer.toString(count);
                CloudEventBase value = CloudEventBase.newBuilder()
                    .setId(Integer.toString(i))
                    .setSource(DRIVER_ID)
                    .setTimestamp(0)
                    .build();
                if (i%2==0) {
                    ExampleEventRecord1 dataRecord = ExampleEventRecord1.newBuilder()
                    .setCounter(i)
                    .setMessage("Hello World")
                        .build();
                    value.put("data", dataRecord);
                } else {
                    ExampleEventRecord2 dataRecord = ExampleEventRecord2.newBuilder()
                        .setCounter(i)
                        .setLog("Logging \"Hello World\"")
                        .setSeverity(Severity.INFO)
                        .build();
                    value.put("data", dataRecord);
                }
                ProducerRecord<String, CloudEventBase> producerRecord = new ProducerRecord<>(TOPIC, key, value);
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
