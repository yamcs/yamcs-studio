package org.yamcs.studio.ui.commanding.stack;

import java.io.FileReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.studio.ui.commanding.stack.xml.CommandStack;
import org.yamcs.studio.ui.commanding.stack.xml.CommandStack.Command.CommandArgument;
import org.yamcs.xtce.Argument;
import org.yamcs.xtce.MetaCommand;

public class ImportCommandStackHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ImportCommandStackHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        String importFile = dialog.open();
        if (importFile == null)
        {
            // cancelled
            return null;
        }
        log.log(Level.INFO, "Importing command stack from file: " + importFile);

        // get command stack object
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(CommandStackView.ID);
        CommandStackView commandStackView = (CommandStackView) part;

        // clear current stack
        //        org.yamcs.studio.ui.commanding.stack.CommandStack.getInstance().getCommands().clear();
        //        commandStackView.refreshState();
        // Actually, why clear current stack ? could be more flexible to keep current commands. User could delete them before if he wishes so.

        // import new commands
        for (StackedCommand sc : parseCommandStack(importFile))
        {
            commandStackView.addTelecommand(sc);
        }

        return null;
    }

    public List<StackedCommand> parseCommandStack(String fileName)
    {
        try {
            final JAXBContext jc = JAXBContext.newInstance(org.yamcs.studio.ui.commanding.stack.xml.CommandStack.class);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            final org.yamcs.studio.ui.commanding.stack.xml.CommandStack commandStack =
                    (org.yamcs.studio.ui.commanding.stack.xml.CommandStack) unmarshaller
                            .unmarshal(new FileReader(fileName));

            List<StackedCommand> importedStack = new LinkedList<StackedCommand>();
            for (CommandStack.Command c : commandStack.getCommand())
            {
                StackedCommand sc = new StackedCommand();

                MetaCommand mc = getMetaCommandFromYamcs(c.getCommandName());
                if (mc == null)
                {
                    MessageDialog.openError(Display.getCurrent().getActiveShell(), "Import Command Stack",
                            "Command " + c.getCommandName() + " does not exist in MDB.");
                    return null;
                }
                sc.setMetaCommand(mc);

                for (CommandArgument ca : c.getCommandArgument())
                {
                    Argument a = getArgumentFromYamcs(mc, ca.getArgumentName());
                    if (a == null)
                    {
                        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Import Command Stack",
                                "In command " + c.getCommandName() + ", argument " + ca.getArgumentName() + " does not exist in MDB.");
                        return null;
                    }
                    sc.addAssignment(a, ca.getArgumentValue());
                }
                importedStack.add(sc);
            }

            return importedStack;

        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to load command stack for importation. Check the XML file is correct. Details:\n"
                    + e.toString());
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Import Command Stack",
                    "Unable to load command stack for importation. Check the XML file is correct. Details:\n"
                            + e.toString());

            return null;
        }
    }

    private Argument getArgumentFromYamcs(MetaCommand mc, String argumentName) {
        for (Argument a : mc.getArgumentList())
        {
            if (a.getName().equals(argumentName))
                return a;
        }
        return null;
    }

    private MetaCommand getMetaCommandFromYamcs(String commandName) {
        Collection<MetaCommand> mcs = CommandingCatalogue.getInstance().getMetaCommands();
        for (MetaCommand mc : mcs)
        {
            if (mc.getQualifiedName().equals(commandName))
                return mc;
        }
        return null;
    }

}
