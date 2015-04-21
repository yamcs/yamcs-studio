package org.csstudio.utility.pvmanager.yamcs;

import org.csstudio.platform.libs.yamcs.YamcsPlugin;
import org.csstudio.platform.libs.yamcs.WebSocketRegistrar;
import org.epics.pvmanager.ChannelHandler;
import org.epics.pvmanager.DataSource;

/**
 * When running the OPIbuilder this is instantiated for every parameter separately.
 */
public class ParameterDataSource extends DataSource {

    private static WebSocketRegistrar webSocketClient;

    public ParameterDataSource() {
        super(false /* read-only */);
        webSocketClient = YamcsPlugin.getDefault().getWebSocketClient();
    }

    @Override
    protected ChannelHandler createChannel(String channelName) {
        return new ParameterChannelHandler(channelName, webSocketClient);
    }
}
