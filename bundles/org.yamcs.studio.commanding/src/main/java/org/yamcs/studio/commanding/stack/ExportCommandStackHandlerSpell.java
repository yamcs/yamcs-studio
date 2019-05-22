package org.yamcs.studio.commanding.stack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.studio.commanding.stack.xml.CommandStack.Command.CommandArgument;
import org.yamcs.studio.core.client.YamcsStudioClient;
import org.yamcs.studio.core.model.Catalogue;
import org.yamcs.studio.core.security.YamcsAuthorizations;
import org.yamcs.xtce.ArgumentAssignment;

public class ExportCommandStackHandlerSpell extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Get current command stack
        Collection<StackedCommand> scs = org.yamcs.studio.commanding.stack.CommandStack.getInstance().getCommands();
        if (scs == null || scs.isEmpty()) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export Command Stack",
                    "Current command stack is empty. No command to export.");
            return null;
        }

        // get export file name
        Shell shell = Display.getCurrent().getActiveShell();
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.py" });
        String exportFile = dialog.open();
        System.out.println("SPELL procedure file name choosen: " + exportFile);
        if (exportFile == null) {
            // cancelled
            return null;
        }
        Path p = Paths.get(exportFile);
        String filename = p.getFileName().toString();
        String filenameNoExt = filename;
        try {
            filenameNoExt = filename.substring(0, filename.lastIndexOf('.'));
        } catch (Exception e) {
        }

        // get options
        ExportCommandStackSpellDialog spellOptionsDialog = new ExportCommandStackSpellDialog(
                shell, scs, filenameNoExt);
        int result = spellOptionsDialog.open();
        if (result != Window.OK) {
            // cancelled
            return null;
        }
        System.out.println("SPELL procedure export options: " + spellOptionsDialog.exportDelays + ", "
                + spellOptionsDialog.spaceCraftName + "," + spellOptionsDialog.procedureName);

        // Build model
        org.yamcs.studio.commanding.stack.xml.CommandStack exportCommandStack = new org.yamcs.studio.commanding.stack.xml.CommandStack();
        List<org.yamcs.studio.commanding.stack.xml.CommandStack.Command> exportedCommands = exportCommandStack
                .getCommand();

        // Write model to a SPELL procedure, using a velocity template
        try {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));

            File file = new File(exportFile);

            // Initializes the velocity engine
            VelocityEngine ve = new VelocityEngine();
            ve.init();

            // Get the Template
            // Reading the file contents from the JAR
            InputStream inStream = ExportCommandStackHandlerSpell.class
                    .getResourceAsStream("/resources/spell-procedure.vm");
            StringBuilder stringBuilder = new StringBuilder();
            InputStreamReader streamReader = new InputStreamReader(inStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String template = stringBuilder.toString();

            // create a context and add data
            String author = YamcsAuthorizations.getInstance().getUsername();

            DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            Date date = new Date();
            String dateString = dateFormat.format(date);

            VelocityContext context = new VelocityContext();
            context.put("name", "World");
            context.put("stackedCommands", scs);
            context.put("exportDelays", spellOptionsDialog.exportDelays);
            context.put("filename", filename);
            context.put("procedureName", spellOptionsDialog.procedureName);
            context.put("spacecraft", spellOptionsDialog.spaceCraftName);
            context.put("author", author);
            context.put("date", dateString);
            context.put("nl", "\n");
            context.put("h", "#");

            // now render the template into a FileWriter
            FileWriter writer = new FileWriter(file);
            ve.evaluate(context, writer, "spell-procedure-generation", template);
            writer.close();

        } catch (Exception e) {
            MessageDialog.openError(shell, "Export Command Stack",
                    "Unable to perform command stack export to a SPELL procedure.\nDetails:" + e.getMessage());
            return null;
        }

        MessageDialog.openInformation(shell, "Export Command Stack to SPELL Procedure",
                "Command stack exported successfully to SPELL Procedure.");
        return null;
    }

    private List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
