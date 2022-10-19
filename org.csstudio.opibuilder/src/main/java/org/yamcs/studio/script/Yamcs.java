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

import java.util.Map;
import java.util.logging.Logger;

import org.yamcs.client.storage.ObjectId;
import org.yamcs.studio.commanding.CommandParser;
import org.yamcs.studio.core.YamcsPlugin;

public class Yamcs {

    public static Logger log = Logger.getLogger(Yamcs.class.getName());

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

        var builder = processor.prepareCommand(parsed.getQualifiedName())
                .withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
        for (var arg : parsed.getAssignments()) {
            builder.withArgument(arg.getName(), arg.getValue());
        }
        builder.issue();
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

        var builder = processor.prepareCommand(command).withSequenceNumber(YamcsPlugin.nextCommandSequenceNumber());
        if (args != null) {
            for (var arg : args.entrySet()) {
                builder.withArgument(arg.getKey(), String.valueOf(arg.getValue()));
            }
        }
        builder.issue();
    }

    /**
     * Write a text file to a bucket.
     * <p>
     * Sample use: Yamcs.writeTextFileToBucket("ys://bucketName", "fileName.txt", "Hello!\n");
     *
     * @param bucketUrl
     *            bucket URL.
     * @param fileName
     *            file name with extension.
     * @param text
     *            the text to be written to the file.
     */
    public static void writeTextFileToBucket(String bucketUrl, String fileName, String text) {
        var targetUrl = bucketUrl + "/" + fileName;
        ObjectId targetObject = ObjectId.parseURL(targetUrl);

        byte[] byteArray = text.getBytes();

        var storage = YamcsPlugin.getStorageClient();
        storage.uploadObject(targetObject, byteArray).exceptionally((ex) -> {
            log.warning("Recovered from \"" + ex.getMessage() + "\"");
            return null;
        });
    }

}
