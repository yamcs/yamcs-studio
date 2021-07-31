package org.yamcs.studio.archive;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Debouncer {

    private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private Future<?> delayed;

    public void debounce(Runnable runnable, long delay, TimeUnit unit) {
        var prev = delayed;
        delayed = exec.schedule(() -> {
            try {
                runnable.run();
            } finally {
                delayed = null;
            }
        }, delay, unit);
        if (prev != null) {
            prev.cancel(true);
        }
    }

    public void shutdown() {
        exec.shutdownNow();
    }
}
