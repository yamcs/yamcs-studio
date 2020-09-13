package org.yamcs.studio.data;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.studio.data.sim.SimFunction;
import org.yamcs.studio.data.sim.Simulation;
import org.yamcs.studio.data.sim.TimeInterval;
import org.yamcs.studio.data.vtype.VType;

public class SimData {

    private static final Logger log = Logger.getLogger(SimData.class.getName());

    private Simulation<?> simulation;
    private Set<IPV> pvs = new HashSet<>();
    private VType value;

    private final Runnable task = () -> {
        try {
            if (simulation.lastTime == null) {
                simulation.lastTime = Instant.now();
            }
            List<VType> newValues = simulation
                    .createValues(TimeInterval.between(simulation.lastTime, Instant.now()));

            for (VType newValue : newValues) {
                if (newValue != null) {
                    value = newValue;
                }
                pvs.forEach(pv -> pv.notifyValueChange());
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Data simulation problem", ex);
        }
    };

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> taskFuture;

    public SimData(Simulation<?> simulation, ScheduledExecutorService executor) {
        this.simulation = simulation;
        this.executor = executor;
    }

    public boolean isConnected() {
        return !pvs.isEmpty();
    }

    public VType getValue() {
        return value;
    }

    void register(IPV pv) {
        pvs.add(pv);
        if (taskFuture == null) {
            simulation.lastTime = Instant.now();
            if (simulation instanceof SimFunction) {
                simulation.lastTime = simulation.lastTime.minus(((SimFunction<?>) simulation).getTimeBetweenSamples());
            }
            taskFuture = executor.scheduleWithFixedDelay(task, 0, 10, TimeUnit.MILLISECONDS);
        }
        pv.notifyConnectionChange();
        if (value != null) {
            pv.notifyValueChange();
        }
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
