package org.yamcs.studio.core.pvmanager;

import java.util.logging.Logger;

import org.epics.pvmanager.ChannelWriteCallback;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.MultiplexedChannelHandler;
import org.epics.pvmanager.ValueCache;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Rest.RestDataSource;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.PVConnectionInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsPVReader;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.vtype.YamcsVTypeAdapter;
import org.yamcs.studio.core.web.ResponseHandler;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.xtce.BooleanParameterType;
import org.yamcs.xtce.EnumeratedParameterType;
import org.yamcs.xtce.FloatParameterType;
import org.yamcs.xtce.IntegerParameterType;
import org.yamcs.xtce.Parameter;
import org.yamcs.xtce.ParameterType;
import org.yamcs.xtce.StringParameterType;

import com.google.protobuf.MessageLite;

/**
 * Supports writable Software parameters
 */
public class SoftwareParameterChannelHandler extends MultiplexedChannelHandler<PVConnectionInfo, ParameterValue>
        implements YamcsPVReader, StudioConnectionListener {

    private WebSocketRegistrar webSocketClient = null;
    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(SoftwareParameterChannelHandler.class.getName());

    public SoftwareParameterChannelHandler(String channelName) {
        super(channelName);
        YamcsPlugin.getDefault().addStudioConnectionListener(this);
    }

    @Override
    public void processConnectionInfo(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        log.info("processConnectionInfo called on " + getChannelName());
        this.disconnect();
        this.webSocketClient = webSocketClient;
        connect();
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
    public void disconnect() { // Interpret this as an unsubscribe
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
                && info.parameter.getDataSource() == RestDataSource.LOCAL;
    }

    @Override
    protected boolean isWriteConnected(PVConnectionInfo info) {
        System.out.println("Called isWriteConnected " + info);
        return isConnected(info);
    }

    private static Value toValue(Parameter parameter, String stringValue) {
        ParameterType ptype = parameter.getParameterType();
        if (ptype instanceof StringParameterType || ptype instanceof EnumeratedParameterType) {
            return Value.newBuilder().setType(Type.STRING).setStringValue(stringValue).build();
        } else if (ptype instanceof IntegerParameterType) {
            return Value.newBuilder().setType(Type.UINT64).setUint64Value(Long.parseLong(stringValue)).build();
        } else if (ptype instanceof FloatParameterType) {
            return Value.newBuilder().setType(Type.DOUBLE).setDoubleValue(Double.parseDouble(stringValue)).build();
        } else if (ptype instanceof BooleanParameterType) {
            return Value.newBuilder().setType(Type.BOOLEAN).setBooleanValue(Boolean.parseBoolean(stringValue)).build();
        }
        return null;
    }

    @Override
    protected void write(Object newValue, ChannelWriteCallback callback) {
        Parameter p = YamcsPlugin.getDefault().getMdb().getParameter(YamcsPlugin.getDefault().getMdbNamespace(), getChannelName());
        ParameterData pdata = ParameterData.newBuilder().addParameter(ParameterValue.newBuilder()
                .setId(toNamedObjectId())
                .setEngValue(toValue(p, (String) newValue))).build();

        RestClient client = YamcsPlugin.getDefault().getRestClient();
        if (client == null)
        {
            callback.channelWritten(new Exception("Client is disconnected from Yamcs server"));
            return;
        }

        client.setParameters(pdata, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                // Report success
                callback.channelWritten(null);
            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
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
        if (info.parameter != null && info.parameter.getDataSource() != RestDataSource.LOCAL) {
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
