package org.csstudio.utility.pvmanager.yamcs;

import org.csstudio.platform.libs.yamcs.YamcsPlugin;
import org.csstudio.platform.libs.yamcs.WebSocketRegistrar;
import org.epics.pvmanager.ChannelHandler;
import org.epics.pvmanager.DataSource;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class YamcsDataSource extends DataSource {

    private static WebSocketRegistrar webSocketClient;

    public YamcsDataSource() {
        super(false /* read-only */);
        webSocketClient = YamcsPlugin.getDefault().getWebSocketClient();
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new YamcsChannelHandler(channelName, webSocketClient);
    }
}
