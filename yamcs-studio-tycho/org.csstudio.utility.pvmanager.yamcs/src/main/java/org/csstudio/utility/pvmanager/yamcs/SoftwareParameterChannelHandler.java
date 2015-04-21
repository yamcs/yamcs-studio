package org.csstudio.utility.pvmanager.yamcs;

import java.util.logging.Logger;

import org.csstudio.platform.libs.yamcs.YamcsPVReader;
import org.csstudio.platform.libs.yamcs.YamcsPlugin;
import org.csstudio.platform.libs.yamcs.YamcsUtils;
import org.csstudio.platform.libs.yamcs.WebSocketRegistrar;
import org.csstudio.platform.libs.yamcs.vtype.YamcsVTypeAdapter;
import org.csstudio.platform.libs.yamcs.web.ResponseHandler;
import org.csstudio.platform.libs.yamcs.web.RestClient;
import org.epics.pvmanager.ChannelWriteCallback;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.MultiplexedChannelHandler;
import org.epics.pvmanager.ValueCache;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.xtce.BooleanParameterType;
import org.yamcs.xtce.DataSource;
import org.yamcs.xtce.EnumeratedParameterType;
import org.yamcs.xtce.FloatParameterType;
import org.yamcs.xtce.IntegerParameterType;
import org.yamcs.xtce.Parameter;
import org.yamcs.xtce.ParameterType;
import org.yamcs.xtce.StringParameterType;
import org.yamcs.xtce.XtceDb;

import com.google.protobuf.MessageLite;

/**
 * Supports writable Software parameters
 */
public class SoftwareParameterChannelHandler extends MultiplexedChannelHandler<Boolean, ParameterValue> implements YamcsPVReader {

    private WebSocketRegistrar webSocketClient;
    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(SoftwareParameterChannelHandler.class.getName());

    public SoftwareParameterChannelHandler(String channelName, WebSocketRegistrar webSocketClient) {
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
            if (p != null && p.getDataSource() == DataSource.LOCAL) {
                webSocketClient.connectPVReader(this);
                processConnection(Boolean.TRUE); // TODO should call this from outside, on connection.
            } else {
                reportExceptionToAllReadersAndWriters(new IllegalArgumentException("Not a valid software parameter channel: '" + getChannelName()
                        + "'"));
            }
        }
    }

    @Override
    protected boolean isWriteConnected(Boolean payload) {
        System.out.println("Called isWriteConnected " + payload);
        return payload != null && payload.booleanValue();
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
        Parameter p = YamcsPlugin.getDefault().getMdb().getParameter(YamcsPlugin.getDefault().getMdbNamespace(), getChannelName());
        ParameterData pdata = ParameterData.newBuilder().addParameter(ParameterValue.newBuilder()
                .setId(YamcsUtils.toNamedObjectId(getPVName()))
                .setEngValue(toValue(p, (String) newValue))).build();

        RestClient client = YamcsPlugin.getDefault().getRestClient();
        client.setParameters(pdata, new ResponseHandler() {
            @Override
            public void onMessage(MessageLite responseMsg) {
                // Report success
                System.out.println("success!");
                callback.channelWritten(null);
            }

            @Override
            public void onException(Exception e) {
                System.out.println("failure");
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
