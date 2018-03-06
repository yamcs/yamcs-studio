/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.sim;

import static org.diirt.util.Executors.namedPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;
import org.diirt.datasource.vtype.DataTypeSupport;

/**
 * Data source to produce simulated signals that can be using during development and testing.
 *
 * @author carcassi
 */
public final class SimulationDataSource extends DataSource {

    static {
        // Install type support for the types it generates.
        DataTypeSupport.install();
    }

    public SimulationDataSource() {
        super(false);
    }

    /**
     * Data source instance.
     *
     * @return the data source instance
     */
    public static DataSource simulatedData() {
        return SimulationDataSource.instance;
    }

    static final SimulationDataSource instance = new SimulationDataSource();

    /**
     * ExecutorService on which all simulated data is generated.
     */
    private final ScheduledExecutorService exec = Executors
            .newSingleThreadScheduledExecutor(namedPool("PVMgr Simulator "));

    @Override
    @SuppressWarnings("unchecked")
    protected ChannelHandler createChannel(String channelName) {
        if (channelName.startsWith("const(")) {
            return new ConstantChannelHandler(channelName);
        }
        if (channelName.startsWith("delayedConnectionChannel(")) {
            return new DelayedConnectionChannelHandler(channelName, exec);
        }
        if (channelName.startsWith("intermittentChannel(")) {
            return new IntermittentChannelHandler(channelName, exec);
        }

        SimFunction<?> simFunction = (SimFunction<?>) NameParser.createFunction(channelName);
        return new SimulationChannelHandler(channelName, simFunction, exec);
    }

    @Override
    public void close() {
        exec.shutdownNow();
        super.close();
    }

}
