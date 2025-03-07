#!/bin/bash
echo "Using target schema with decimal logical type"
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
# Note: These rules assume a precision of 4 and a scale of 2 for the decimal
export MEASUREMENT2_DECIMAL_SCHEMA_RAW=$(cat << 'eof'
{   schema: $schema,
    "metadata": { "properties": { "application.major.version": "2" } },
    "ruleSet": {
        "migrationRules": [
            {
            "name": "upgradeFloatValueToDecimal",
            "kind": "TRANSFORM",
            "type": "JSONATA",
            "mode": "UPGRADE",
            "expr": "$merge([$, {'value': $round($power(10,2)*$number(value))%$power(10,4) }])",
            "disabled": true
            },
            {
            "name": "downgradeDecimalValueToFloat",
            "kind": "TRANSFORM",
            "type": "JSONATA",
            "mode": "DOWNGRADE",
            "expr": "$merge([$, {'value': $parseInteger(value, )/$power(10,2) }])",
            "disabled": true
            }
        ]
    }
}
eof
)
export MEASUREMENT2_DECIMAL_SCHEMA=$(jq -n --rawfile schema avro/measurement-v2-decimal.avsc "${MEASUREMENT2_DECIMAL_SCHEMA_RAW}")

# Register schemas
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data "$MEASUREMENT1_SCHEMA" \
http://localhost:8081/subjects/measurements-value/versions
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data "$MEASUREMENT2_DECIMAL_SCHEMA" \
http://localhost:8081/subjects/measurements-value/versions

# Produce messages with Schema 1
./gradlew -p producer-schema-v1 run
# Produce messages with Schema 2
./gradlew -p producer-schema-v2-decimal run

