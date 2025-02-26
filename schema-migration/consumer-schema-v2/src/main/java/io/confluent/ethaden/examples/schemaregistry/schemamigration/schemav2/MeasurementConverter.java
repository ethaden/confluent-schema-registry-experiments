package io.confluent.ethaden.examples.schemaregistry.schemamigration.schemav2;

import models.avro.Measurement;

import java.math.BigDecimal;

public class MeasurementConverter {
public static Measurement toAvro(SpecificMeasurement measurement) {
    return Measurement.newBuilder()
            .setName(measurement.name())
            .setValue(measurement.value().toPlainString())
            .setUnit(measurement.unit())
            .build();
}

public static SpecificMeasurement fromAvro(Measurement avroMeasurement) {
    return new SpecificMeasurement(avroMeasurement.getName(),
            new BigDecimal(avroMeasurement.getValue()),
            avroMeasurement.getUnit());
    }
}
