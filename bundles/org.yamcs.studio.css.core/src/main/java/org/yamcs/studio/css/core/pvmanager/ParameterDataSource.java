package org.yamcs.studio.css.core.pvmanager;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;
import org.diirt.datasource.vtype.DataTypeSupport;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class ParameterDataSource extends DataSource {

    static {
        // Without below statement PVFactories don't work
        DataTypeSupport.install();
    }

    public ParameterDataSource() {
        super(false /* read-only */);
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new ParameterChannelHandler(channelName);
    }
}
