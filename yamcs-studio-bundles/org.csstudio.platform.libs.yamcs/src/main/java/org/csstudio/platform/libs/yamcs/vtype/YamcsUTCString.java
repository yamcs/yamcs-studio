package org.csstudio.platform.libs.yamcs.vtype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses dates using SimpleDateFormat patterns. This is thread safe unlike the
 * normal SimpleDateFormat
 */
public class YamcsUTCString {
    private static final Logger log = Logger.getLogger(YamcsUTCString.class.getName());
    static volatile Map<String, ThreadLocal<SimpleDateFormat>> formatters = new HashMap<>();
    static public String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static Date parse(final String pattern, String source) {
        ThreadLocal<SimpleDateFormat> f = formatters.get(pattern);
        if (f == null) {
            Map<String, ThreadLocal<SimpleDateFormat>> tmp = new HashMap<>(formatters);
            f = new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    SimpleDateFormat s1 = new SimpleDateFormat(pattern);
                    s1.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return s1;
                }
            };
            tmp.put(pattern, f);
            formatters = tmp;
        }
        try {
            return f.get().parse(source);
        } catch (ParseException e) {
            log.log(Level.WARNING, "Could not parse incoming date string", e);
            return null;
        }
    }

    public static Date parse(String source) {
        if (source == null)
            return null;
        return parse(DEFAULT_DATE_FORMAT, source);
    }
}
