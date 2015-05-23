package org.yamcs.studio.core.pvmanager;

import org.epics.pvmanager.ChannelHandler;
import org.epics.pvmanager.DataSource;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class ParameterDataSource extends DataSource {

    public ParameterDataSource() {
        super(false /* read-only */);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new ParameterChannelHandler(channelName);
    }

}
