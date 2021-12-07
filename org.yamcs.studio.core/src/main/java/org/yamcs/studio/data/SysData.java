package org.yamcs.studio.data;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.studio.data.vtype.Display;
import org.yamcs.studio.data.vtype.DisplayBuilder;
import org.yamcs.studio.data.vtype.NumberFormats;
import org.yamcs.studio.data.vtype.VType;

public abstract class SysData {

    private static Logger log = Logger.getLogger(SysData.class.getName());

    protected static Display memoryDisplay = new DisplayBuilder().format(NumberFormats.format(3)).units("MiB")
            .lowerAlarmLimit(0.0).lowerCtrlLimit(0.0).lowerDisplayLimit(0.0).lowerWarningLimit(0.0)
            .upperAlarmLimit(maxMemory()).upperCtrlLimit(maxMemory()).upperDisplayLimit(maxMemory())
            .upperWarningLimit(maxMemory()).build();

    protected static double bytesToMebiByte(long bytes) {
        return ((double) bytes) / (1024.0 * 1024.0);
    }

    private static double maxMemory() {
        return bytesToMebiByte(Runtime.getRuntime().maxMemory());
    }

    private final Runnable task = () -> {
        try {
            createAndSaveValue();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Sys problem", ex);
        }
    };

    private Set<IPV> pvs = new HashSet<>();
    private VType value;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> taskFuture;

    public SysData(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public boolean isConnected() {
        return !pvs.isEmpty();
    }

    public VType getValue() {
        return value;
    }

    private void createAndSaveValue() {
        var newValue = createValue();
        if (newValue != null) {
            this.value = newValue;
        }
        pvs.forEach(pv -> pv.notifyValueChange());
    }

    abstract VType createValue();

    void register(IPV pv) {
        pvs.add(pv);
        if (taskFuture == null) {
            taskFuture = executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
        }
        pv.notifyConnectionChange();
        pv.notifyValueChange();
    }

    void unregister(IPV pv) { // Note that we don't reset the value, it can stay around for a new connect
        pvs.remove(pv);
        if (pvs.isEmpty()) {
            taskFuture.cancel(true);
            taskFuture = null;
            pv.notifyConnectionChange();
        }
    }
}
