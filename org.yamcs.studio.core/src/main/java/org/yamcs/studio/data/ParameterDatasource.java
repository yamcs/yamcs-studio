package org.yamcs.studio.data;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.client.processor.ProcessorClient;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.protobuf.Yamcs.Value.Type;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.vtype.VType;
import org.yamcs.studio.data.yamcs.YamcsSubscriptionService;

/**
 * A datasource that provides parameter data coming from Yamcs
 */
public class ParameterDatasource implements Datasource {

    private static final Logger log = Logger.getLogger(ParameterDatasource.class.getName());
    private static final List<String> TRUTHY = Arrays.asList("y", "true", "yes", "1", "1.0");

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
        return isConnected(pv);
    }

    @Override
    public void writeValue(IPV pv, Object value, WriteCallback callback) {
        try {
            NamedObjectId id = YamcsSubscriptionService.identityOf(pv.getName());

            ParameterTypeInfo ptype = YamcsPlugin.getMissionDatabase().getParameterTypeInfo(id);
            Value v = toValue(ptype, value);
            ProcessorClient processor = YamcsPlugin.getProcessorClient();
            String parameterName = id.getName();
            if (id.hasNamespace()) {
                parameterName = id.getNamespace() + "/" + parameterName;
            }
            processor.setValue(parameterName, v).whenComplete((data, e) -> {
                if (e != null) {
                    log.log(Level.SEVERE, "Could not write to parameter", e);
                    if (e instanceof Exception) {
                        callback.dataWritten((Exception) e);
                    } else {
                        callback.dataWritten(new ExecutionException(e));
                    }
                } else {
                    // Report success
                    callback.dataWritten(null);
                }
            });
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to write parameter value: " + value, e);
            return;
        }
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
}
