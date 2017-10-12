package org.yamcs.studio.core.pvmanager;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;
import org.diirt.datasource.vtype.DataTypeSupport;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class SoftwareParameterDataSource extends DataSource {

    static {
        // Without below statement PVFactories don't work
        DataTypeSupport.install();
    }

    public SoftwareParameterDataSource() {
        super(true /* writable */);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new SoftwareParameterChannelHandler(channelName);
    }
}
