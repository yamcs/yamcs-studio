package org.yamcs.studio.core.commanding.cmdstack;

/**
 * Non-UI client-side structure to keep track of a non-issued command. For now, used only as part of
 * a command stack. Could also eventually be renamed to StackedCommand
 *
 * @see {@link CommandStack}
 */
public class Telecommand {

    private CommandStack commandStack;
    private String commandText;

    public Telecommand(CommandStack commandStack, String commandText) {
        this.commandStack = commandStack;
        this.commandText = commandText;
    }

    public int getRowId() {
        return commandStack.indexOf(this);
    }

    public CommandStack commandStack() {
        return commandStack;
    }

    public String getCommandText() {
        return commandText;
    }
}
