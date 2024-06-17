plugins {
    id("java-common-conventions")
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:7.1.1-ccs")
    implementation("io.confluent:kafka-streams-avro-serde:7.1.1")
}