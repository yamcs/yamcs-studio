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

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.yamcs.studio.data.yamcs.StringConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ExportUtil {

    public static JsonObject toJSON(CommandStack stack) {
        var root = new JsonObject();
        root.addProperty("$schema", "https://yamcs.org/schema/command-stack.schema.json");

        var commandsArray = new JsonArray();
        root.add("commands", commandsArray);
        for (var command : stack.getCommands()) {
            var commandObject = new JsonObject();
            commandObject.addProperty("name", command.getMetaCommand().getQualifiedName());
            if (command.getComment() != null) {
                commandObject.addProperty("comment", command.getComment());
            }

            var argumentsArray = new JsonArray();
            for (var arg : command.getEffectiveAssignments()) {
                var value = command.getAssignedStringValue(arg.getArgumentInfo());

                if (value == null && arg.getArgumentInfo().hasInitialValue()) {
                    continue;
                }
                if (!arg.isEditable()) {
                    continue;
                }
                if (arg.getArgumentInfo().hasInitialValue() && !command.isDefaultChanged(arg.getArgumentInfo())) {
                    continue;
                }

                var argumentObject = new JsonObject();
                argumentObject.addProperty("name", arg.getName());
                if ("true".equals(value)) {
                    argumentObject.addProperty("value", true);
                } else if ("false".equals(value)) {
                    argumentObject.addProperty("value", false);
                } else {
                    argumentObject.addProperty("value", value);
                }
                argumentsArray.add(argumentObject);
            }
            commandObject.add("arguments", argumentsArray);

            if (!command.getExtra().isEmpty()) {
                var extraOptions = new JsonArray();

                command.getExtra().forEach((option, value) -> {
                    var optionObject = new JsonObject();
                    extraOptions.add(optionObject);
                    optionObject.addProperty("id", option);
                    switch (value.getType()) {
                    case BOOLEAN:
                        optionObject.addProperty("value", value.getBooleanValue());
                        break;
                    case UINT32:
                        optionObject.addProperty("value", value.getUint32Value());
                        break;
                    case UINT64:
                        optionObject.addProperty("value", value.getUint64Value());
                        break;
                    case SINT32:
                        optionObject.addProperty("value", value.getSint32Value());
                        break;
                    case SINT64:
                        optionObject.addProperty("value", value.getSint64Value());
                        break;
                    case FLOAT:
                        optionObject.addProperty("value", value.getFloatValue());
                        break;
                    case DOUBLE:
                        optionObject.addProperty("value", value.getDoubleValue());
                        break;
                    default:
                        optionObject.addProperty("value", StringConverter.toString(value));
                    }
                });

                commandObject.add("extraOptions", extraOptions);
            }

            if (command.getDelayMs() > 0) {
                var advancementObject = new JsonObject();
                advancementObject.addProperty("wait", command.getDelayMs());
                commandObject.add("advancement", advancementObject);
            }

            commandsArray.add(commandObject);
        }

        return root;
    }

    public static String toXML(CommandStack stack) throws IOException, TransformerException {
        var factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
        var doc = builder.newDocument();
        var rootElement = doc.createElement("commandStack");
        doc.appendChild(rootElement);

        for (var command : stack.getCommands()) {
            var commandElement = doc.createElement("command");

            var attr = doc.createAttribute("qualifiedName");
            attr.setValue(command.getMetaCommand().getQualifiedName());
            commandElement.setAttributeNode(attr);

            if (command.getComment() != null) {
                attr = doc.createAttribute("comment");
                attr.setValue(command.getComment());
                commandElement.setAttributeNode(attr);
            }

            if (!command.getExtra().isEmpty()) {
                var extraEl = doc.createElement("extraOptions");
                commandElement.appendChild(extraEl);

                command.getExtra().forEach((option, value) -> {
                    var optionEl = doc.createElement("extraOption");
                    extraEl.appendChild(optionEl);
                    var idAttr = doc.createAttribute("id");
                    idAttr.setValue(option);
                    optionEl.setAttributeNode(idAttr);

                    var valueAttr = doc.createAttribute("value");
                    valueAttr.setValue(StringConverter.toString(value));
                    optionEl.setAttributeNode(valueAttr);
                });
            }

            if (command.getDelayMs() > 0) {
                attr = doc.createAttribute("delayMs");
                attr.setValue(Integer.toString(command.getDelayMs()));
                commandElement.setAttributeNode(attr);
            }

            for (var arg : command.getEffectiveAssignments()) {
                var value = command.getAssignedStringValue(arg.getArgumentInfo());

                if (value == null && arg.getArgumentInfo().hasInitialValue()) {
                    continue;
                }
                if (!arg.isEditable()) {
                    continue;
                }
                if (arg.getArgumentInfo().hasInitialValue() && !command.isDefaultChanged(arg.getArgumentInfo())) {
                    continue;
                }

                var argumentElement = doc.createElement("commandArgument");

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

        try (var writer = new StringWriter()) {
            var transformerFactory = TransformerFactory.newInstance();
            var transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            var source = new DOMSource(doc);
            var result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        }
    }
}
