package org.yamcs.studio.core.pvmanager;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvmanager.ChannelWriteCallback;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.MultiplexedChannelHandler;
import org.epics.pvmanager.ValueCache;
import org.yamcs.protobuf.Parameters.DataSourceType;
import org.yamcs.protobuf.Parameters.ParameterInfo;
import org.yamcs.protobuf.Parameters.ParameterTypeInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.vtype.YamcsVTypeAdapter;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;

import com.google.protobuf.MessageLite;

/**
 * Supports writable Software parameters
 */
public class SoftwareParameterChannelHandler extends MultiplexedChannelHandler<PVConnectionInfo, ParameterValue>
        implements YamcsPVReader, StudioConnectionListener {

    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(SoftwareParameterChannelHandler.class.getName());
    private static final List<String> TRUTHY = Arrays.asList("y", "true", "yes", "1");

    private NamedObjectId id;

    public SoftwareParameterChannelHandler(String channelName) {
        super(channelName);
        id = NamedObjectId.newBuilder().setName(channelName).build();
        ConnectionManager.getInstance().addStudioConnectionListener(this);
    }

    @Override
    public void onStudioConnect() {
        log.info("processConnectionInfo called on " + getChannelName());
        disconnect();
        connect();
    }

    @Override
    public void onStudioDisconnect() {
        disconnect(); // Unregister PV
    }

    @Override
    public NamedObjectId getId() {
        return id;
    }

    @Override
    protected void connect() {
        log.fine("PV connect on " + getChannelName());
        ParameterCatalogue.getInstance().register(this);
    }

    @Override
    public void disconnect() { // Interpret this as an unsubscribe
        log.fine("PV disconnect on " + getChannelName());
        ParameterCatalogue.getInstance().unregister(this);
    }

    /**
     * Returns true when this channelhandler is connected to an open websocket and subscribed to a
     * valid parameter.
     */
    @Override
    protected boolean isConnected(PVConnectionInfo info) {
        return info.connected
                && info.parameter != null
                && info.parameter.getDataSource() == DataSourceType.LOCAL;
    }

    @Override
    protected boolean isWriteConnected(PVConnectionInfo info) {
        return isConnected(info);
    }

    private static Value toValue(ParameterInfo p, String stringValue) {
        ParameterTypeInfo ptype = p.getType();
        if (ptype != null) {
            switch (ptype.getEngType()) {
            case "string":
            case "enumeration":
                return Value.newBuilder().setType(Type.STRING).setStringValue(stringValue).build();
            case "integer":
                return Value.newBuilder().setType(Type.UINT64).setUint64Value(Long.parseLong(stringValue)).build();
            case "float":
                return Value.newBuilder().setType(Type.DOUBLE).setDoubleValue(Double.parseDouble(stringValue)).build();
            case "boolean":
                boolean booleanValue = TRUTHY.contains(stringValue.toLowerCase());
                return Value.newBuilder().setType(Type.BOOLEAN).setBooleanValue(booleanValue).build();
            }
        }
        return null;
    }

    @Override
    protected void write(Object newValue, ChannelWriteCallback callback) {
        ParameterInfo p = ParameterCatalogue.getInstance().getParameterInfo(id);
        ParameterData pdata = ParameterData.newBuilder().addParameter(ParameterValue.newBuilder()
                .setId(id)
                .setEngValue(toValue(p, (String) newValue))).build();

        RestClient restClient = ConnectionManager.getInstance().getRestClient();
        if (restClient == null) {
            callback.channelWritten(new Exception("Client is disconnected from Yamcs server"));
            return;
        }

        restClient.setParameters(pdata, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                // Report success
                callback.channelWritten(null);
            }

            @Override
            public void onException(Exception e) {
                log.log(Level.SEVERE, "Could not write to parameter", e);
                callback.channelWritten(e);
            }
        });
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
        /*
         * Check that it's not actually a regular parameter, because we don't want leaking between
         * the datasource schemes (the web socket client wouldn't make the distinction).
         */
        if (info.parameter != null && info.parameter.getDataSource() != DataSourceType.LOCAL) {
            reportExceptionToAllReadersAndWriters(new IllegalArgumentException(
                    "Not a valid software parameter channel: '" + getChannelName() + "'"));
        }

        // Call the real (but protected) method
        processConnection(info);
    }

    @Override
    public void reportException(Exception e) { // Expose protected method
        reportExceptionToAllReadersAndWriters(e);
    }
}
