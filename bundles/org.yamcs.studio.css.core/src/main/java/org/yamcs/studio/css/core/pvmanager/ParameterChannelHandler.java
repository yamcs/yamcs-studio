package org.yamcs.studio.css.core.pvmanager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.diirt.datasource.ChannelWriteCallback;
import org.diirt.datasource.DataSourceTypeAdapter;
import org.diirt.datasource.MultiplexedChannelHandler;
import org.diirt.datasource.ValueCache;
import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.protobuf.Mdb.DataSourceType;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.studio.core.YamcsConnectionListener;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.InstanceListener;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.css.core.PVCatalogue;
import org.yamcs.studio.css.core.vtype.YamcsVTypeAdapter;

public class ParameterChannelHandler extends MultiplexedChannelHandler<PVConnectionInfo, ParameterValue>
        implements YamcsConnectionListener, InstanceListener {

    private static final YamcsVTypeAdapter TYPE_ADAPTER = new YamcsVTypeAdapter();
    private static final Logger log = Logger.getLogger(ParameterChannelHandler.class.getName());
    private static final List<String> TRUTHY = Arrays.asList("y", "true", "yes", "1", "1.0");
    private NamedObjectId id;

    public ParameterChannelHandler(NamedObjectId id) {
        super(id.getName());
        this.id = id;
        YamcsPlugin.getDefault().addYamcsConnectionListener(this);
    }

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
        return info.connected && (sysParam || info.parameter != null);
    }

    @Override
    protected boolean isWriteConnected(PVConnectionInfo info) {
        return isConnected(info)
                && info.parameter != null
                && info.parameter.hasDataSource()
                && (info.parameter.getDataSource() == DataSourceType.LOCAL
                        || info.parameter.getDataSource() == DataSourceType.EXTERNAL1
                        || info.parameter.getDataSource() == DataSourceType.EXTERNAL2
                        || info.parameter.getDataSource() == DataSourceType.EXTERNAL3);
    }

    @Override
    protected void write(Object newValue, ChannelWriteCallback callback) {
        try {
            ParameterTypeInfo ptype = ParameterCatalogue.getInstance().getParameterTypeInfo(id);
            Value v = toValue(ptype, newValue);
            ProcessorClient processor = YamcsPlugin.getProcessorClient();
            String parameterName = id.getName();
            if (id.hasNamespace()) {
                parameterName = id.getNamespace() + "/" + parameterName;
            }
            processor.setValue(parameterName, v).whenComplete((data, e) -> {
                if (e != null) {
                    log.log(Level.SEVERE, "Could not write to parameter", e);
                    if (e instanceof Exception) {
                        callback.channelWritten((Exception) e);
                    } else {
                        callback.channelWritten(new ExecutionException(e));
                    }
                } else {
                    // Report success
                    callback.channelWritten(null);
                }
            });
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to write parameter value: " + newValue, e);
            return;
        }
    }

    private static Value toValue(ParameterTypeInfo ptype, Object value) {
        if (ptype != null) {
            switch (ptype.getEngType()) {
            case "string":
            case "enumeration":
                return Value.newBuilder().setType(Type.STRING).setStringValue(String.valueOf(value)).build();
            case "string[]":
            case "enumeration[]":
                Value.Builder stringValueArray = Value.newBuilder().setType(Type.ARRAY);
                for (Object objectValue : (Object[]) value) {
                    stringValueArray.addArrayValue(Value.newBuilder()
                            .setType(Type.STRING)
                            .setStringValue(String.valueOf(objectValue)));
                }
                return stringValueArray.build();
            case "integer":
                if (value instanceof Double) {
                    return Value.newBuilder().setType(Type.SINT64).setSint64Value(((Double) value).longValue()).build();
                } else {
                    return Value.newBuilder().setType(Type.SINT64).setSint64Value(Long.parseLong(String.valueOf(value)))
                            .build();
                }
            case "integer[]":
                Value.Builder intValueArray = Value.newBuilder().setType(Type.ARRAY);
                if (value instanceof int[]) {
                    for (int intValue : (int[]) value) {
                        intValueArray.addArrayValue(Value.newBuilder().setType(Type.UINT64).setUint64Value(intValue));
                    }
                } else {
                    for (Object objectValue : (Object[]) value) {
                        long longValue = Long.parseLong(String.valueOf(objectValue));
                        intValueArray.addArrayValue(Value.newBuilder().setType(Type.UINT64).setUint64Value(longValue));
                    }
                }
                return intValueArray.build();
            case "float":
                return Value.newBuilder().setType(Type.DOUBLE).setDoubleValue(Double.parseDouble(String.valueOf(value)))
                        .build();
            case "float[]":
                Value.Builder floatValueArray = Value.newBuilder().setType(Type.ARRAY);
                for (float floatValue : (float[]) value) {
                    floatValueArray.addArrayValue(Value.newBuilder().setType(Type.FLOAT).setFloatValue(floatValue));
                }
                return floatValueArray.build();
            case "boolean":
                boolean booleanValue = TRUTHY.contains(String.valueOf(value).toLowerCase());
                return Value.newBuilder().setType(Type.BOOLEAN).setBooleanValue(booleanValue).build();
            case "boolean[]":
                Value.Builder booleanValueArray = Value.newBuilder().setType(Type.ARRAY);
                for (Object objectValue : (Object[]) value) {
                    booleanValueArray.addArrayValue(Value.newBuilder()
                            .setType(Type.BOOLEAN)
                            .setBooleanValue(TRUTHY.contains(String.valueOf(objectValue).toLowerCase())));
                }
                return booleanValueArray.build();
            }
        }
        return null;
    }

    /**
     * Process a parameter value update to be sent to the display
     */
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

    public void processConnectionInfo(PVConnectionInfo info) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("Processing %s", info));
        }

        // Call the real (but protected) method
        processConnection(info);
    }

    public void reportException(Exception e) { // Expose protected method
        reportExceptionToAllReadersAndWriters(e);
    }
}
