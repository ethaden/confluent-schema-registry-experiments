package io.confluent.ethaden.examples.schemaregistry.schemamigration.schemav2decimal;

import models.avro.Measurement;

import java.math.BigDecimal;

public class MeasurementConverterDecimal {
public static Measurement toAvro(SpecificMeasurementDecimal measurement) {
    return Measurement.newBuilder()
            .setName(measurement.name())
            .setValue(measurement.value())
            .setUnit(measurement.unit())
            .build();
}

public static SpecificMeasurementDecimal fromAvro(Measurement avroMeasurement) {
    return new SpecificMeasurementDecimal(avroMeasurement.getName(),
            avroMeasurement.getValue(),
            avroMeasurement.getUnit());
    }
}
