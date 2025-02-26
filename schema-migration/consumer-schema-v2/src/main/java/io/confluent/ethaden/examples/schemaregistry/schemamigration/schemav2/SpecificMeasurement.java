package io.confluent.ethaden.examples.schemaregistry.schemamigration.schemav2;

import java.math.BigDecimal;

public record SpecificMeasurement(String name, BigDecimal value, String unit) {
}
