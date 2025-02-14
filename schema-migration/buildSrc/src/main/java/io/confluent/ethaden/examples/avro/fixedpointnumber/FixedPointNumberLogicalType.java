package io.confluent.ethaden.examples.avro.fixedpointnumber;

import org.apache.avro.LogicalType;
import org.apache.avro.Schema;

public class FixedPointNumberLogicalType extends LogicalType {
    static final FixedPointNumberLogicalType INSTANCE = new FixedPointNumberLogicalType();

    private FixedPointNumberLogicalType() {
        super(FixedPointNumberConversion.LOGICAL_TYPE_NAME);
    }

    @Override
    public void validate(Schema schema) {
        super.validate(schema);
        if (schema.getType() != Schema.Type.STRING) {
            throw new IllegalArgumentException(
                    "Logical type '"+FixedPointNumberConversion.LOGICAL_TYPE_NAME+"' must be backed by string");
        }
    }
}
