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
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.yamcs.protobuf.IssueCommandRequest.Assignment;
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
        for (Assignment arg : parsed.getAssignments()) {
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
            for (Entry<String, Object> arg : args.entrySet()) {
                builder.withArgument(arg.getKey(), String.valueOf(arg.getValue()));
            }
        }
        builder.issue();
    }
}
