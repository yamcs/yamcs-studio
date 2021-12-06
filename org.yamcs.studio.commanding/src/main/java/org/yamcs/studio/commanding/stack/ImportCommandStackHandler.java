package org.yamcs.studio.commanding.stack;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.studio.core.YamcsPlugin;

public class ImportCommandStackHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ImportCommandStackHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        String importFile = dialog.open();
        if (importFile == null) {
            // cancelled
            return null;
        }
        log.info("Importing command stack from file: " + importFile);

        // get command stack object
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPart part = window.getActivePage().findView(CommandStackView.ID);
        CommandStackView commandStackView = (CommandStackView) part;

        // import new commands
        for (StackedCommand sc : parseCommandStack(shell, Paths.get(importFile))) {
            commandStackView.addTelecommand(sc);
        }

        return null;
    }

    private List<StackedCommand> parseCommandStack(Shell shell, Path file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file.toFile());
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("command");

            List<StackedCommand> commands = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element commandElement = (Element) node;
                    String qname = commandElement.getAttribute("qualifiedName");

                    CommandInfo mdbInfo = YamcsPlugin.getMissionDatabase().getCommandInfo(qname);
                    if (mdbInfo == null) {
                        MessageDialog.openError(shell, "Import Command Stack",
                                "Command " + qname + " does not exist in MDB.");
                        return null;
                    }

                    StackedCommand command = new StackedCommand();
                    command.setMetaCommand(mdbInfo);
                    if (commandElement.hasAttribute("comment")) {
                        String comment = commandElement.getAttribute("comment");
                        command.setComment(comment);
                    }
                    if (commandElement.hasAttribute("delayMs")) {
                        int delay = Integer.parseInt(commandElement.getAttribute("delayMs"));
                        command.setDelayMs(delay);
                    }

                    NodeList argNodes = commandElement.getElementsByTagName("commandArgument");
                    for (int j = 0; j < argNodes.getLength(); j++) {
                        Node argNode = argNodes.item(j);
                        if (argNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element argElement = (Element) argNode;
                            String argName = argElement.getAttribute("argumentName");
                            String argValue = argElement.getAttribute("argumentValue");
                            ArgumentInfo argInfo = getArgumentFromYamcs(mdbInfo, argName);
                            if (argInfo == null) {
                                MessageDialog.openError(shell, "Import Command Stack",
                                        "In command " + qname + ", argument " + argName + " does not exist in MDB.");
                                return null;
                            }
                            command.addAssignment(argInfo, argValue);
                        }
                    }
                    commands.add(command);
                }
            }

            return commands;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to import command stack. Check the XML file is correct. Details: " + e);
            MessageDialog.openError(shell, "Import Command Stack",
                    "Unable to import command stack. Check the XML file is correct. Details:\n" + e);
            return null;
        }
    }

    private ArgumentInfo getArgumentFromYamcs(CommandInfo mc, String argumentName) {
        // look for argument in the command
        for (ArgumentInfo a : mc.getArgumentList()) {
            if (a.getName().equals(argumentName)) {
                return a;
            }
        }

        // else look in the parent command
        if (mc.getBaseCommand() != mc) {
            return getArgumentFromYamcs(mc.getBaseCommand(), argumentName);
        }

        // else, argument is not found...
        return null;
    }
}
