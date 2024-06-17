plugins {
    id("java-application-conventions")
    id("kafka-java-conventions")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.3.0"
}

val avroVersion = "1.11.0"

dependencies {
    compileOnly("org.apache.avro:avro-tools:$avroVersion")
//    implementation(project(":utilities"))
}

application {
    mainClass.set("io.confluent.ethaden.examples.schemaregistry.ProduceV1")
}

avro {
    setCreateSetters(false)
}