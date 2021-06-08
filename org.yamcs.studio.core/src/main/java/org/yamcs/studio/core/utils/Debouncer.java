package org.yamcs.studio.core.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Debouncer {

    private boolean ownsExec;
    private ScheduledExecutorService exec;
    private Future<?> delayedFuture;

    public Debouncer() {
        exec = Executors.newSingleThreadScheduledExecutor();
        ownsExec = true;
    }

    public Debouncer(ScheduledExecutorService exec) {
        this.exec = exec;
        ownsExec = false;
    }

    /**
     * Debounces the runnable by the specified delay.
     */
    public void debounce(Runnable runnable, long delay, TimeUnit unit) {
        if (delayedFuture != null && !delayedFuture.isDone()) {
            delayedFuture.cancel(true);
        }
        delayedFuture = exec.schedule(runnable, delay, unit);
    }

    public void shutdown() {
        if (!ownsExec) {
            throw new UnsupportedOperationException("Don't shutdown a debouncer that uses an external scheduler");
        }
        exec.shutdownNow();
    }
}
