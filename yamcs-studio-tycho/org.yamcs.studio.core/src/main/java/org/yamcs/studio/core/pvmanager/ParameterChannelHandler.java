package org.yamcs.studio.core.pvmanager;

import java.util.logging.Logger;

import org.epics.pvmanager.ChannelWriteCallback;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.MultiplexedChannelHandler;
import org.epics.pvmanager.ValueCache;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Rest.RestDataSource;
import org.yamcs.studio.core.PVConnectionInfo;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPVReader;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.vtype.YamcsVTypeAdapter;

/**
 * Supports read-only PVs. Would be good if one day CSS added support for this at the PV-level,
 * rather than at the Datasource level. Then we wouldn't have to split out the software parameters
 * under a different scheme.
 */
public class ParameterChannelHandler extends MultiplexedChannelHandler<PVConnectionInfo, ParameterValue> implements YamcsPVReader {

    private WebSocketRegistrar webSocketClient;
    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(ParameterChannelHandler.class.getName());

    public ParameterChannelHandler(String channelName, WebSocketRegistrar webSocketClient) {
        super(channelName);
        this.webSocketClient = webSocketClient;
    }

    @Override
    public String getMdbNamespace() {
        return YamcsPlugin.getDefault().getMdbNamespace();
    }

    @Override
    public String getPVName() {
        return getChannelName();
    }

    @Override
    protected void connect() {
        log.info("Connect called on " + getChannelName());
        webSocketClient.register(this);
    }

    @Override
    protected void disconnect() { // Interpret this as an unsubscribe
        log.info("Disconnect called on " + getChannelName());
        webSocketClient.unregister(this);
    }

    /**
     * Returns true when this channelhandler is connected to an open websocket and subscribed to a
     * valid parameter.
     */
    @Override
    protected boolean isConnected(PVConnectionInfo info) {
        return info.webSocketOpen
                && info.parameter != null
                && info.parameter.getDataSource() != RestDataSource.LOCAL;
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
        log.fine(String.format("Incoming value %s", pval));
        processMessage(pval);
    }

    @Override
    protected DataSourceTypeAdapter<PVConnectionInfo, ParameterValue> findTypeAdapter(ValueCache<?> cache, PVConnectionInfo info) {
        return TYPE_ADAPTER;
    }

    @Override
    public void processConnectionInfo(PVConnectionInfo info) {
        log.fine(String.format("Processing %s", info));
        /*
         * Check that it's not actually a software parameter, because we don't want leaking between
         * the datasource schemes (the web socket client wouldn't make the distinction).
         */
        if (info.parameter != null && info.parameter.getDataSource() == RestDataSource.LOCAL) {
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
