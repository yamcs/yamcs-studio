package org.csstudio.yamcs.commanding;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.yamcs.protostuff.CommandId;
import org.yamcs.protostuff.Value;
import org.yamcs.protostuff.Value.Type;
import org.yamcs.utils.TimeEncoding;

/**
 * Keeps an assembled state of multiple events related to one CommandId
 */
public class TelecommandRecord {
    
    private static final String KEY_VALUE = "VALUE";
    private static final String KEY_IMAGE = "IMAGE";
    private static final String KEY_TOOLTIP = "TOOLTIP";
    
    private static final Logger log = Logger.getLogger(TelecommandRecord.class.getName());
    public static final String STATUS_SUFFIX = "_Status";
    public static final String TIME_SUFFIX = "_Time";
    
    private static final long ONE_SECOND = 1000; // millis
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;
    
    private CommandId id;
    private String source;
    private String username = "anonymous";
    private String finalSequenceCount;
    // TODO should use some custom object here, not properties
    private Map<String, Properties> cellPropsByColumn = new LinkedHashMap<>();
    
    public TelecommandRecord(CommandId id) {
        this.id = id;
    }
    
    public void setFinalSequenceCount(Value finalSequenceCount) {
        this.finalSequenceCount = valueToString(finalSequenceCount);
    }
    
    public void setSource(Value source) {
        this.source = valueToString(source);
    }
    
    public void setUsername(Value username) {
        this.username = valueToString(username);
    }
    
    public void addCellValue(String columnName, Value value) {
        if (!cellPropsByColumn.containsKey(columnName)) {
            cellPropsByColumn.put(columnName, new Properties());
        }
        
        if (value.getType() == Type.TIMESTAMP) {
            cellPropsByColumn.get(columnName).setProperty(KEY_VALUE, toHumanTimeDiff(value.getTimestampValue(), id.getGenerationTime()));
            cellPropsByColumn.get(columnName).setProperty(KEY_TOOLTIP, TimeEncoding.toString(value.getTimestampValue()));
        } else {
            cellPropsByColumn.get(columnName).setProperty(KEY_VALUE, valueToString(value));
        }
    }
        
    public String toHumanTimeDiff(long generationTime, long timestamp) {
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
            cellPropsByColumn.put(columnName, new Properties());
        }
        cellPropsByColumn.get(columnName).setProperty(KEY_IMAGE, imageLocation);
    }
    
    public int getSequenceNumber() {
        return id.getSequenceNumber();
    }
    
    public String getCommandName() {
        return id.getCommandName();
    }
    
    public String getSource() {
        return source;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getGenerationTime() {
        return TimeEncoding.toString(id.getGenerationTime());
    }
    
    public String getFinalSequenceCount() {
        return finalSequenceCount;
    }
    
    public String getOrigin() {
        return id.getOrigin();
    }
    
    public String getTextForColumn(String columnName) {
        Properties props = cellPropsByColumn.get(columnName);
        return (props!=null) ? props.getProperty(KEY_VALUE) : null;
    }
    
    public String getImageForColumn(String columnName) {
        Properties props = cellPropsByColumn.get(columnName);
        return (props!=null) ? props.getProperty(KEY_IMAGE) : null;
    }
    
    public String getTooltipForColumn(String columnName) {
        Properties props = cellPropsByColumn.get(columnName);
        return (props!=null) ? props.getProperty(KEY_TOOLTIP) : null;
    }
    
    private String valueToString(Value value) {
        if (value == null) return null;
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
