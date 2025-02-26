rootProject.name = "kafka-schema-registry-experiments-schema-migrations"

include("avrofixedpointnumber",
    "producer-schema-v1",
    "consumer-schema-v1",
    "producer-schema-v2",
    "consumer-schema-v2",
    "producer-schema-v2-custom-logicaltype",
    "consumer-schema-v2-custom-logicaltype"
)
