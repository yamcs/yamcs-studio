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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.yamcs.protobuf.Mdb.ArgumentInfo;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.studio.core.YamcsPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ImportCommandStackHandler extends AbstractHandler {

    private static final Logger log = Logger.getLogger(ImportCommandStackHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        var shell = HandlerUtil.getActiveShell(event);
        var dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterExtensions(new String[] { "*.xml;*.ycs" });
        var importFile = dialog.open();
        if (importFile == null) {
            return null;
        }
        log.info("Importing command stack from file: " + importFile);

        // get command stack object
        var window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        var part = window.getActivePage().findView(CommandStackView.ID);
        var commandStackView = (CommandStackView) part;

        if (importFile.toLowerCase().endsWith(".xml")) {
            for (var sc : parseXmlCommandStack(shell, Path.of(importFile))) {
                commandStackView.addTelecommand(sc);
            }
        } else if (importFile.toLowerCase().endsWith(".ycs")) {
            for (var sc : parseYcsCommandStack(shell, Path.of(importFile))) {
                commandStackView.addTelecommand(sc);
            }
        }

        return null;
    }

    private List<StackedCommand> parseYcsCommandStack(Shell shell, Path file) {
        var commands = new ArrayList<StackedCommand>();
        var gson = new Gson();
        JsonObject stackObject;
        try (var reader = Files.newBufferedReader(file)) {
            stackObject = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to import command stack:" + e, e);
            MessageDialog.openError(shell, "Import Command Stack",
                    "Unable to import command stack. Details:\n" + e);
            return null;
        }

        if (stackObject.has("commands")) {
            for (var commandEl : stackObject.getAsJsonArray("commands")) {
                var commandObject = commandEl.getAsJsonObject();

                var name = commandObject.get("name").getAsString();

                CommandInfo mdbInfo;
                if (commandObject.has("namespace")) {
                    var namespace = commandObject.get("namespace").getAsString();
                    mdbInfo = YamcsPlugin.getMissionDatabase().getCommandInfo(namespace, name);
                    if (mdbInfo == null) {
                        MessageDialog.openError(shell, "Import Command Stack",
                                "Command " + name + " (" + namespace + ") does not exist in MDB");
                        return null;
                    }
                } else {
                    mdbInfo = YamcsPlugin.getMissionDatabase().getCommandInfo(name);
                    if (mdbInfo == null) {
                        MessageDialog.openError(shell, "Import Command Stack",
                                "Command " + name + " does not exist in MDB");
                        return null;
                    }
                }

                var command = new StackedCommand();
                command.setMetaCommand(mdbInfo);
                if (commandObject.has("comment")) {
                    var comment = commandObject.get("comment").getAsString();
                    command.setComment(comment);
                }
                if (commandObject.has("advancement")) {
                    var advancementObject = commandObject.get("advancement").getAsJsonObject();
                    if (advancementObject.has("wait")) {
                        var wait = commandObject.get("delayMs").getAsInt();
                        command.setDelayMs(wait);
                    }
                }
                if (commandObject.has("arguments")) {
                    var argumentsArray = commandObject.get("arguments").getAsJsonArray();
                    for (var argumentEl : argumentsArray) {
                        var argumentObject = argumentEl.getAsJsonObject();

                        var argName = argumentObject.get("name").getAsString();
                        var argValue = argumentObject.get("value");
                        var argInfo = getArgumentFromYamcs(mdbInfo, argName);
                        if (argInfo == null) {
                            MessageDialog.openError(shell, "Import Command Stack",
                                    "Argument " + argName + " does not exist in MDB for command " + name);
                            return null;
                        }
                        if (argValue.isJsonNull()) {
                            command.addAssignment(argInfo, null);
                        } else if (argValue.isJsonPrimitive()) {
                            command.addAssignment(argInfo, argValue.getAsString());
                        } else if (argValue.isJsonArray()) {
                            command.addAssignment(argInfo, argValue.getAsJsonArray().toString());
                        } else if (argValue.isJsonObject()) {
                            command.addAssignment(argInfo, argValue.getAsJsonObject().toString());
                        } else {
                            throw new IllegalArgumentException("Unexpected value: " + argValue);
                        }
                    }
                }

                commands.add(command);
            }
        }

        return commands;
    }

    private List<StackedCommand> parseXmlCommandStack(Shell shell, Path file) {
        try {
            var dbFactory = DocumentBuilderFactory.newInstance();
            var dBuilder = dbFactory.newDocumentBuilder();
            var doc = dBuilder.parse(file.toFile());
            doc.getDocumentElement().normalize();

            var nodes = doc.getElementsByTagName("command");

            var commands = new ArrayList<StackedCommand>();
            for (var i = 0; i < nodes.getLength(); i++) {
                var node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    var commandElement = (Element) node;
                    var qname = commandElement.getAttribute("qualifiedName");

                    var mdbInfo = YamcsPlugin.getMissionDatabase().getCommandInfo(qname);
                    if (mdbInfo == null) {
                        MessageDialog.openError(shell, "Import Command Stack",
                                "Command " + qname + " does not exist in MDB.");
                        return null;
                    }

                    var command = new StackedCommand();
                    command.setMetaCommand(mdbInfo);
                    if (commandElement.hasAttribute("comment")) {
                        var comment = commandElement.getAttribute("comment");
                        command.setComment(comment);
                    }
                    if (commandElement.hasAttribute("delayMs")) {
                        var delay = Integer.parseInt(commandElement.getAttribute("delayMs"));
                        command.setDelayMs(delay);
                    }

                    var argNodes = commandElement.getElementsByTagName("commandArgument");
                    for (var j = 0; j < argNodes.getLength(); j++) {
                        var argNode = argNodes.item(j);
                        if (argNode.getNodeType() == Node.ELEMENT_NODE) {
                            var argElement = (Element) argNode;
                            var argName = argElement.getAttribute("argumentName");
                            var argValue = argElement.getAttribute("argumentValue");
                            var argInfo = getArgumentFromYamcs(mdbInfo, argName);
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
        for (var a : mc.getArgumentList()) {
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
