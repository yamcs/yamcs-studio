/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.script;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.yamcs.client.base.ResponseObserver;
import org.yamcs.client.storage.ObjectId;
import org.yamcs.studio.commanding.CommandParser;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.yamcs.YamcsVType;
import org.yamcs.studio.spell.api.ExecutorInfo;
import org.yamcs.studio.spell.api.StartProcedureRequest;

public class Yamcs {

    public static final Logger log = Logger.getLogger(Yamcs.class.getName());

    /**
     * Sample use:
     *
     * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)');
     */
    public static void issueCommand(String commandText) {
        var parsed = CommandParser.parseCommand(commandText);

        var processor = YamcsPlugin.getProcessorClient();
        if (processor == null) {
            log.warning("No active processor");
            return;
        }

        // Support ops:// namespace, made to look similar with ops:// parameter naming
        var commandName = parsed.getQualifiedName();
        if (commandName.startsWith("ops://")) {
            commandName = commandName.replace("ops://", "MDB:OPS Name/");
        }

        var builder = processor.prepareCommand(commandName)
                .withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
        for (var arg : parsed.getAssignments()) {
            builder.withArgument(arg.getName(), arg.getValue());
        }
        builder.issue().whenComplete((result, t) -> {
            if (t != null) {
                log.log(Level.SEVERE, "Failed to issue command: " + t, t);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(null, "Failed to issue command", t.getMessage());
                });
            }
        });
    }

    /**
     * Sample use:
     *
     * Yamcs.issueCommand('/YSS/SIMULATOR/SWITCH_VOLTAGE_ON', {"voltage_num": 1});
     */
    public static void issueCommand(String command, Map<String, Object> args) {
        var processor = YamcsPlugin.getProcessorClient();
        if (processor == null) {
            log.warning("No active processor");
            return;
        }

        // Support ops:// namespace, made to look similar with ops:// parameter naming
        if (command.startsWith("ops://")) {
            command = command.replace("ops://", "MDB:OPS Name/");
        }

        var builder = processor.prepareCommand(command).withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
        if (args != null) {
            for (var arg : args.entrySet()) {
                builder.withArgument(arg.getKey(), String.valueOf(arg.getValue()));
            }
        }
        builder.issue().whenComplete((result, t) -> {
            if (t != null) {
                log.log(Level.SEVERE, "Failed to issue command: " + t, t);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(null, "Failed to issue command", t.getMessage());
                });
            }
        });
    }

    /**
     * Submit a SPELL procedure.
     * <p>
     * Only useful when the connected Yamcs backend supports this (not publicly released).
     */
    public static void runProcedure(String procedure) {
        runProcedure(procedure, Collections.emptyMap());
    }

    /**
     * Submit a SPELL procedure with arguments.
     * <p>
     * Only useful when the connected Yamcs backend supports this (not publicly released).
     */
    public static void runProcedure(String procedure, Map<String, Object> args) {
        var spellClient = YamcsPlugin.getSpellClient();
        if (spellClient == null) {
            log.warning("Not running procedure " + procedure + ": not connected");
            return;
        }

        var stringArgs = new LinkedHashMap<String, String>();
        args.forEach((k, v) -> stringArgs.put(k, "" + Objects.requireNonNull(v, "Argument cannot be null")));

        var request = StartProcedureRequest.newBuilder()
                .setInstance(YamcsPlugin.getInstance())
                .setProcedure(procedure)
                .putAllArguments(stringArgs)
                .build();

        var f = new CompletableFuture<ExecutorInfo>();
        spellClient.startProcedure(null, request, new ResponseObserver<>(f));
        f.whenComplete((executor, t) -> {
            if (t != null) {
                log.log(Level.SEVERE, "Failed to submit procedure: " + t, t);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(null, "Failed to submit procedure", t.getMessage());
                });
            } else {
                log.log(Level.INFO, "Submitted procedure " + executor.getId());
            }
        });
    }

    /**
     * Returns the "monitoring result" of a Yamcs parameter. One of IN_LIMITS, DISABLED, WATCH, WARNING, DISTRESS,
     * CRITICAL or SEVERE.
     * <p>
     * For PVs that are not connected to Yamcs parameters, this will always return null.
     */
    public static String getMonitoringResult(IPV pv) {
        var vtype = PVUtil.checkPVValue(pv);
        if (!(vtype instanceof YamcsVType)) {
            throw new IllegalArgumentException("PV " + pv.getName() + " is not a Yamcs parameter PV");
        }

        var pval = ((YamcsVType) vtype).getParameterValue();
        if (pval.hasMonitoringResult()) {
            return pval.getMonitoringResult().toString();
        }

        return null;
    }

    /**
     * Write a text file to a bucket.
     * <p>
     * Sample use: Yamcs.writeTextFileToBucket("bucketName", "some/file.txt", "Hello!\n");
     *
     * @param bucket
     *            target bucket.
     * @param objectName
     *            target object name.
     * @param text
     *            the text to be written to the object.
     */
    public static void writeTextFileToBucket(String bucket, String objectName, String text) {
        var objectId = ObjectId.of(bucket, objectName);

        var storage = YamcsPlugin.getStorageClient();
        var bytes = text.getBytes(StandardCharsets.UTF_8);
        storage.uploadObject(objectId, bytes).whenComplete((result, t) -> {
            if (t != null) {
                log.log(Level.SEVERE, "Failed to write to " + objectId + "\": " + t, t);
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openError(null, "Failed to write to " + objectId, t.getMessage());
                });
            }
        });
    }
}
