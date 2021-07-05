package org.yamcs.studio.core.ui;

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
public class AuthorizationStateProvider extends AbstractSourceProvider implements YamcsAware {

    private static final Logger log = Logger.getLogger(AuthorizationStateProvider.class.getName());

    public static final String STATE_KEY_MAY_COMMAND = "org.yamcs.studio.ui.authorization.mayCommand";
    public static final String STATE_KEY_MAY_READ_STACKS = "org.yamcs.studio.ui.authorization.mayReadStacks";
    public static final String STATE_KEY_MAY_WRITE_STACKS = "org.yamcs.studio.ui.authorization.mayWriteStacks";
    private static final String[] SOURCE_NAMES = {
            STATE_KEY_MAY_COMMAND,
            STATE_KEY_MAY_READ_STACKS,
            STATE_KEY_MAY_WRITE_STACKS,
    };

    public AuthorizationStateProvider() {
        YamcsPlugin.addListener(this);
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(3);
        map.put(STATE_KEY_MAY_COMMAND, YamcsPlugin.hasAnyObjectPrivilege("Command"));
        map.put(STATE_KEY_MAY_READ_STACKS, YamcsPlugin.hasObjectPrivilege("ReadBucket", "stacks"));
        map.put(STATE_KEY_MAY_WRITE_STACKS, YamcsPlugin.hasObjectPrivilege("ManageBucket", "stacks")
                || YamcsPlugin.hasSystemPrivilege("ManageAnyBucket"));
        return map;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return SOURCE_NAMES;
    }

    @Override
    public void onYamcsConnected() {
        Display.getDefault().asyncExec(() -> {
            Map newState = getCurrentState();
            log.fine(String.format("Fire new authz state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public void onYamcsDisconnected() {
        Display.getDefault().asyncExec(() -> {
            Map newState = getCurrentState();
            log.fine(String.format("Fire new authz state %s", newState));
            fireSourceChanged(ISources.WORKBENCH, newState);
        });
    }

    @Override
    public void dispose() {
        YamcsPlugin.removeListener(this);
    }
}
