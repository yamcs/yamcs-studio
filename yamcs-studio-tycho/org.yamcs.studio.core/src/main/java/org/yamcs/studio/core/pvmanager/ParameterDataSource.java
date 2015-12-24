package org.yamcs.studio.core.pvmanager;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;

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
