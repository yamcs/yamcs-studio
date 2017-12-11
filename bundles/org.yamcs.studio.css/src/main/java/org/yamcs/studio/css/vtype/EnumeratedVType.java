package org.yamcs.studio.css.vtype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.diirt.vtype.VEnum;
import org.yamcs.protobuf.Mdb.EnumValue;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.css.pvmanager.PVConnectionInfo;

public class EnumeratedVType extends YamcsVType implements VEnum {

    private PVConnectionInfo info;

    public EnumeratedVType(PVConnectionInfo info, ParameterValue pval) {
        super(pval);
        this.info = info;
    }

    @Override
    public int getIndex() {
        Value rawValue = pval.getRawValue();
        switch (rawValue.getType()) {
        case UINT32:
            return rawValue.getUint32Value();
        case UINT64:
            return (int) rawValue.getUint64Value();
        case SINT32:
            return rawValue.getSint32Value();
        case SINT64:
            return (int) rawValue.getSint64Value();
        case FLOAT:
            return (int) rawValue.getFloatValue();
        case DOUBLE:
            return (int) rawValue.getDoubleValue();
        case STRING:
            long longValue = Long.decode(rawValue.getStringValue());
            return (int) longValue;
        default:
            return -1;
        }
    }

    @Override
    public String getValue() {
        return pval.getEngValue().getStringValue();
    }

    @Override
    public List<String> getLabels() {
        List<EnumValue> enumValues = info.parameter.getType().getEnumValueList();
        if (enumValues != null) {
            List<String> labels = new ArrayList<>(enumValues.size());
            for (EnumValue enumValue : enumValues) {
                labels.add(enumValue.getLabel());
            }
            return labels;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString() {
        // Use String.valueOf, because it formats a nice "null" string
        // in case it is null
        return String.valueOf(pval.getEngValue().getStringValue());
    }
}
