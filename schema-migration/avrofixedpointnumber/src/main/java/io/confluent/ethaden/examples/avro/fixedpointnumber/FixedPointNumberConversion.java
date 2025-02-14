package io.confluent.ethaden.examples.avro.fixedpointnumber;

import java.math.BigDecimal;
import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;

public class FixedPointNumberConversion extends Conversion<BigDecimal> {
    public static final String LOGICAL_TYPE_NAME = "fixedpointnumber";

    //This conversion operates on ByteBuffer and returns ByteBuffer
    @Override
    public Class<BigDecimal> getConvertedType() { return BigDecimal.class; }

    @Override
    public String getLogicalTypeName() { return LOGICAL_TYPE_NAME; }

    @Override
    public Schema getRecommendedSchema() {
        return FixedPointNumberLogicalType.INSTANCE.addToSchema(Schema.create(Schema.Type.STRING));
    }

    @Override
    public CharSequence toCharSequence(BigDecimal value, Schema schema, LogicalType type) {
        return value.toString();
    }
    @Override
    public BigDecimal fromCharSequence(CharSequence value, Schema schema, LogicalType type)
    {
        return new BigDecimal(value.toString());
    }
}
