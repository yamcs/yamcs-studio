package org.csstudio.utility.pvmanager.yamcs;

import org.csstudio.platform.libs.yamcs.YRegistrar;
import org.epics.pvmanager.ChannelHandler;
import org.epics.pvmanager.DataSource;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class YamcsPVDataSource extends DataSource {

    private static YRegistrar registrar;

    public YamcsPVDataSource() {
        super(false);
        registrar = YRegistrar.getInstance();
    }

    @Override
    public void close() { // hmm not called on opiruntime close..
        registrar.shutdown();
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new YamcsPVChannelHandler(channelName, registrar);
    }
}
