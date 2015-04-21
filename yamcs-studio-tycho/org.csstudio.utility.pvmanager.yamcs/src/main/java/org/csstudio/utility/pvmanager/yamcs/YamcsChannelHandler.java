package org.csstudio.utility.pvmanager.yamcs;

import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.WebSocketRegistrar;
import org.csstudio.platform.libs.yamcs.YamcsPVReader;
import org.csstudio.platform.libs.yamcs.YamcsPlugin;
import org.csstudio.platform.libs.yamcs.vtype.YamcsVTypeAdapter;
import org.epics.pvmanager.ChannelWriteCallback;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.MultiplexedChannelHandler;
import org.epics.pvmanager.ValueCache;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.xtce.DataSource;
import org.yamcs.xtce.Parameter;
import org.yamcs.xtce.XtceDb;

/**
 * Supports read-only PVs. Would be good if one day CSS added support for this at the PV-level,
 * rather than at the Datasource level. Then we wouldn't have to split out the software parameters
 * under a different scheme.
 */
public class YamcsChannelHandler extends MultiplexedChannelHandler<Boolean, ParameterValue> implements YamcsPVReader {

    private WebSocketRegistrar webSocketClient;
    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(YamcsChannelHandler.class.getName());

    public YamcsChannelHandler(String channelName, WebSocketRegistrar webSocketClient) {
        super(channelName);
        this.webSocketClient = webSocketClient;
    }

    @Override
    protected void connect() {
        log.info("Connect called on " + getChannelName());
        XtceDb mdb = YamcsPlugin.getDefault().getMdb();
        if (mdb != null) { // MDB might not have arrived yet
            Parameter p = mdb.getParameter(YamcsPlugin.getDefault().getMdbNamespace(), getChannelName());
            /*
             * Check that it's not actually a software parameter, because we don't want leaking
             * between the datasource schemes (the web socket client wouldn't make the distinction).
             */
            if (p != null && p.getDataSource() != DataSource.LOCAL) {
                webSocketClient.connectPVReader(this);
                processConnection(Boolean.TRUE); // TODO should call this from outside, on connection.
            } else {
                reportExceptionToAllReadersAndWriters(new IllegalArgumentException("Not a valid parameter channel: '" + getChannelName() + "'"));
            }
        }
    }

    @Override
    public String getPVName() {
        return getChannelName();
    }

    /**
     * This gets called when a channel has no more active readers. This could also happen while in
     * the same OPI runtime session. So don't close the websocket here.
     */
    @Override
    protected void disconnect() { // Interpret this as an unsubscribe
        log.info("Disconnect called on " + getChannelName());
        webSocketClient.disconnectPVReader(this);
    }

    @Override
    protected boolean isConnected(Boolean connected) {
        return connected != null && connected.booleanValue();
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
    protected DataSourceTypeAdapter<Boolean, ParameterValue> findTypeAdapter(ValueCache<?> cache, Boolean connection) {
        return TYPE_ADAPTER;
    }

    @Override
    public void signalYamcsConnected() {
        processConnection(Boolean.TRUE);
    }

    @Override
    public void signalYamcsDisconnected() {
        processConnection(Boolean.FALSE);
    }

    @Override
    public void reportException(Exception e) { // Expose protected method
        reportExceptionToAllReadersAndWriters(e);
    }
}
