package org.yamcs.studio.core.model;

import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.api.ws.WebSocketClientCallback;
import org.yamcs.api.ws.WebSocketRequest;
import org.yamcs.protobuf.Web.WebSocketServerMessage.WebSocketSubscriptionData;
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.client.YamcsStudioClient;
import org.yamcs.utils.TimeEncoding;

public class TimeCatalogue implements Catalogue, WebSocketClientCallback {

    private volatile long currentTime = TimeEncoding.INVALID_INSTANT;
    private Set<TimeListener> timeListeners = new CopyOnWriteArraySet<>();

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
        if (wallClockIfUnset && t == TimeEncoding.INVALID_INSTANT) {
            t = TimeEncoding.getWallclockTime();
        }
        return t;
    }

    public Calendar getMissionTimeAsCalendar() {
        return getMissionTimeAsCalendar(false);
    }

    public Calendar getMissionTimeAsCalendar(boolean wallClockIfUnset) {
        long t = getMissionTime(wallClockIfUnset);
        if (t == TimeEncoding.INVALID_INSTANT) {
            return null;
        }

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

    @Override
    public void onYamcsConnected() {
        YamcsStudioClient yamcsClient = YamcsPlugin.getYamcsClient();
        yamcsClient.subscribe(new WebSocketRequest("time", "subscribe"), this);
        distributeTime(TimeEncoding.INVALID_INSTANT);
    }

    @Override
    public void onMessage(WebSocketSubscriptionData msg) {
        if (msg.hasTimeInfo()) {
            TimeInfo timeInfo = msg.getTimeInfo();            
            long instant = TimeEncoding.fromProtobufTimestamp(timeInfo.getCurrentTime());            
            distributeTime(instant);
        }
    }

    @Override
    public void instanceChanged(String oldInstance, String newInstance) {
        distributeTime(TimeEncoding.INVALID_INSTANT);
    }

    @Override
    public void onYamcsDisconnected() {
        distributeTime(TimeEncoding.INVALID_INSTANT);
    }

    private void distributeTime(long time) {
        currentTime = time;
        timeListeners.forEach(l -> l.processTime(time));
    }
}
