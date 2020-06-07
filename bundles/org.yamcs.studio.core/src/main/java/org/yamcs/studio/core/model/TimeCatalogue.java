package org.yamcs.studio.core.model;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArraySet;

import org.yamcs.client.TimeSubscription;
import org.yamcs.studio.core.YamcsPlugin;

public class TimeCatalogue extends Catalogue {

    private volatile Instant currentTime;

    private TimeSubscription subscription;
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

    public Instant getMissionTime() {
        return getMissionTime(false);
    }

    public Instant getMissionTime(boolean wallClockIfUnset) {
        Instant t = currentTime;
        if (wallClockIfUnset && t == null) {
            t = Instant.now();
        }
        return t;
    }

    public TimeZone getTimeZone() {
        // Currently always using local timezone, because need to hack into XYChart because
        // it doesn't support Timezones. Only date formats seem to be accounted for.
        // At least for now, it should stay consistent with the workbench
        // TODO Research modifications to SWT xychart and then make this controllable from user prefs
        return TimeZone.getDefault();
    }

    public ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }

    @Override
    public void changeInstance(String instance) {
        if (subscription != null) {
            subscription.cancel(true);
            timeListeners.forEach(l -> l.processTime(null));
        }

        if (instance == null) {
            timeListeners.forEach(l -> l.processTime(null));
        } else {
            subscription = YamcsPlugin.getYamcsClient().createTimeSubscription();
            subscription.addMessageListener(proto -> {
                currentTime = Instant.ofEpochSecond(proto.getSeconds(), proto.getNanos());
                timeListeners.forEach(l -> l.processTime(currentTime));
            });
        }
    }
}
