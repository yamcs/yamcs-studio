package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.studio.commanding.cmdhist.CommandHistoryRecord;

/**
 * Store command entries copied from the command history or the command stack. Copy command strings to system clipboard
 */
public class CommandClipboard {

    private static List<CommandHistoryRecord> copiedCommandHistoryRecords = new ArrayList<>();
    private static List<StackedCommand> copiedStackedCommands = new ArrayList<>();
    private static List<StackedCommand> cutStackedCommands = new ArrayList<>();

    public static void addCommandHistoryRecords(List<CommandHistoryRecord> chrs) {
        copiedStackedCommands.clear();
        cutStackedCommands.clear();
        copiedCommandHistoryRecords.clear();
        copiedCommandHistoryRecords.addAll(chrs);
    }

    public static void addStackedCommands(List<StackedCommand> scs, boolean cut, Display display) {
        copiedStackedCommands.clear();
        cutStackedCommands.clear();
        copiedCommandHistoryRecords.clear();
        copiedStackedCommands.addAll(scs);
        if (cut) {
            cutStackedCommands.addAll(scs);
        }

        String source = "";
        for (StackedCommand sc : scs) {
            source += sc.getSource() + "\n";
        }
        textToClipboard(source, display);
    }

    public static List<StackedCommand> getCopiedCommands() throws Exception {
        List<StackedCommand> result = new ArrayList<>();

        // convert CommandHistoryRecord to new Stacked Command
        for (CommandHistoryRecord chr : copiedCommandHistoryRecords) {
            StackedCommand pastedCommand = StackedCommand.buildCommandFromSource(chr.getCommandString());
            pastedCommand.setComment(chr.getTextForColumn("Comment", false));
            result.add(pastedCommand);
        }

        // copy stacked commands
        for (StackedCommand sc : copiedStackedCommands) {
            StackedCommand copy = new StackedCommand();
            copy.setMetaCommand(sc.getMetaCommand());
            if (sc.getComment() != null) {
                copy.setComment(sc.getComment());
            }
            if (sc.getSelectedAlias() != null) {
                copy.setSelectedAliase(sc.getSelectedAlias());
            }
            for (Entry<ArgumentInfo, String> entry : sc.getAssignments().entrySet()) {
                copy.addAssignment(entry.getKey(), entry.getValue());
            }

            result.add(copy);
        }

        return result;
    }

    public static List<StackedCommand> getCutCommands() {
        List<StackedCommand> result = new ArrayList<>(cutStackedCommands);
        return result;
    }

    public static boolean hasData() {
        return !(copiedCommandHistoryRecords.isEmpty() && copiedStackedCommands.isEmpty());
    }

    static private void textToClipboard(String text, Display display) {
        final Clipboard cb = new Clipboard(display);
        TextTransfer textTransfer = TextTransfer.getInstance();
        cb.setContents(new Object[] { text }, new Transfer[] { textTransfer });
    }

}
