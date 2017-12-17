package org.yamcs.studio.css.core.pvmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.diirt.datasource.ChannelWriteCallback;
import org.diirt.datasource.DataSourceTypeAdapter;
import org.diirt.datasource.MultiplexedChannelHandler;
import org.diirt.datasource.ValueCache;
import org.yamcs.protobuf.Mdb.DataSourceType;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ManagementCatalogue;
import org.yamcs.studio.css.core.PVCatalogue;
import org.yamcs.studio.css.core.vtype.YamcsVTypeAdapter;

/**
 * Supports read-only PVs. Would be good if one day CSS added support for this at the PV-level, rather than at the
 * Datasource level. Then we wouldn't have to split out the software parameters under a different scheme.
 */
public class ParameterChannelHandler extends MultiplexedChannelHandler<PVConnectionInfo, ParameterValue>
        implements YamcsPVReader, YamcsConnectionListener, InstanceListener {

    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(ParameterChannelHandler.class.getName());
    private NamedObjectId id;

    public ParameterChannelHandler(String channelName) {
        super(channelName);
        id = NamedObjectId.newBuilder().setName(channelName).build();
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
        ManagementCatalogue.getInstance().addInstanceListener(this);
    }

    @Override
    public NamedObjectId getId() {
        return id;
    }

    @Override
    public void onYamcsConnected() {
        log.fine("onStudioConnect called on " + getChannelName());
        connect();
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        // The server will normally transfer our parameter subscription,
        // but don't necessarily trust that right now. So reconnect all pvs
        // manually
        // (probably handled by OPIUtils.refresh in org.yamcs.studio.css.core.Activator)
        /// disconnect();
        /// connect();
    }

    @Override
    public void onYamcsDisconnected() {
        disconnect(); // Unregister PV
    }

    @Override
    protected void connect() {
        log.fine("PV connect on " + getChannelName());
        PVCatalogue.getInstance().register(this);
    }

    @Override
    protected void disconnect() { // Interpret this as an unsubscribe
        log.fine("PV disconnect on " + getChannelName());
        PVCatalogue catalogue = PVCatalogue.getInstance();
        if (catalogue != null) { // Conservative, could be null at shutdown
            PVCatalogue.getInstance().unregister(this);
        }
    }

    /**
     * Returns true when this channelhandler is connected to an open websocket and subscribed to a valid parameter.
     */
    @Override
    protected boolean isConnected(PVConnectionInfo info) {
        boolean sysParam = getId().getName().startsWith("/yamcs"); // These are always valid in yamcs world
        boolean nonLocal = info.parameter != null && info.parameter.getDataSource() != DataSourceType.LOCAL;
        return info.connected && (sysParam || nonLocal);
    }

    @Override
    protected void write(Object newValue, ChannelWriteCallback callback) {
        throw new UnsupportedOperationException("Channel write not supported");
    }

    /**
     * Process a parameter value update to be sent to the display
     */
    @Override
    public void processParameterValue(ParameterValue pval) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("Incoming value %s", pval));
        }
        processMessage(pval);
    }

    @Override
    protected DataSourceTypeAdapter<PVConnectionInfo, ParameterValue> findTypeAdapter(ValueCache<?> cache,
            PVConnectionInfo info) {
        return TYPE_ADAPTER;
    }

    @Override
    public void processConnectionInfo(PVConnectionInfo info) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("Processing %s", info));
        }
        /*
         * Check that it's not actually a software parameter, because we don't want leaking between the datasource
         * schemes (the web socket client wouldn't make the distinction).
         */
        if (info.parameter != null && info.parameter.getDataSource() == DataSourceType.LOCAL) {
            reportExceptionToAllReadersAndWriters(new IllegalArgumentException(
                    "Not a valid parameter channel: '" + getChannelName() + "'"));
        }

        // Call the real (but protected) method
        processConnection(info);
    }

    @Override
    public void reportException(Exception e) { // Expose protected method
        reportExceptionToAllReadersAndWriters(e);
    }
}
