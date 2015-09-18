package org.yamcs.studio.ui.commanding.stack;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.yamcs.studio.ui.commanding.stack.xml.CommandStack.Command.CommandArgument;
import org.yamcs.xtce.Argument;

public class ExportCommandStackHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Get current command stack
        Collection<StackedCommand> scs = org.yamcs.studio.ui.commanding.stack.CommandStack.getInstance().getCommands();
        if (scs == null || scs.isEmpty())
        {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export Command Stack",
                    "Current command stack is empty. No command to export.");
            return null;
        }

        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        //  dialog.setFilterPath("c:\\temp");
        String exportFile = dialog.open();
        System.out.println("export file choosen: " + exportFile);

        if (exportFile == null)
        {
            // cancelled
            return null;
        }

        // Build model
        org.yamcs.studio.ui.commanding.stack.xml.CommandStack exportCommandStack = new org.yamcs.studio.ui.commanding.stack.xml.CommandStack();
        List<org.yamcs.studio.ui.commanding.stack.xml.CommandStack.Command> exportedCommands = exportCommandStack.getCommand();
        for (StackedCommand sc : scs)
        {
            org.yamcs.studio.ui.commanding.stack.xml.CommandStack.Command c = new org.yamcs.studio.ui.commanding.stack.xml.CommandStack.Command();
            c.setCommandName(sc.getMetaCommand().getQualifiedName());
            exportedCommands.add(c);
            List<CommandArgument> cas = c.getCommandArgument();

            Iterator<?> it = sc.getAssignments().entrySet().iterator();
            while (it.hasNext())
            {
                @SuppressWarnings("unchecked")
                Map.Entry<Argument, String> pair = (Entry<Argument, String>) it.next();
                String argName = pair.getKey().getName();
                String argValue = pair.getValue();

                CommandArgument ca = new CommandArgument();
                ca.setArgumentName(argName);
                ca.setArgumentValue(argValue);
                cas.add(ca);
            }
        }

        // Write model to file
        try {
            File file = new File(exportFile);
            JAXBContext jaxbContext = JAXBContext.newInstance(org.yamcs.studio.ui.commanding.stack.xml.CommandStack.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(exportCommandStack, file);
            jaxbMarshaller.marshal(exportCommandStack, System.out);
        } catch (Exception e)
        {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export Command Stack",
                    "Unable to perform command stack export.\nDetails:" + e.getMessage());
            return null;
        }

        MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Export Command Stack", "Command stack exported successfully.");
        return null;
    }
}
