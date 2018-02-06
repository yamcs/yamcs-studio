package org.yamcs.studio.commanding.cmdhist;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.protobuf.Commanding.CommandId;
import org.yamcs.protobuf.Yamcs.Value;
import org.yamcs.studio.commanding.PTVInfo;
import org.yamcs.utils.TimeEncoding;

import com.google.protobuf.ByteString;

/**
 * Keeps an assembled state of multiple events related to one CommandId
 */
public class CommandHistoryRecord {

    public enum CommandState {
        UNKNOWN, COMPLETED, FAILED
    }

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

    private CommandId id;
    private CommandState commandState = CommandState.UNKNOWN;
    private String commandString;
    private ByteString binary;
    private String username = "anonymous";
    private String finalSequenceCount;
    private PTVInfo ptvInfo;
    private Map<String, Map<String, Object>> cellPropsByColumn = new LinkedHashMap<>();

    private Map<String, VerificationStep> verificationStepsByName = new LinkedHashMap<>();

    public CommandHistoryRecord(CommandId id) {
        this.id = id;
        ptvInfo = new PTVInfo();
    }

    public CommandId getCommandId() {
        return id;
    }

    public void setFinalSequenceCount(Value finalSequenceCount) {
        this.finalSequenceCount = valueToString(finalSequenceCount);
    }

    public void setCommandString(Value commandString) {
        this.commandString = valueToString(commandString);
    }

    public void setUsername(Value username) {
        this.username = valueToString(username);
    }

    public void setCommandState(CommandState commandState) {
        this.commandState = commandState;
    }

    public void setBinary(ByteString binary) {
        this.binary = binary;
    }

    public void addVerificationStep(VerificationStep step) {
        verificationStepsByName.put(step.getName(), step);
    }

    public void updateVerificationStepTime(String shortName, CommandHistoryAttribute timeAttribute) {
        VerificationStep step = verificationStepsByName.get(shortName);
        step.setTime(timeAttribute);
    }

    public void addCellValue(String columnName, Value value) {
        if (!cellPropsByColumn.containsKey(columnName)) {
            cellPropsByColumn.put(columnName, new HashMap<>());
        }

        cellPropsByColumn.get(columnName).put(KEY_RAW_VALUE, valueToRawValue(value));
        if (value.getType() == Value.Type.TIMESTAMP) {
            cellPropsByColumn.get(columnName).put(KEY_ACK_DURATION, id.getGenerationTime() - value.getTimestampValue());
            cellPropsByColumn.get(columnName).put(KEY_VALUE, TimeEncoding.toString(value.getTimestampValue()));
            cellPropsByColumn.get(columnName).put(KEY_RELATIVE_VALUE,
                    toHumanTimeDiff(value.getTimestampValue(), id.getGenerationTime()));
            cellPropsByColumn.get(columnName).put(KEY_TOOLTIP, TimeEncoding.toString(value.getTimestampValue()));
        } else {
            cellPropsByColumn.get(columnName).put(KEY_VALUE, valueToString(value));
        }
    }

    private String toHumanTimeDiff(long generationTime, long timestamp) {
        long millis = generationTime - timestamp;
        String sign = (millis >= 0) ? "+" : "-";
        if (millis >= ONE_DAY) {
            return TimeEncoding.toString(timestamp);
        } else if (millis >= ONE_HOUR) {
            return sign + String.format("%d h, %d m",
                    MILLISECONDS.toHours(millis),
                    MILLISECONDS.toMinutes(millis) - HOURS.toMinutes(MILLISECONDS.toHours(millis)));
        } else if (millis >= ONE_MINUTE) {
            return sign + String.format("%d m, %d s",
                    MILLISECONDS.toMinutes(millis),
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
        if (!cellPropsByColumn.containsKey(columnName))
            return null;
        return (String) cellPropsByColumn.get(columnName).get(KEY_IMAGE);
    }

    public int getSequenceNumber() {
        return id.getSequenceNumber();
    }

    public String getCommandName() {
        return id.getCommandName();
    }

    public String getCommandString() {
        return commandString;
    }

    public String getUsername() {
        return username;
    }

    public CommandState getCommandState() {
        return commandState;
    }

    public String getGenerationTime() {
        return TimeEncoding.toString(id.getGenerationTime());
    }

    public long getRawGenerationTime() {
        return id.getGenerationTime();
    }

    public String getFinalSequenceCount() {
        return finalSequenceCount;
    }

    public String getOrigin() {
        return id.getOrigin();
    }

    public ByteString getBinary() {
        return binary;
    }

    public PTVInfo getPTVInfo() {
        return ptvInfo;
    }

    public List<VerificationStep> getVerificationSteps() {
        return new ArrayList<>(verificationStepsByName.values());
    }

    public String getTextForColumn(String columnName, boolean showRelativeTime) {
        Map<String, Object> props = cellPropsByColumn.get(columnName);
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
        Map<String, Object> props = cellPropsByColumn.get(columnName);
        if (props != null && props.get(KEY_IMAGE) != null) {
            return String.valueOf(props.get(KEY_IMAGE));
        }
        return null;
    }

    public String getTooltipForColumn(String columnName) {
        Map<String, Object> props = cellPropsByColumn.get(columnName);
        if (props != null && props.get(KEY_TOOLTIP) != null) {
            return String.valueOf(props.get(KEY_TOOLTIP));
        }
        return null;
    }

    /**
     * How long it took before the ACK for this dynamic column arrived. (relative to generationtime for the row)
     */
    public long getAckDurationForColumn(String columnName) {
        Map<String, Object> props = cellPropsByColumn.get(columnName);
        if (props != null && props.get(KEY_ACK_DURATION) != null) {
            return (long) props.get(KEY_ACK_DURATION);
        }
        return 0;
    }

    private Object valueToRawValue(Value value) {
        if (value == null)
            return null;
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
        if (value == null)
            return null;
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
}
