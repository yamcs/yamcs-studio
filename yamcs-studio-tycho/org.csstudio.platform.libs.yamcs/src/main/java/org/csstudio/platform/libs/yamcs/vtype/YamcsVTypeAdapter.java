package org.csstudio.platform.libs.yamcs.vtype;

import org.csstudio.platform.libs.yamcs.PVConnectionInfo;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.ValueCache;
import org.yamcs.protobuf.Pvalue.ParameterValue;

public class YamcsVTypeAdapter implements DataSourceTypeAdapter<PVConnectionInfo, ParameterValue> {

    @Override
    public int match(ValueCache<?> cache, PVConnectionInfo info) {
        return 1;
    }

    @Override
    public Object getSubscriptionParameter(ValueCache<?> cache, PVConnectionInfo info) {
        throw new UnsupportedOperationException(); // Don't expect this on MultiplexedDataSources
    }

    /**
     * Takes the information in the message and updates the cache
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean updateCache(ValueCache cache, PVConnectionInfo info, ParameterValue pval) {
        cache.writeValue(YamcsVType.fromYamcs(pval));
        return true;
    }
}
