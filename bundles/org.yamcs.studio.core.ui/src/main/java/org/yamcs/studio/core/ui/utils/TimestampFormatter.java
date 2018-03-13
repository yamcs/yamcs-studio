package org.yamcs.studio.core.ui.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class TimestampFormatter {

    public static String format(long instant) {
        Date date = new Date(instant);
        SimpleDateFormat sdf = new SimpleDateFormat(YamcsUIPlugin.getDefault().getPreferenceStore()
                .getString("views.dateFormat"));
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

}
