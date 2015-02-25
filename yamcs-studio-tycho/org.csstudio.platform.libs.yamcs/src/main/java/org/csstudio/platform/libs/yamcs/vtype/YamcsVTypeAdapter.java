package org.csstudio.platform.libs.yamcs.vtype;

import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.ValueCache;
import org.yamcs.protobuf.ParameterValue;

public class YamcsVTypeAdapter implements DataSourceTypeAdapter<Boolean, ParameterValue> {

    @Override
    public int match(ValueCache<?> cache, Boolean connection) {
        return 1;
    }

    @Override
    public Object getSubscriptionParameter(ValueCache<?> cache, Boolean connection) {
        throw new UnsupportedOperationException(); // Don't expect this on MultiplexedDataSources
    }

    /**
     * Takes the information in the message and updates the cache
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean updateCache(ValueCache cache, Boolean connection, ParameterValue pval) {
        switch (pval.getEngValue().getType()) {
        case UINT32:
            cache.writeValue(new Uint32VType(pval));
            break;
        case SINT32:
            cache.writeValue(new Sint32VType(pval));
            break;
        case UINT64:
            cache.writeValue(new Uint64VType(pval));
            break;
        case SINT64:
            cache.writeValue(new Sint64VType(pval));
            break;
        case FLOAT:
            cache.writeValue(new FloatVType(pval));
            break;
        case DOUBLE:
            cache.writeValue(new DoubleVType(pval));
            break;
        case BOOLEAN:
            cache.writeValue(new BooleanVType(pval));
            break;
        case STRING:
            cache.writeValue(new StringVType(pval));
            break;
        case BINARY:
            cache.writeValue(new BinaryVType(pval));
            break;
        case TIMESTAMP:
            throw new UnsupportedOperationException("No support for timestamp pvals");
        }
        return true;
    }
}
