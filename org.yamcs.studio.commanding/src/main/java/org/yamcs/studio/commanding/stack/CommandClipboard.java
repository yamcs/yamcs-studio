package org.yamcs.studio.commanding.stack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

    static class SortByGenerationTime implements Comparator<CommandHistoryRecord> {
        // Used for sorting in ascending order of roll number
        @Override
        public int compare(CommandHistoryRecord a, CommandHistoryRecord b) {
            return a.getCommand().getGenerationTime().compareTo(b.getCommand().getGenerationTime());
        }
    }

    public static List<StackedCommand> getCopiedCommands() throws Exception {
        List<StackedCommand> result = new ArrayList<>();

        // Convert CommandHistoryRecord to new Stacked Command
        // first compute the stack delays from the cmd history generation times
        List<CommandHistoryRecord> sortedRecords = new ArrayList<>();
        HashMap<CommandHistoryRecord, Integer> commandHistoryRecordDelays = new HashMap<>();
        for (CommandHistoryRecord chr : copiedCommandHistoryRecords) {
            sortedRecords.add(chr);
        }
        Collections.sort(sortedRecords, new SortByGenerationTime());
        long lastTime = 0;
        for (CommandHistoryRecord chr : sortedRecords) {
            long currentTime = chr.getCommand().getGenerationTime().toEpochMilli();
            if (lastTime == 0) {
                commandHistoryRecordDelays.put(chr, 0);
            } else {
                int currentDelay = (int) (currentTime - lastTime);
                commandHistoryRecordDelays.put(chr, currentDelay);
            }
            lastTime = currentTime;
        }
        // then add to the result
        for (CommandHistoryRecord chr : copiedCommandHistoryRecords) {
            StackedCommand pastedCommand = StackedCommand.buildCommandFromSource(chr.getCommand().getSource());
            pastedCommand.setComment(chr.getTextForColumn("Comment", false));
            pastedCommand.setDelayMs(commandHistoryRecordDelays.get(chr));
            result.add(pastedCommand);
        }

        // copy stacked commands
        for (StackedCommand sc : copiedStackedCommands) {
            StackedCommand copy = new StackedCommand();
            copy.setMetaCommand(sc.getMetaCommand());
            if (sc.getComment() != null) {
                copy.setComment(sc.getComment());
            }
            sc.getExtra().forEach((option, value) -> {
                copy.setExtra(option, value);
            });
            if (sc.getSelectedAlias() != null) {
                copy.setSelectedAliase(sc.getSelectedAlias());
            }
            for (Entry<ArgumentInfo, String> entry : sc.getAssignments().entrySet()) {
                copy.addAssignment(entry.getKey(), entry.getValue());
            }
            copy.setDelayMs(sc.getDelayMs());
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
