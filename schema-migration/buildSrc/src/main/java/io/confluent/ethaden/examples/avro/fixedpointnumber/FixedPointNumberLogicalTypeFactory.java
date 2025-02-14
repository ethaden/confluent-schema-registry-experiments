package io.confluent.ethaden.examples.avro.fixedpointnumber;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;

public class FixedPointNumberLogicalTypeFactory implements LogicalTypes.LogicalTypeFactory{
    @Override
    public LogicalType fromSchema(Schema schema) {
        return FixedPointNumberLogicalType.INSTANCE;
    } 
}
