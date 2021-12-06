package org.yamcs.studio.commanding.stack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class ExportCommandStackHandlerSpell extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var commands = CommandStack.getInstance().getCommands();
        if (commands == null || commands.isEmpty()) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export as SPELL Procedure",
                    "Current command stack is empty. No command to export.");
            return null;
        }

        var shell = Display.getCurrent().getActiveShell();
        var dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.py" });
        var exportFile = dialog.open();
        if (exportFile == null) {
            return null; // cancelled
        }

        var buf = new StringBuilder();
        var first = true;
        for (var command : commands) {
            if (!first) {
                buf.append("\n");
            }
            first = false;
            if (command.getDelayMs() > 0) {
                var delay = command.getDelayMs() / 1000;
                buf.append("WaitFor(").append(delay).append(" * SECOND)\n");
            }
            buf.append("Send(command=\"")
                    .append(command.getMetaCommand().getQualifiedName())
                    .append("\"");

            // Reduce to only the non-default assignments
            var assignments = new LinkedHashMap<>(command.getAssignments());
            var it = assignments.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                if (entry.getKey().hasInitialValue() && !command.isDefaultChanged(entry.getKey())) {
                    it.remove();
                }
            }

            if (!assignments.entrySet().isEmpty()) {
                buf.append(", args=[\n");
                for (var entry : assignments.entrySet()) {
                    buf.append("    [\"")
                            .append(entry.getKey().getName())
                            .append("\", ")
                            .append(entry.getValue())
                            .append("]")
                            .append(",\n");
                }
                buf.append("]");
            }
            var comment = command.getComment();
            if (comment != null && !comment.isBlank()) {
                buf.append(", addInfo={\"comment\": \"\"\"").append(comment).append("\"\"\"}");
            }
            buf.append(")\n");
        }

        try (var writer = new FileWriter(new File(exportFile))) {
            writer.append(buf.toString());
        } catch (IOException e) {
            MessageDialog.openError(shell, "Export Command Stack",
                    "Unable to perform command stack export to a SPELL procedure.\nDetails:" + e.getMessage());
            return null;
        }

        MessageDialog.openInformation(shell, "Export Command Stack to SPELL Procedure", "Export done.");
        return null;
    }
}
