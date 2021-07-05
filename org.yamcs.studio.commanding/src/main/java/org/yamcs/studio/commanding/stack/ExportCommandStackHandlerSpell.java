package org.yamcs.studio.commanding.stack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class ExportCommandStackHandlerSpell extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Collection<StackedCommand> commands = CommandStack.getInstance().getCommands();
        if (commands == null || commands.isEmpty()) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export as SPELL Procedure",
                    "Current command stack is empty. No command to export.");
            return null;
        }

        // get export file name
        Shell shell = Display.getCurrent().getActiveShell();
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.py" });
        String exportFile = dialog.open();
        if (exportFile == null) {
            return null; // cancelled
        }

        Path p = Paths.get(exportFile);
        String filename = p.getFileName().toString();
        String filenameWithoutExtension = filename;
        if (filename.endsWith(".py")) {
            filenameWithoutExtension.substring(0, filename.length() - 3);
        }

        ExportCommandStackSpellDialog spellOptionsDialog = new ExportCommandStackSpellDialog(
                shell, commands, filenameWithoutExtension);
        int result = spellOptionsDialog.open();
        if (result != Window.OK) {
            return null; // cancelled
        }

        // Write model to a SPELL procedure, using a velocity template
        try {
            File file = new File(exportFile);

            // Initializes the velocity engine
            VelocityEngine engine = new VelocityEngine();
            engine.init();

            // Get the Template
            // Reading the file contents from the JAR
            StringBuilder buf = new StringBuilder();
            try (InputStream inStream = ExportCommandStackHandlerSpell.class
                    .getResourceAsStream("/resources/spell-procedure.vm");
                    InputStreamReader streamReader = new InputStreamReader(inStream);
                    BufferedReader bufferedReader = new BufferedReader(streamReader)) {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    buf.append(line);
                }
            }

            String template = buf.toString();

            // create a context and add data
            DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            Date date = new Date();
            String dateString = dateFormat.format(date);

            VelocityContext context = new VelocityContext();
            context.put("name", "World");
            context.put("stackedCommands", commands);
            context.put("exportDelays", spellOptionsDialog.exportDelays);
            context.put("filename", filename);
            context.put("procedureName", spellOptionsDialog.procedureName);
            context.put("spacecraft", spellOptionsDialog.spaceCraftName);
            context.put("author", System.getProperty("user.name"));
            context.put("date", dateString);
            context.put("nl", "\n");
            context.put("h", "#");

            // now render the template into a FileWriter
            try (FileWriter writer = new FileWriter(file)) {
                engine.evaluate(context, writer, "spell-procedure-generation", template);
            }

        } catch (Exception e) {
            MessageDialog.openError(shell, "Export Command Stack",
                    "Unable to perform command stack export to a SPELL procedure.\nDetails:" + e.getMessage());
            return null;
        }

        MessageDialog.openInformation(shell, "Export Command Stack to SPELL Procedure",
                "Command stack exported successfully to SPELL Procedure.");
        return null;
    }
}
