package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.List;

import org.yamcs.studio.ui.commanding.stack.StackedCommand.StackedState;

/**
 * Client-side structure for keeping track of an ordered set of commands with various options and
 * checks.
 * <p>
 * Currently a stack is considered to be something at a workbench level, but this would ideally be
 * refactored later on.
 */
public class CommandStack {

    private static final CommandStack INSTANCE = new CommandStack();
    private List<StackedCommand> commands = new ArrayList<>();

    private CommandStack() {
    }

    public static CommandStack getInstance() {
        return INSTANCE;
    }

    public void addCommand(StackedCommand command) {
        commands.add(command);
    }

    public List<StackedCommand> getCommands() {
        return commands;
    }

    public boolean isValid() {
        for (StackedCommand cmd : commands)
            if (!cmd.isValid())
                return false;

        return true;
    }

    public int indexOf(StackedCommand command) {
        return commands.indexOf(command);
    }

    public List<String> getErrorMessages() {
        List<String> msgs = new ArrayList<>();
        for (StackedCommand cmd : commands)
            for (String msg : cmd.getMessages())
                msgs.add(msg);

        return msgs;
    }

    public StackedCommand getActiveCommand() {
        for (StackedCommand command : commands)
            if (command.getStackedState() != StackedState.ISSUED && command.getStackedState() != StackedState.SKIPPED)
                return command;

        return null;
    }

    public boolean hasRemaining() {
        return getActiveCommand() != null;
    }

    public void disarmArmed() {
        for (StackedCommand command : commands)
            if (command.isArmed())
                command.setStackedState(StackedState.DISARMED);
    }

    public void resetExecutionState() {
        commands.forEach(c -> c.resetExecutionState());
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }
}
