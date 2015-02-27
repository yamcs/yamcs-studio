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
        cache.writeValue(YamcsVType.fromYamcs(pval));
        return true;
    }
}
