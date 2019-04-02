package org.yamcs.studio.commanding.cmdhist;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Locale;

import org.yamcs.protobuf.Commanding.CommandHistoryAttribute;
import org.yamcs.studio.core.ui.YamcsUIPlugin;
import org.yamcs.utils.TimeEncoding;

public class Stage {

    private static final long ONE_SECOND = 1000; // millis
    private static final long ONE_MINUTE = 60 * ONE_SECOND;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;

    private CommandHistoryRecord rec;
    private String name;
    private String status;
    private long instant = TimeEncoding.INVALID_INSTANT;

    public Stage(CommandHistoryRecord rec, String name, CommandHistoryAttribute statusAttribute) {
        this.rec = rec;
        this.name = name;
        this.status = statusAttribute.getValue().getStringValue();
    }

    public void setTime(CommandHistoryAttribute timeAttribute) {
        this.instant = timeAttribute.getValue().getTimestampValue();
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        if (instant != TimeEncoding.INVALID_INSTANT) {
            return YamcsUIPlugin.getDefault().formatInstant(instant);
        } else {
            return null;
        }
    }

    public String getDelta() {
        if (instant != TimeEncoding.INVALID_INSTANT) {
            return toHumanTimeDiff(instant, rec.getRawGenerationTime());
        } else {
            return null;
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
}
