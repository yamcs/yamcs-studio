package org.yamcs.studio.core;

import java.util.Calendar;
import java.util.TimeZone;

import org.yamcs.api.YamcsConnectData;
import org.yamcs.api.ws.YamcsConnectionProperties;
import org.yamcs.protobuf.Yamcs.TimeInfo;
import org.yamcs.studio.core.web.RestClient;
import org.yamcs.utils.TimeEncoding;

/**
 * It would be cool if we could just use E4 annotations to pass this information around instead of
 * needing this class.
 */
public class TimeCatalogue implements StudioConnectionListener, TimeListener {

    private volatile long currentTime = TimeEncoding.INVALID_INSTANT;

    public static TimeCatalogue getInstance() {
        return YamcsPlugin.getDefault().getTimeCatalogue();
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

    @Override
    public void onStudioConnect(YamcsConnectionProperties webProps, YamcsConnectData hornetqProps, RestClient restclient, WebSocketRegistrar webSocketClient) {
        webSocketClient.addTimeListener(this);
        currentTime = TimeEncoding.INVALID_INSTANT;
    }

    @Override
    public void processTime(TimeInfo timeInfo) {
        currentTime = timeInfo.getCurrentTime();
    }

    @Override
    public void onStudioDisconnect() {
        currentTime = TimeEncoding.INVALID_INSTANT;
    }
}
