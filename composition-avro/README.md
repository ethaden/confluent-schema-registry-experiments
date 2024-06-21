# Experiments with Confluent Schema Registry

## Building the code

Building the code using gradle:

```bash
./gradlew build
```

Potentially, you might need to update gradle first:

```bash
gradle wrapper
```

## Running the code

First, start the docker environment:

```bash
docker compose up -d
```

First, set the compatilibity level of our subject to "FORWARD_TRANSITIVE":

```bash
curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data '{"compatibility": "FORWARD_TRANSITIVE"}' \
http://localhost:8081/config/topic-value
```

Register the schemas. Initially, there is exactly one type allowed for the `data` (except for the `null` value, of course) which is `io.confluent.example.avro.cloudevents.ExampleEventRecord1`.
We need to register this schema first using the `RecordNameStrategy` (Note: in this clean environment we assume that the versions of the referenced schemas are always `1`.
Please query and use the actual version of each referenced schema in a production environment):

```bash
SCHEMA="$(sed -e ':a' -e 'N' -e '$!ba' -e 's/\n//g' -e 's/[ \t]+//g' -e 's/\([\"]\)/\\\1/g' avro/example-event-record1.avsc)"
JSON_DATA=$(printf "{\"schema\": \"${SCHEMA}\"}\n")
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data "${JSON_DATA}" \
http://localhost:8081/subjects/io.confluent.example.avro.cloudevents.ExampleEventRecord1/versions
```

Then we can register the schema which refers to our `ExampleEventRecord1`, here using the `TopicNameStrategy`:

```bash
SCHEMA="$(sed -e ':a' -e 'N' -e '$!ba' -e 's/\n//g' -e 's/[ \t]+//g' -e 's/\([\"]\)/\\\1/g' avro/cloud-event-base-v1.avsc)"
VERSION_ExampleEventRecord1=1
REFERENCES="[{\"name\": \"io.confluent.example.avro.cloudevents.ExampleEventRecord1\", \"subject\": \"io.confluent.example.avro.cloudevents.ExampleEventRecord1\", \"version\": ${VERSION_ExampleEventRecord1}}]"
JSON_DATA=$(printf "{\"schema\": \"${SCHEMA}\", \"references\": ${REFERENCES}}\n")
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data "${JSON_DATA}" \
http://localhost:8081/subjects/topic-value/versions
```

Now upload the second example schema:

```bash
SCHEMA="$(sed -e ':a' -e 'N' -e '$!ba' -e 's/\n//g' -e 's/[ \t]+//g' -e 's/\([\"]\)/\\\1/g' avro/example-event-record2.avsc)"
JSON_DATA=$(printf "{\"schema\": \"${SCHEMA}\"}\n")
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
--data "${JSON_DATA}" \
http://localhost:8081/subjects/io.confluent.example.avro.cloudevents.ExampleEventRecord2/versions
```

You can get the list of all subjects in the schema registry like this:

```bash
curl http://localhost:8081/subjects
```

A (optionally pretty printed with `jq`) list of all schemas can be retrieved like this:

```bash
curl http://localhost:8081/schemas | jq
```


Run producer with gradle:

```bash
./gradlew -p java-producer-with-schema-v1 run
```

Run consumer with:

```bash
./gradlew -p java-consumer run
```

Run a consumer compiled with an older version of the avro schema to see if it can successfully consume the messages produced with the newer schema:

```bash
./gradlew -p java-consumer-old-avro run
```

Notice how the consumer will work, but does not output the extra field `theNewName` which was added when updating the schema.


## Helpful tools

### Schema Registry


Get all known versions:

```bash
curl -H "Content-Type: application/vnd.schemaregistry.v1+json" http://localhost:8081/subjects/topic-value/versions
```

Inspect a specific version (here: version 1):

```bash
curl -H "Content-Type: application/vnd.schemaregistry.v1+json" http://localhost:8081/subjects/topic-value/versions/1
```

Soft delete a specific version:

```bash
curl -X DELETE -H "Content-Type: application/vnd.schemaregistry.v1+json" http://localhost:8081/subjects/topic-value/versions/1
```

Permanently delete a specific version (you need to soft delete first):

```bash
curl -X DELETE -H "Content-Type: application/vnd.schemaregistry.v1+json" http://localhost:8081/subjects/topic-value/versions/1?permanent=true
```


### CLI Consumer

Read messages via CLI tools, using standard console consumer:

```bash
docker compose exec kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic topic --from-beginning
```

Read messages via avro console consumer (please install locally as the avro console conumser is not contained in the docker image):

```bash
kafka-avro-console-consumer --bootstrap-server localhost:9092 --property schema.registry.url=http://localhost:8081 --topic topic --from-beginning
```

You might want to delete the topic to start fresh between tests:

```bash
docker compose exec kafka  kafka-topics --bootstrap-server kafka:9092 --delete --topic topic
```

Alternatively, if you just want to consume the same messages again with the Java consumer, just reset the consumer groups offset:

```bash
docker compose exec kafka kafka-consumer-groups --bootstrap-server kafka:9092 --group Consumer --reset-offsets --to-earliest --topic topic --execute
```

You can view the offsets by running:

```bash
docker compose exec kafka kafka-consumer-groups --bootstrap-server kafk:9092 --group Consumer --describe
```

## Experimenting

## Shutting down, deleting containers

```bash
docker compose down -v
```

