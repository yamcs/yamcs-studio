package org.yamcs.studio.core.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.studio.core.ConnectionManager;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsClient;
import org.yamcs.utils.TimeEncoding;

public class TimeCatalogue implements Catalogue {

    private volatile long currentTime = TimeEncoding.INVALID_INSTANT;
    private Set<TimeListener> timeListeners = new CopyOnWriteArraySet<>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static TimeCatalogue getInstance() {
        return YamcsPlugin.getDefault().getCatalogue(TimeCatalogue.class);
    }

    public void addTimeListener(TimeListener listener) {
        timeListeners.add(listener);

        // Inform listeners of the current model
        listener.processTime(currentTime);
    }

    public void removeTimeListener(TimeListener listener) {
        timeListeners.remove(listener);
    }

    public long getMissionTime() {
        return getMissionTime(false);
    }

    public long getMissionTime(boolean wallClockIfUnset) {
        long t = currentTime;
        if (wallClockIfUnset && t == TimeEncoding.INVALID_INSTANT)
            t = TimeEncoding.getWallclockTime();
        return t;
    }

    public Calendar getMissionTimeAsCalendar() {
        return getMissionTimeAsCalendar(false);
    }

    public Calendar getMissionTimeAsCalendar(boolean wallClockIfUnset) {
        long t = getMissionTime(wallClockIfUnset);
        if (t == TimeEncoding.INVALID_INSTANT)
            return null;

        Calendar cal = TimeEncoding.toCalendar(t);
        cal.setTimeZone(getTimeZone());
        return cal;
    }

    public TimeZone getTimeZone() {
        // Currently always using local timezone, because need to hack into XYChart because
        // it doesn't support Timezones. Only date formats seem to be accounted for.
        // At least for now, it should stay consistent with the workbench
        // TODO Research modifications to SWT xychart and then make this controllable from user prefs
        return TimeZone.getDefault();
    }

    // must be called on the swt thread due to the dateformatter being reused
    public String toString(long instant) {
        // TODO Improve this. Don't use Date
        Calendar cal = TimeEncoding.toCalendar(instant);
        cal.setTimeZone(TimeCatalogue.getInstance().getTimeZone());
        format.setTimeZone(cal.getTimeZone());
        return format.format(cal.getTime());
    }

    @Override
    public void onStudioConnect() {
        YamcsClient yamcsClient = ConnectionManager.getInstance().getYamcsClient();
        yamcsClient.sendMessage(new WebSocketRequest("time", "subscribe"));
        distributeTime(TimeEncoding.INVALID_INSTANT);
    }

    public void processTimeInfo(TimeInfo timeInfo) {
        distributeTime(timeInfo.getCurrentTime());
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        distributeTime(TimeEncoding.INVALID_INSTANT);
    }

    @Override
    public void onStudioDisconnect() {
        distributeTime(TimeEncoding.INVALID_INSTANT);
    }

    private void distributeTime(long time) {
        currentTime = time;
        timeListeners.forEach(l -> l.processTime(time));
    }
}
