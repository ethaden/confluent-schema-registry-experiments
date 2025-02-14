import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.confluent.ethaden.examples.avro.fixedpointnumber.FixedPointNumberLogicalTypeFactory
import io.confluent.ethaden.examples.avro.fixedpointnumber.FixedPointNumberLogicalType
import io.confluent.ethaden.examples.avro.fixedpointnumber.FixedPointNumberConversion

/*buildscript {
    dependencies {
        classpath("../avrofixedpointnumber/build/libs/avrofixedpointnumber.jar")
    }
}*/

plugins {
    id("java-application-conventions")
    id("kafka-java-conventions")
    id("com.github.davidmc24.gradle.plugin.avro")
    id("com.github.ben-manes.versions") version "0.52.0"
    id("io.confluent.ethaden.examples.avro.fixedpointnumber")
}

val avroVersion = "1.12.0"

dependencies {
    compileOnly("org.apache.avro:avro-tools:$avroVersion")
    implementation(project(":avrofixedpointnumber"))
}

application {
    mainClass.set("io.confluent.ethaden.examples.schemaregistry.schemamigration.schemav2.ProducerV2")
}

avro {
    setCreateSetters(false)
    //stringType("String")
    //outputCharacterEncoding("UTF-8")
    logicalTypeFactory("fixedpointnumber", "io.confluent.ethaden.examples.avro.fixedpointnumber.FixedPointNumberLogicalTypeFactory")
    customConversion("io.confluent.ethaden.examples.avro.fixedpointnumber.FixedPointNumberConversion")
}

// Used by dependency update plugin
fun String.isNonStable(): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA", "ccs").any { uppercase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(this)
  return isStable.not()
}

// disallow release candidates as upgradable versions from stable versions
tasks.withType<DependencyUpdatesTask> {
  resolutionStrategy {
    componentSelection {
      all {
        if (candidate.version.isNonStable() && !currentVersion.isNonStable()) {
          reject("Release candidate")
        }
      }
    }
  }
}
