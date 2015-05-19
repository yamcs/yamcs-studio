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

    private static final CommandStack INSTANCE = new CommandStack();
    private List<StackedCommand> commands = new ArrayList<>();
    private StackedCommand nextCommand;

    private CommandStack() {
    }

    public static CommandStack getInstance() {
        return INSTANCE;
    }

    public void addCommand(StackedCommand command) {
        commands.add(command);
        if (nextCommand == null)
            nextCommand = command;
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

    public StackedCommand getNextCommand() {
        return nextCommand;
    }

    // This looks like something we could do better
    public StackedCommand incrementAndGet() {
        if (commands.isEmpty()) {
            nextCommand = null;
        } else if (nextCommand == null) {
            nextCommand = commands.get(0);
        } else {
            int idx = commands.indexOf(nextCommand);
            if (++idx < commands.size())
                nextCommand = commands.get(idx);
            else
                nextCommand = null;
        }
        return nextCommand;
    }

    public boolean hasRemaining() {
        return nextCommand != null && commands.size() > commands.indexOf(nextCommand);
    }
}
