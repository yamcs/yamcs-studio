package org.yamcs.studio.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.YamcsManagement.ClientInfo;
import org.yamcs.studio.core.StudioConnectionListener;
import org.yamcs.studio.core.WebSocketRegistrar;
import org.yamcs.studio.core.YamcsAuthorizations;
import org.yamcs.studio.core.YamcsAuthorizations.SystemPrivilege;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.studio.ui.connections.ConnectionStateProvider;

/**
 * Used in plugin.xml core-expressions to keep track of connection state
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AuthorizationStateProvider extends AbstractSourceProvider implements StudioConnectionListener {

    private static final Logger log = Logger.getLogger(ConnectionStateProvider.class.getName());

    public static final String STATE_KEY_MAY_COMMAND_PAYLOAD = "org.yamcs.studio.ui.authorization.mayCommandPayload";
    private static final String[] SOURCE_NAMES = { STATE_KEY_MAY_COMMAND_PAYLOAD };

    public AuthorizationStateProvider() {
        YamcsPlugin.getDefault().addStudioConnectionListener(this);
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(1);
        map.put(STATE_KEY_MAY_COMMAND_PAYLOAD, YamcsAuthorizations.getInstance().hasSystemPrivilege(SystemPrivilege.MayCommandPayload));
        return map;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return SOURCE_NAMES;
    }

    @Override
    public void onStudioConnect(ClientInfo clientInfo, YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        Display.getDefault().asyncExec(() -> {
            Map newState = getCurrentState();
            log.fine(String.format("Fire new connection state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public void onStudioDisconnect() {
        Display.getDefault().asyncExec(() -> {
            Map newState = getCurrentState();
            log.fine(String.format("Fire new connection state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public void dispose() {
        YamcsPlugin.getDefault().removeStudioConnectionListener(this);
    }
}
