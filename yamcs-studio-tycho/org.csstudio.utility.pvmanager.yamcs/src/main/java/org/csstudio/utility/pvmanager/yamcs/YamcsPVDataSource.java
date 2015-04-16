package org.csstudio.utility.pvmanager.yamcs;

import org.csstudio.platform.libs.yamcs.YamcsWebSocketRegistrar;
import org.epics.pvmanager.ChannelHandler;
import org.epics.pvmanager.DataSource;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class YamcsPVDataSource extends DataSource {

    private static YamcsWebSocketRegistrar registrar;

    public YamcsPVDataSource() {
        super(false);
        registrar = YamcsWebSocketRegistrar.getInstance();
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new YamcsPVChannelHandler(channelName, registrar);
    }
}
