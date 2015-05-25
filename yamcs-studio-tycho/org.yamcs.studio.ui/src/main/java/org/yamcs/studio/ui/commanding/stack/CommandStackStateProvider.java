package org.yamcs.studio.ui.commanding.stack;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.studio.ui.commanding.stack.CommandStack.Mode;
import org.yamcs.studio.ui.commanding.stack.StackedCommand.State;

/**
 * Used in plugin.xml core-expressions to keep track of stack state
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CommandStackStateProvider extends AbstractSourceProvider {

    private static final Logger log = Logger.getLogger(CommandStackStateProvider.class.getName());

    public static final String STATE_KEY_MODE = "org.yamcs.studio.ui.commanding.stack.state.mode";
    public static final String STATE_KEY_REMAINING = "org.yamcs.studio.ui.commanding.stack.state.remaining";
    public static final String STATE_KEY_ARMED = "org.yamcs.studio.ui.commanding.stack.state.armed";
    private static final String[] SOURCE_NAMES = { STATE_KEY_MODE, STATE_KEY_REMAINING, STATE_KEY_ARMED };

    /**
     * The current mode of this stack
     */
    private Mode mode = Mode.EDIT;

    /**
     * Whether there's any remaining commands to be armed/executed
     */
    private boolean remaining = false;

    /**
     * Whether there's currently a command armed and ready to fire
     */
    private boolean armed = false;

    public void refreshState(CommandStack stack) {
        mode = stack.getMode();
        remaining = stack.hasRemaining();
        if (remaining) {
            armed = stack.getActiveCommand().getState() == State.ARMED;
        } else {
            armed = false;
        }

        Map newState = getCurrentState();
        log.info(String.format("Fire new stack state %s", newState));
        fireSourceChanged(ISources.WORKBENCH, newState);
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(2);
        map.put(STATE_KEY_MODE, mode.toString());
        map.put(STATE_KEY_REMAINING, remaining);
        map.put(STATE_KEY_ARMED, armed);
        return map;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return SOURCE_NAMES;
    }

    @Override
    public void dispose() {
    }
}
