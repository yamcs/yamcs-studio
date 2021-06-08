package org.yamcs.studio.commanding.stack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class ExportCommandStackHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ExportCommandStackHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        CommandStack stack = CommandStack.getInstance();

        Collection<StackedCommand> commands = stack.getCommands();
        if (commands == null || commands.isEmpty()) {
            MessageDialog.openError(shell, "Export Command Stack",
                    "Current command stack is empty. No command to export.");
            return null;
        }

        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        String exportFile = dialog.open();
        if (exportFile == null) {
            // cancelled
            return null;
        }

        try {
            String xml = ExportUtil.toXML(stack);
            Files.write(Paths.get(exportFile), xml.getBytes());
            MessageDialog.openInformation(shell, "Export Command Stack",
                    "Command stack exported successfully.");
        } catch (IOException | TransformerException e) {
            log.log(Level.SEVERE, "Error while exporting stack", e);
            MessageDialog.openError(shell, "Export Command Stack",
                    "Unable to perform command stack export.\nDetails:" + e.getMessage());
        }

        return null;
    }
}
