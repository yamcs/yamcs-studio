package org.yamcs.studio.data;

import static org.yamcs.studio.data.vtype.ValueFactory.alarmNone;
import static org.yamcs.studio.data.vtype.ValueFactory.timeNow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.studio.data.vtype.VDouble;
import org.yamcs.studio.data.vtype.VDoubleArray;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VString;
import org.yamcs.studio.data.vtype.VStringArray;
import org.yamcs.studio.data.vtype.VTable;
import org.yamcs.studio.data.vtype.VType;
import org.yamcs.studio.data.vtype.ValueFactory;

/**
 * Holds local data. Associated with one or more loc:// PVs
 */
public class LocalData {

    private static Logger log = Logger.getLogger(LocalData.class.getName());

    private Object initialArguments;
    private VType initialValue;
    private Class<?> type;

    // The pure name without initializers
    private String name;

    private Set<IPV> pvs = new HashSet<>();

    private VType value;

    // Value may be initialized to null, so following field distinguishes between
    // null and undefined
    private AtomicBoolean valueDefined = new AtomicBoolean(false);

    public LocalData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isConnected() {
        return !pvs.isEmpty();
    }

    @SuppressWarnings("unchecked")
    synchronized void setInitialValue(Object value) {
        if (initialArguments != null && !initialArguments.equals(value)) {
            var message = String.format("Different initialization for local channel %s: %s but was %s", name, value,
                    initialArguments);
            log.log(Level.WARNING, message);
            throw new RuntimeException(message);
        }
        initialArguments = value;
        if (!valueDefined.getAndSet(true)) {
            if (VEnum.class.equals(type)) {
                List<?> args = (List<?>) initialArguments;
                // TODO error message if not Number
                var index = ((Number) args.get(0)).intValue();
                var labels = (List<String>) args.get(1);
                initialValue = ValueFactory.newVEnum(index, labels, alarmNone(), timeNow());
            } else {
                initialValue = checkValue(ValueFactory.toVTypeChecked(value));
            }
            processValue(initialValue);
        }
    }

    public VType getValue() {
        return value;
    }

    public void writeValue(Object newValue, WriteCallback callback) {
        try {
            if (VEnum.class.equals(type)) {
                // Handle enum writes
                var newIndex = -1;
                var firstEnum = (VEnum) initialValue;
                var labels = firstEnum.getLabels();
                if (newValue instanceof Number) {
                    newIndex = ((Number) newValue).intValue();
                } else if (newValue instanceof String) {
                    newIndex = labels.indexOf((String) newValue);
                    // Only if the String is not in the labels, try and
                    // parse a number.
                    if (newIndex == -1) {
                        var value = (String) newValue;
                        try {
                            newIndex = Double.valueOf(value).intValue();
                        } catch (NumberFormatException ex) {
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Value" + newValue + " can not be accepted by VEnum.");
                }
                newValue = ValueFactory.newVEnum(newIndex, firstEnum.getLabels(), alarmNone(), timeNow());
            } else if (VString.class.equals(type) && newValue instanceof String) {
                newValue = ValueFactory.newVString((String) newValue, alarmNone(), timeNow());
            } else {
                // If the string can be parsed to a number, do it
                if (newValue instanceof String) {
                    var value = (String) newValue;
                    try {
                        newValue = Double.valueOf(value);
                    } catch (NumberFormatException ex) {
                        // Not a double - continue
                    }
                }
                // If new value is not a VType, try to convert it
                if (!(newValue instanceof VType)) {
                    newValue = checkValue(ValueFactory.toVTypeChecked(newValue));
                }
            }

            processValue((VType) newValue);
            callback.dataWritten(null);
        } catch (Exception e) {
            callback.dataWritten(e);
        }
    }

    private void processValue(VType value) {
        this.value = value;
        pvs.forEach(pv -> pv.notifyValueChange());
    }

    private VType checkValue(VType value) {
        if (type != null && !type.isInstance(value)) {
            throw new IllegalArgumentException("Value " + value + " is not of type " + type.getSimpleName());
        }
        return value;
    }

    synchronized void setType(String typeName) {
        if (typeName == null) {
            return;
        }
        Class<?> newType = null;
        if ("VDouble".equals(typeName)) {
            newType = VDouble.class;
        }
        if ("VString".equals(typeName)) {
            newType = VString.class;
        }
        if ("VDoubleArray".equals(typeName)) {
            newType = VDoubleArray.class;
        }
        if ("VStringArray".equals(typeName)) {
            newType = VStringArray.class;
        }
        if ("VTable".equals(typeName)) {
            newType = VTable.class;
        }
        if ("VEnum".equals(typeName)) {
            newType = VEnum.class;
        }
        if (newType == null) {
            throw new IllegalArgumentException(
                    String.format("Type %s for channel %s is not supported by local datasource.", typeName, name));
        }
        if (type != null && !type.equals(newType)) {
            throw new IllegalArgumentException(
                    String.format("Type mismatch for channel %s: %s but was %s", name, typeName, type.getSimpleName()));
        }
        type = newType;
    }

    void register(IPV pv) {
        pvs.add(pv);
        pv.notifyConnectionChange();
        if (value != null) {
            pv.notifyValueChange();
        }
        pv.notifyWritePermissionChange();
    }

    void unregister(IPV pv) {
        pvs.remove(pv);
        if (pvs.isEmpty()) {
            initialArguments = null;
            type = null;
            value = null;
            valueDefined.set(false);
            pv.notifyConnectionChange();
            pv.notifyValueChange();
        }
    }
}
