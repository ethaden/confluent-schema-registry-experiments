#!/bin/bash
# Start from scratch:
# Delete topic
docker compose exec broker kafka-topics --bootstrap-server broker:9092 --topic measurements --delete
# Delete all schemas
curl schema-registry:8081/subjects/measurements-value/versions | jq .[] | xargs -L1 -I'{}' curl -X DELETE -H "Content-Type: application/vnd.schemaregistry.v1+json" 'http://localhost:8081/subjects/measurements-value/versions/{}'

# Create the topic
docker compose exec broker kafka-topics --bootstrap-server broker:9092 --topic measurements --create

# Configure the subject for advanced application version handling:

curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data '{ "compatibilityGroup": "application.major.version" }' \
http://localhost:8081/config/measurements-value

# Prepare payload for curl for both schemas
export MEASUREMENT1_SCHEMA_RAW=$(cat << 'eof'
{schema: $schema,
"metadata": { "properties": { "application.major.version": "1" } }
}
eof
)
export MEASUREMENT1_SCHEMA=$(jq -n --rawfile schema avro/measurement-v1.avsc "${MEASUREMENT1_SCHEMA_RAW}")
# Schema 2
export MEASUREMENT2_SCHEMA_RAW=$(cat << 'eof'
{   schema: $schema,
    "metadata": { "properties": { "application.major.version": "2" } },
    "ruleSet": {
        "migrationRules": [
            {
            "name": "upgradeFloatValueToString",
            "kind": "TRANSFORM",
            "type": "JSONATA",
            "mode": "UPGRADE",
            "expr": "$merge([$, {'value': $string(Measurement.value)}])",
            "disabled": false
            },
            {
            "name": "downgradeStringValueToFloat",
            "kind": "TRANSFORM",
            "type": "JSONATA",
            "mode": "DOWNGRADE",
            "expr": "$merge([$, {'value': $number(Measurement.value)}])",
            "disabled": false
            }
        ]
    }
}
eof
)
export MEASUREMENT2_SCHEMA=$(jq -n --rawfile schema avro/measurement-v2.avsc "${MEASUREMENT2_SCHEMA_RAW}")

# Register schemas
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data "$MEASUREMENT1_SCHEMA" \
http://localhost:8081/subjects/measurements-value/versions
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data "$MEASUREMENT2_SCHEMA" \
http://localhost:8081/subjects/measurements-value/versions

# Produce messages with Schema 1
./gradlew -p producer-schema-v1 run
# Produce messages with Schema 2
./gradlew -p producer-schema-v2 run

