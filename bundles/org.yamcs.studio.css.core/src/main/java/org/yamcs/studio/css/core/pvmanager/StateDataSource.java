package org.yamcs.studio.css.core.pvmanager;

import static org.diirt.util.Executors.namedPool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;
import org.diirt.datasource.vtype.DataTypeSupport;

public final class StateDataSource extends DataSource {

    static {
        // Install type support for the types it generates.
        DataTypeSupport.install();
    }

    public StateDataSource() {
        super(false);
    }

    /**
     * ExecutorService on which all data is polled.
     */
    private static ScheduledExecutorService exec = Executors
            .newSingleThreadScheduledExecutor(namedPool("pvmanager-state poller "));

    static ScheduledExecutorService getScheduledExecutorService() {
        return exec;
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        switch (channelName) {
        case "yamcs.host":
            return new YamcsHostChannelHandler(channelName);
        case "yamcs.instance":
            return new YamcsInstanceChannelHandler(channelName);
        case "yamcs.processor":
            return new YamcsProcessorChannelHandler(channelName);
        case "yamcs.serverId":
            return new YamcsServerIdChannelHandler(channelName);
        case "yamcs.username":
            return new YamcsUsernameChannelHandler(channelName);
        case "yamcs.version":
            return new YamcsVersionChannelHandler(channelName);
        default:
            throw new IllegalArgumentException("Channel " + channelName + " does not exist");
        }
    }
}
