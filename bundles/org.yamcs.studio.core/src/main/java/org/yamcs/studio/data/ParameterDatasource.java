package org.yamcs.studio.data;

import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.vtype.VType;
import org.yamcs.studio.data.yamcs.YamcsSubscriptionService;

/**
 * A datasource that provides parameter data coming from Yamcs
 */
public class ParameterDatasource implements Datasource {

    private YamcsSubscriptionService yamcsSubscription = YamcsPlugin.getService(YamcsSubscriptionService.class);

    @Override
    public boolean supportsPVName(String pvName) {
        return true; // This datasource is used as catch-all for anything that other datasources don't support
    }

    @Override
    public boolean isConnected(IPV pv) {
        return yamcsSubscription.isSubscriptionAvailable();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return false;
    }

    @Override
    public void writeValue(IPV pv, Object value, WriteCallback callback) {
    }

    @Override
    public VType getValue(IPV pv) {
        return yamcsSubscription.getValue(pv.getName());
    }

    @Override
    public void onStarted(IPV pv) {
        yamcsSubscription.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        yamcsSubscription.unregister(pv);
    }
}
