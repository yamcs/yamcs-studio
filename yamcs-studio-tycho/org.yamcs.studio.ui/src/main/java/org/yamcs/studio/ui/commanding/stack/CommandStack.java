package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side structure for keeping track of an ordered set of commands with various options and
 * checks.
 * <p>
 * Currently a stack is considered to be something at a workbench level, but this would ideally be
 * refactored later on.
 */
public class CommandStack {

    public enum Mode {
        EDIT,
        EXECUTE
    }

    private static final CommandStack INSTANCE = new CommandStack();
    private List<StackedCommand> commands = new ArrayList<>();
    private StackedCommand activeCommand;
    private Mode mode = Mode.EDIT;

    private CommandStack() {
    }

    public static CommandStack getInstance() {
        return INSTANCE;
    }

    public Mode getMode() {
        return mode;
    }

    public void addCommand(StackedCommand command) {
        commands.add(command);
        if (activeCommand == null)
            activeCommand = command;
    }

    public List<StackedCommand> getCommands() {
        return commands;
    }

    public void checkForErrors() {
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
        return activeCommand;
    }

    // This looks like something we could do better
    public StackedCommand incrementAndGet() {
        if (commands.isEmpty()) {
            activeCommand = null;
        } else if (activeCommand == null) {
            activeCommand = commands.get(0);
        } else {
            int idx = commands.indexOf(activeCommand);
            if (++idx < commands.size())
                activeCommand = commands.get(idx);
            else
                activeCommand = null;
        }
        return activeCommand;
    }

    public boolean hasRemaining() {
        return activeCommand != null && commands.size() > commands.indexOf(activeCommand);
    }
}
