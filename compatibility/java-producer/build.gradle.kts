import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("java-application-conventions")
    id("kafka-java-conventions")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    id("com.github.ben-manes.versions") version "0.52.0"
}

val avroVersion = "1.12.0"

dependencies {
    compileOnly("org.apache.avro:avro-tools:$avroVersion")
//    implementation(project(":utilities"))
}

application {
    mainClass.set("io.confluent.ethaden.examples.schemaregistry.Produce")
}

avro {
    setCreateSetters(false)
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
