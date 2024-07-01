plugins {
    id("java-application-conventions")
    id("kafka-java-conventions")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.3.0"
}

val avroVersion = "1.11.0"

dependencies {
}

application {
    mainClass.set("io.confluent.ethaden.examples.schemaregistry.ConsumerWithReflection")
}

avro {
    setCreateSetters(false)
}