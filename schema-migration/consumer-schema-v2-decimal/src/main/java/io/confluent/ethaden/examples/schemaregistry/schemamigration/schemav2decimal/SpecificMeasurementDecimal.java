package io.confluent.ethaden.examples.schemaregistry.schemamigration.schemav2decimal;

import java.math.BigDecimal;

public record SpecificMeasurementDecimal(String name, BigDecimal value, String unit) {
}
