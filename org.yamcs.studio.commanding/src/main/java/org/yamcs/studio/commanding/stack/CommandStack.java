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

import java.util.ArrayList;
import java.util.List;

import org.yamcs.studio.commanding.stack.StackedCommand.StackedState;

/**
 * Client-side structure for keeping track of an ordered set of commands with various options and checks.
 * <p>
 * Currently a stack is considered to be something at a workbench level, but this would ideally be refactored later on.
 */
public class CommandStack {

    private static final CommandStack INSTANCE = new CommandStack();
    private List<StackedCommand> commands = new ArrayList<>();
    private boolean executing = false;
    private int waitTime = 0;

    public boolean isExecuting() {
        return executing;
    }

    public void setExecuting(boolean executing) {
        this.executing = executing;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public static CommandStack getInstance() {
        return INSTANCE;
    }

    public void addCommand(StackedCommand command) {
        commands.add(command);
    }

    public void insertCommand(StackedCommand command, int index) {
        commands.add(index, command);
    }

    public List<StackedCommand> getCommands() {
        return commands;
    }

    public boolean isValid() {
        for (var cmd : commands) {
            if (!cmd.isValid()) {
                return false;
            }
        }

        return true;
    }

    public List<String> getErrorMessages() {
        var msgs = new ArrayList<String>();
        for (var cmd : commands) {
            msgs.addAll(cmd.getMessages());
        }

        return msgs;
    }

    public void disarmArmed() {
        for (var command : commands) {
            if (command.isArmed()) {
                command.setStackedState(StackedState.DISARMED);
            }
        }
    }

    public void resetExecutionState() {
        commands.forEach(StackedCommand::resetExecutionState);
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }
}
