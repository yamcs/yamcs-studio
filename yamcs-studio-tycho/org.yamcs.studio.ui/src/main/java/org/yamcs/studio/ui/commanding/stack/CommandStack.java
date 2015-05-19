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
    private List<Telecommand> commands = new ArrayList<>();

    private CommandStack() {
    }

    public static CommandStack getInstance() {
        return INSTANCE;
    }

    public void addCommand(Telecommand command) {
        commands.add(command);
    }

    public List<Telecommand> getCommands() {
        return commands;
    }

    public void checkForErrors() {
    }

    public int indexOf(Telecommand command) {
        return commands.indexOf(command);
    }

    public List<String> getErrorMessages() {
        List<String> msgs = new ArrayList<>();
        for (Telecommand cmd : commands)
            for (String msg : cmd.getMessages())
                msgs.add(msg);
        return msgs;
    }

    public Telecommand getNextCommand() {
        return commands.get(0);
    }
}
