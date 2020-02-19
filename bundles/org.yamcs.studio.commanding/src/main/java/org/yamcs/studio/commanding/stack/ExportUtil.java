package org.yamcs.studio.commanding.stack;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExportUtil {

    public static String toXML(CommandStack stack) throws IOException, TransformerException {
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

        for (StackedCommand command : stack.getCommands()) {
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

        try (Writer writer = new StringWriter()) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        }
    }
}
