package org.yamcs.studio.core.ui.connections;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Used in plugin.xml core-expressions to keep track of connection state
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConnectionStateProvider extends AbstractSourceProvider implements YamcsAware {

    private static final Logger log = Logger.getLogger(ConnectionStateProvider.class.getName());

    public static final String STATE_KEY_CONNECTING = "org.yamcs.studio.ui.state.connecting";
    public static final String STATE_KEY_CONNECTED = "org.yamcs.studio.ui.state.connected";

    private static final String[] SOURCE_NAMES = { STATE_KEY_CONNECTING, STATE_KEY_CONNECTED };

    private boolean connecting = false;
    private boolean connected = false;

    public ConnectionStateProvider() {
        YamcsPlugin.addListener(this);
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(2);
        map.put(STATE_KEY_CONNECTING, connecting);
        map.put(STATE_KEY_CONNECTED, connected);
        return map;
    }

    // Utility method for easier programmatic access.
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return SOURCE_NAMES;
    }

    @Override
    public void onYamcsConnecting() {
        Display.getDefault().asyncExec(() -> {
            connecting = true;
            connected = false;
            Map newState = getCurrentState();
            log.fine(String.format("Fire new connection state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public void onYamcsConnected() {
        Display.getDefault().asyncExec(() -> {
            connecting = false;
            connected = true;
            Map newState = getCurrentState();
            log.fine(String.format("Fire new connection state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public void onYamcsDisconnected() {
        Display.getDefault().asyncExec(() -> {
            connecting = false;
            connected = false;
            Map newState = getCurrentState();
            log.fine(String.format("Fire new connection state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public void onYamcsConnectionFailed(Throwable t) {
        Display.getDefault().asyncExec(() -> {
            connecting = false;
            connected = false;
            Map newState = getCurrentState();
            log.fine(String.format("Fire new connection state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public void dispose() {
        YamcsPlugin.removeListener(this);
    }
}
