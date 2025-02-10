plugins {
    id("java-common-conventions")
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:7.8.0-ce")
    implementation("io.confluent:kafka-streams-avro-serde:7.8.0")
}