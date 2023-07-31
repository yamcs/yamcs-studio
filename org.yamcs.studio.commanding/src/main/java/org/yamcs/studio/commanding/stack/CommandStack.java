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

    public enum StackStatus {
        IDLE,
        EXECUTING;
    }

    public enum StackMode {
        MANUAL(0),
        AUTOMATIC(1);

        private final int index;

        StackMode(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    public enum AutoMode {
        AFAP(0),
        FIX_DELAY(1),
        STACK_DELAYS(2);

        private final int index;

        AutoMode(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    private static final CommandStack INSTANCE = new CommandStack();
    private List<StackedCommand> commands = new ArrayList<>();
    private StackStatus stackStatus = StackStatus.IDLE;
    private StackMode stackMode = StackMode.MANUAL;
    private AutoMode autoMode = AutoMode.AFAP;
    private int waitTime = 0;

    public StackStatus getStackStatus() {
        return stackStatus;
    }

    public void setStackStatus(StackStatus stackStatus) {
        this.stackStatus = stackStatus;
    }

    public StackMode getStackMode() {
        return stackMode;
    }

    public void setStackMode(StackMode stackMode) {
        this.stackMode = stackMode;
    }

    public AutoMode getAutoMode() {
        return autoMode;
    }

    public void setAutoMode(AutoMode autoMode) {
        this.autoMode = autoMode;
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

    public StackedCommand getActiveCommand() {
        for (var command : commands) {
            if (command.getStackedState() != StackedState.ISSUED) {
                return command;
            }
        }

        return null;
    }

    public boolean hasRemaining() {
        return getActiveCommand() != null;
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
