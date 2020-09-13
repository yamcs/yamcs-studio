package org.yamcs.studio.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.yamcs.studio.data.sim.NameParser;
import org.yamcs.studio.data.sim.SimFunction;
import org.yamcs.studio.data.sim.Simulation;
import org.yamcs.studio.data.vtype.VType;

public class SimDatasource implements Datasource {

    private static final String SCHEME = "sim://";
    private static ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    private Map<String, SimData> name2data = new HashMap<>();
    private Map<IPV, SimData> pv2data = new HashMap<>();

    @Override
    public boolean supportsPVName(String pvName) {
        return pvName.startsWith(SCHEME);
    }

    @Override
    public boolean isConnected(IPV pv) {
        SimData simData = pv2data.get(pv);
        return simData != null && simData.isConnected();
    }

    @Override
    public boolean isWriteAllowed(IPV pv) {
        return false;
    }

    @Override
    public VType getValue(IPV pv) {
        SimData simData = pv2data.get(pv);
        if (simData != null) {
            return simData.getValue();
        }
        return null;
    }

    @Override
    public void writeValue(IPV pv, Object value, WriteCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onStarted(IPV pv) {
        String basename = pv.getName().substring(SCHEME.length());

        SimData simData = name2data.computeIfAbsent(basename, x -> {
            SimFunction<?> function = (SimFunction<?>) NameParser.createFunction(basename);
            return new SimulationSimData(function, exec);
        });

        pv2data.put(pv, simData);
        simData.register(pv);
    }

    @Override
    public void onStopped(IPV pv) {
        SimData simData = pv2data.remove(pv);
        if (simData != null) {
            simData.unregister(pv);
        }
    }

    private static final class SimulationSimData extends SimData {
        SimulationSimData(Simulation<?> simulation, ScheduledExecutorService exec) {
            super(simulation, exec);
        }
    }
}
