package org.yamcs.studio.commanding.stack;

import java.io.File;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExportCommandStackHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ExportCommandStackHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Get current command stack
        Collection<StackedCommand> commands = org.yamcs.studio.commanding.stack.CommandStack.getInstance()
                .getCommands();
        if (commands == null || commands.isEmpty()) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export Command Stack",
                    "Current command stack is empty. No command to export.");
            return null;
        }

        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { "*.xml" });
        // dialog.setFilterPath("c:\\temp");
        String exportFile = dialog.open();

        if (exportFile == null) {
            // cancelled
            return null;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("commandStack");
        doc.appendChild(rootElement);

        for (StackedCommand command : commands) {
            Element commandElement = doc.createElement("command");

            Attr attr = doc.createAttribute("qualifiedName");
            attr.setValue(command.getMetaCommand().getQualifiedName());
            commandElement.setAttributeNode(attr);

            attr = doc.createAttribute("selectedAlias");
            attr.setValue(command.getSelectedAlias());
            commandElement.setAttributeNode(attr);

            if (command.getComment() != null) {
                attr = doc.createAttribute("comment");
                attr.setValue(command.getComment());
                commandElement.setAttributeNode(attr);
            }

            if (command.getDelayMs() > 0) {
                attr = doc.createAttribute("delayMs");
                attr.setValue(Integer.toString(command.getDelayMs()));
                commandElement.setAttributeNode(attr);
            }

            for (TelecommandArgument arg : command.getEffectiveAssignments()) {
                String value = command.getAssignedStringValue(arg.getArgumentInfo());

                if (value == null && arg.getArgumentInfo().hasInitialValue()) {
                    continue;
                }
                if (!arg.isEditable()) {
                    continue;
                }
                if (arg.getArgumentInfo().hasInitialValue() && !command.isDefaultChanged(arg.getArgumentInfo())) {
                    continue;
                }

                Element argumentElement = doc.createElement("commandArgument");

                attr = doc.createAttribute("argumentName");
                attr.setValue(arg.getName());
                argumentElement.setAttributeNode(attr);

                attr = doc.createAttribute("argumentValue");
                attr.setValue(value);
                argumentElement.setAttributeNode(attr);

                commandElement.appendChild(argumentElement);
            }

            rootElement.appendChild(commandElement);
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(exportFile));
            transformer.transform(source, result);
        } catch (TransformerException e) {
            log.log(Level.SEVERE, "Error while exporting stack", e);
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Export Command Stack",
                    "Unable to perform command stack export.\nDetails:" + e.getMessage());
            return null;
        }

        MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Export Command Stack",
                "Command stack exported successfully.");
        return null;
    }
}
