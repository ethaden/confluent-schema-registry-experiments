plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

val avroVersion = "1.12.0"

dependencies {
    compileOnly("org.apache.avro:avro:$avroVersion")
    implementation("com.github.davidmc24.gradle.plugin:gradle-avro-plugin:1.9.1")
}

repositories {
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("myPlugins") {
            id = "io.confluent.ethaden.examples.avro.fixedpointnumber"
            implementationClass = "io.confluent.ethaden.examples.avro.fixedpointnumber.AvroConventionPlugin"
        }
    }
}
