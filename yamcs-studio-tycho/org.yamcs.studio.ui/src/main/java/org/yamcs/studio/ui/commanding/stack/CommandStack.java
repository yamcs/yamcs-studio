package org.yamcs.studio.ui.commanding.stack;

import java.util.ArrayList;
import java.util.List;

/**
 * Non-ui client-side structure for keeping track of an ordered set of commands with various options
 * and checks.
 * <p>
 * Currently a stack is considered to be something at a workbench level, but this would ideally be
 * refactored later on.
 */
public class CommandStack {

    private List<Telecommand> commands = new ArrayList<>();

    public void addCommand(Telecommand command) {
        commands.add(command);
    }

    /**
     * Returns zero-based index of a command within its stack, or -1 if it could not be found.
     */
    public int indexOf(Telecommand command) {
        return commands.indexOf(command);
    }
}
