/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.stack;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;

/**
 * Used in plugin.xml core-expressions to keep track of stack state
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CommandStackStateProvider extends AbstractSourceProvider {

    public static final String STATE_KEY_EXECUTION_STARTED = "org.yamcs.studio.commanding.stack.state.executionStarted";
    public static final String STATE_KEY_EMPTY = "org.yamcs.studio.commanding.stack.state.empty";
    public static final String STATE_KEY_EXECUTING = "org.yamcs.studio.commanding.stack.state.executing";
    private static final String[] SOURCE_NAMES = {
            STATE_KEY_EXECUTION_STARTED,
            STATE_KEY_EMPTY,
            STATE_KEY_EXECUTING,
    };

    /**
     * Whether any stacked command has already left the unarmed state
     */
    private boolean executionStarted = false;

    /**
     * Whether the stack is empty
     */
    private boolean empty = false;

    /**
     * Whether the stack is currently busy executing
     */
    private boolean executing = false;

    public void refreshState(CommandStack stack) {
        empty = stack.isEmpty();

        executionStarted = false;
        for (var cmd : stack.getCommands()) {
            if (cmd.getStackedState() != StackedState.DISARMED) {
                executionStarted = true;
                break;
            }
        }

        executing = stack.isExecuting();

        var newState = getCurrentState();
        fireSourceChanged(ISources.WORKBENCH, newState);
    }

    @Override
    public Map getCurrentState() {
        Map map = new HashMap(3);
        map.put(STATE_KEY_EXECUTION_STARTED, executionStarted);
        map.put(STATE_KEY_EMPTY, empty);
        map.put(STATE_KEY_EXECUTING, executing);
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
