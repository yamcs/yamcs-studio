package org.yamcs.studio.core.pvmanager;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class SoftwareParameterDataSource extends DataSource {

    public SoftwareParameterDataSource() {
        super(true /* writable */);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new SoftwareParameterChannelHandler(channelName);
    }
}
