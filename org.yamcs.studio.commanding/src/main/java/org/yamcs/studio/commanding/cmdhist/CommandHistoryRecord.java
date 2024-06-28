/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.cmdhist;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.yamcs.client.Command;
import org.yamcs.client.Helpers;
import org.yamcs.protobuf.Commanding.CommandAssignment;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.commanding.CommandingPlugin;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * Keeps an assembled state of multiple events related to one CommandId
 */
public class CommandHistoryRecord {

    private static final String KEY_RAW_VALUE = "RAW_VALUE";
    private static final String KEY_ACK_DURATION = "ACK_DURATION";
    private static final String KEY_VALUE = "VALUE";
    private static final String KEY_RELATIVE_VALUE = "RELATIVE_VALUE";
    private static final String KEY_IMAGE = "IMAGE";
    private static final String KEY_TOOLTIP = "TOOLTIP";

    private static final Logger log = Logger.getLogger(CommandHistoryRecord.class.getName());

    private static final long ONE_SECOND = 1000; // millis
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;

    private static final char[] HEXCHARS = "0123456789abcdef".toCharArray();

    private Command command;

    private String displayedName;
    private String source;

    private Map<String, Map<String, Object>> cellPropsByColumn = new LinkedHashMap<>();

    public CommandHistoryRecord(Command command) {
        this.command = command;

        displayedName = command.getName();
        var preferredNamespace = CommandingPlugin.getDefault().getPreferredNamespace();
        if (preferredNamespace != null) {
            var alias = command.getName(preferredNamespace);
            if (alias != null) {
                displayedName = alias;
            }
        }

        source = buildSource();
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public void addCellValue(String columnName, Value value) {
        if (!cellPropsByColumn.containsKey(columnName)) {
            cellPropsByColumn.put(columnName, new HashMap<>());
        }

        cellPropsByColumn.get(columnName).put(KEY_RAW_VALUE, valueToRawValue(value));
        if (value.getType() == Value.Type.TIMESTAMP) {
            var valueTime = Instant.parse(value.getStringValue());
            cellPropsByColumn.get(columnName).put(KEY_ACK_DURATION,
                    command.getGenerationTime().toEpochMilli() - valueTime.toEpochMilli());
            cellPropsByColumn.get(columnName).put(KEY_VALUE, valueTime);
            cellPropsByColumn.get(columnName).put(KEY_RELATIVE_VALUE,
                    toHumanTimeDiff(valueTime, command.getGenerationTime()));
            cellPropsByColumn.get(columnName).put(KEY_TOOLTIP, Instant.parse(value.getStringValue()));
        } else {
            cellPropsByColumn.get(columnName).put(KEY_VALUE, valueToString(value));
        }
    }

    private String toHumanTimeDiff(Instant generationTime, Instant timestamp) {
        var millis = generationTime.toEpochMilli() - timestamp.toEpochMilli();
        var sign = (millis >= 0) ? "+" : "-";
        if (millis >= ONE_DAY) {
            return YamcsPlugin.getDefault().formatInstant(timestamp);
        } else if (millis >= ONE_HOUR) {
            return sign + String.format("%d h, %d m", MILLISECONDS.toHours(millis),
                    MILLISECONDS.toMinutes(millis) - HOURS.toMinutes(MILLISECONDS.toHours(millis)));
        } else if (millis >= ONE_MINUTE) {
            return sign + String.format("%d m, %d s", MILLISECONDS.toMinutes(millis),
                    MILLISECONDS.toSeconds(millis) - MINUTES.toSeconds(MILLISECONDS.toMinutes(millis)));
        } else {
            return String.format(Locale.US, "%+,d ms", millis);
        }
    }

    public void addCellImage(String columnName, String imageLocation) {
        if (!cellPropsByColumn.containsKey(columnName)) {
            cellPropsByColumn.put(columnName, new HashMap<>());
        }
        cellPropsByColumn.get(columnName).put(KEY_IMAGE, imageLocation);
    }

    public String getCellImage(String columnName) {
        if (!cellPropsByColumn.containsKey(columnName)) {
            return null;
        }
        return (String) cellPropsByColumn.get(columnName).get(KEY_IMAGE);
    }

    public String getTextForColumn(String columnName, boolean showRelativeTime) {
        var props = cellPropsByColumn.get(columnName);
        if (props != null) {
            if (showRelativeTime && props.get(KEY_RELATIVE_VALUE) != null) {
                return String.valueOf(props.get(KEY_RELATIVE_VALUE));
            } else if (props.get(KEY_VALUE) != null) {
                return String.valueOf(props.get(KEY_VALUE));
            }
        }
        return null;
    }

    public String getImageForColumn(String columnName) {
        var props = cellPropsByColumn.get(columnName);
        if (props != null && props.get(KEY_IMAGE) != null) {
            return String.valueOf(props.get(KEY_IMAGE));
        }
        return null;
    }

    public String getTooltipForColumn(String columnName) {
        var props = cellPropsByColumn.get(columnName);
        if (props != null && props.get(KEY_TOOLTIP) != null) {
            return String.valueOf(props.get(KEY_TOOLTIP));
        }
        return null;
    }

    /**
     * How long it took before the ACK for this dynamic column arrived. (relative to generationtime for the row)
     */
    public long getAckDurationForColumn(String columnName) {
        var props = cellPropsByColumn.get(columnName);
        if (props != null && props.get(KEY_ACK_DURATION) != null) {
            return (long) props.get(KEY_ACK_DURATION);
        }
        return 0;
    }

    public String getSource() {
        return source;
    }

    private String buildSource() {
        StringBuilder buf = new StringBuilder(displayedName).append("(");
        buf.append(command.getAssignments().stream()
                .filter(CommandAssignment::getUserInput)
                .map(assignment -> {
                    Object value = Helpers.parseValue(assignment.getValue());
                    if (value instanceof String) {
                        return assignment.getName() + ": \"" + value + "\"";
                    } else if (value instanceof byte[]) {
                        return assignment.getName() + ": 0x" + toHex((byte[]) value);
                    } else {
                        return assignment.getName() + ": " + value;
                    }
                }).collect(Collectors.joining(", ")));
        return buf.append(")").toString();
    }

    public String printArguments() {
        var buf = new StringBuilder();
        buf.append(command.getAssignments().stream()
                .filter(CommandAssignment::getUserInput)
                .map(assignment -> {
                    Object value = Helpers.parseValue(assignment.getValue());
                    if (value instanceof String) {
                        return assignment.getName() + ": \"" + value + "\"";
                    } else if (value instanceof byte[]) {
                        return assignment.getName() + ": 0x" + toHex((byte[]) value);
                    } else {
                        return assignment.getName() + ": " + value;
                    }
                }).collect(Collectors.joining("\n")));
        return buf.toString();
    }

    private static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEXCHARS[v >>> 4];
            hexChars[j * 2 + 1] = HEXCHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    private Object valueToRawValue(Value value) {
        if (value == null) {
            return null;
        }
        switch (value.getType()) {
        case STRING:
            return value.getStringValue();
        case DOUBLE:
            return value.getDoubleValue();
        case FLOAT:
            return value.getFloatValue();
        case UINT32:
            return value.getUint32Value();
        case UINT64:
            return value.getUint64Value();
        case SINT32:
            return value.getSint32Value();
        case SINT64:
            return value.getSint64Value();
        case BOOLEAN:
            return value.getBooleanValue();
        case BINARY:
            return value.getBinaryValue();
        case TIMESTAMP:
            return value.getTimestampValue();
        default:
            log.warning("Unexpected attribute of type " + value.getType());
            return "?";
        }
    }

    private String valueToString(Value value) {
        if (value == null) {
            return null;
        }
        switch (value.getType()) {
        case STRING:
            return value.getStringValue();
        case DOUBLE:
            return String.valueOf(value.getDoubleValue());
        case FLOAT:
            return String.valueOf(value.getFloatValue());
        case UINT32:
            return String.valueOf(value.getUint32Value());
        case UINT64:
            return String.valueOf(value.getUint64Value());
        case SINT32:
            return String.valueOf(value.getSint32Value());
        case SINT64:
            return String.valueOf(value.getSint64Value());
        case BOOLEAN:
            return String.valueOf(value.getBooleanValue());
        case BINARY:
            return "<binary>";
        default:
            log.warning("Unexpected attribute of type " + value.getType());
            return "?";
        }
    }

    public Command getCommand() {
        return command;
    }

    public void merge(Command other) {
        command.merge(other);
    }
}
