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

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
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

        var source = "";
        for (var sc : scs) {
            source += sc.getSource() + "\n";
        }
        textToClipboard(source, display);
    }

    public static List<StackedCommand> getCopiedCommands() throws Exception {
        var result = new ArrayList<StackedCommand>();

        for (var rec : copiedCommandHistoryRecords) {
            var copy = new StackedCommand(rec);
            result.add(copy);
        }
        for (var sc : copiedStackedCommands) {
            var copy = new StackedCommand(sc);
            result.add(copy);
        }

        return result;
    }

    public static List<StackedCommand> getCutCommands() {
        return new ArrayList<>(cutStackedCommands);
    }

    public static boolean hasData() {
        return !(copiedCommandHistoryRecords.isEmpty() && copiedStackedCommands.isEmpty());
    }

    private static void textToClipboard(String text, Display display) {
        var cb = new Clipboard(display);
        var textTransfer = TextTransfer.getInstance();
        cb.setContents(new Object[] { text }, new Transfer[] { textTransfer });
    }
}
