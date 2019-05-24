package org.yamcs.studio.commanding.stack;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;

/**
 * Used in plugin.xml core-expressions to keep track of stack state
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CommandStackStateProvider extends AbstractSourceProvider {

    private static final Logger log = Logger.getLogger(CommandStackStateProvider.class.getName());

    public static final String STATE_KEY_REMAINING = "org.yamcs.studio.commanding.stack.state.remaining";
    public static final String STATE_KEY_EXECUTION_STARTED = "org.yamcs.studio.commanding.stack.state.executionStarted";
    public static final String STATE_KEY_EMPTY = "org.yamcs.studio.commanding.stack.state.empty";
    public static final String STATE_KEY_ARMED = "org.yamcs.studio.commanding.stack.state.armed";
    private static final String[] SOURCE_NAMES = { STATE_KEY_REMAINING, STATE_KEY_EXECUTION_STARTED, STATE_KEY_EMPTY,
            STATE_KEY_ARMED };

    /**
     * Whether there's any remaining commands to be armed/executed
     */
    private boolean remaining = false;

    /**
     * Whether any stacked command has already left the unarmed state
     */
    private boolean executionStarted = false;

    /**
     * Whether the stack is empty
     */
    private boolean empty = false;

    /**
     * Whether there's currently a command armed and ready to fire
     */
    private boolean armed = false;

    public void refreshState(CommandStack stack) {
        remaining = stack.hasRemaining();
        if (remaining) {
            armed = stack.getActiveCommand().getStackedState() == StackedState.ARMED;
        } else {
            armed = false;
        }

        empty = stack.isEmpty();

        executionStarted = false;
        for (StackedCommand cmd : stack.getCommands()) {
            if (cmd.getStackedState() != StackedState.DISARMED) {
                executionStarted = true;
                break;
            }
        }

        Map newState = getCurrentState();        
        fireSourceChanged(ISources.WORKBENCH, newState);
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(2);
        map.put(STATE_KEY_REMAINING, remaining);
        map.put(STATE_KEY_EXECUTION_STARTED, executionStarted);
        map.put(STATE_KEY_EMPTY, empty);
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
