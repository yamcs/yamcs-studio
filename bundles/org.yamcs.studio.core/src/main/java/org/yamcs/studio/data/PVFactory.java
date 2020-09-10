package org.yamcs.studio.data;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PVFactory {

    /**
     * The default background thread for PV change event notification. It will only be created on its first use.
     */
    public static ExecutorService SIMPLE_PV_THREAD = null;

    private static final PVFactory INSTANCE = new PVFactory();

    private PVFactory() {
    }

    public static PVFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Create a PV.
     *
     * @param name
     *            name of the PV. Must not be null.
     * @param readOnly
     *            true if the client doesn't need to write to the PV.
     * @param minUpdatePeriodInMs
     *            the minimum update period in milliseconds, which means the PV change event notification will not be
     *            faster than this period.
     * @param bufferAllValues
     *            if all value on the PV should be buffered during two updates.
     * @param notificationThread
     *            the thread on which the read and write listener will be notified. Must not be null.
     * @param exceptionHandler
     *            the handler to handle all exceptions happened in pv connection layer. If this is null, pv read
     *            listener or pv write listener will be notified on read or write exceptions respectively.
     *
     * @return the PV.
     * @throws Exception
     *             error on creating pv.
     */
    public IPV createPV(String name,
            boolean readOnly, long minUpdatePeriodInMs,
            boolean bufferAllValues,
            Executor notificationThread,
            ExceptionHandler exceptionHandler) throws Exception {
        return new IPV(name, notificationThread);
    }

    /**
     * Create a PV with most of the parameters in default value:
     *
     * <pre>
     * readOnly = false;
     * minUpdatePeriod = 10 ms;
     * bufferAllValues = false;
     * notificationThread = {@link #SIMPLE_PV_THREAD}
     * exceptionHandler = null;
     * </pre>
     *
     * @param name
     *            name of the PV. Must not be null.
     * @return the pv.
     * @throws Exception
     *             error on creating pv.
     */
    public synchronized IPV createPV(String name) throws Exception {
        if (SIMPLE_PV_THREAD == null) {
            SIMPLE_PV_THREAD = Executors.newSingleThreadExecutor();
        }
        return createPV(name, false, 10,
                false, SIMPLE_PV_THREAD, null);
    }

    public static synchronized ExecutorService getDefaultPVNotificationThread() {
        if (SIMPLE_PV_THREAD == null) {
            SIMPLE_PV_THREAD = Executors.newSingleThreadExecutor();
        }
        return SIMPLE_PV_THREAD;
    }
}
