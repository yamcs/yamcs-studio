package org.yamcs.studio.css.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class UserLogFormatter extends Formatter {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
    private final Date date = new Date();

    @Override
    public String format(LogRecord record) {
        StringBuilder buf = new StringBuilder();

        // Displayed dates use the local timezone (overridable via -Duser.timezone)
        date.setTime(record.getMillis());
        buf.append("[");
        buf.append(dateFormatter.format(date));
        buf.append("] ");

        buf.append(formatMessage(record));
        buf.append("\n");

        return buf.toString();
    }
}
