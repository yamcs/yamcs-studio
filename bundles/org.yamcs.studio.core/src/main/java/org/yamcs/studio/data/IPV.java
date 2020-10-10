package org.yamcs.studio.data;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yamcs.studio.data.vtype.VType;

public class IPV {

    private static final Logger log = Logger.getLogger(IPV.class.getName());
    private static final AtomicLong SEQ = new AtomicLong();

    private final long id;
    private final String name;
    private final Executor notificationThread;

    private AtomicBoolean started = new AtomicBoolean(false); // start() has been called (fully executed or not)
    private AtomicBoolean starting = new AtomicBoolean(false); // PV is during start

    // Datasources can use this to force a PV as disconnected
    private boolean invalid = false;

    private Datasource datasource;

    private List<IPVListener> listeners = new CopyOnWriteArrayList<>();

    IPV(String name, Datasource datasource, Executor notificationThread) {
        id = SEQ.getAndIncrement();
        this.name = Objects.requireNonNull(name);
        this.datasource = Objects.requireNonNull(datasource);
        this.notificationThread = Objects.requireNonNull(notificationThread);
        log.fine(String.format("Creating PV %s", this));
    }

    /**
     * Add a listener to the PV, which will be notified on events of the PV in the given notify thread.
     */
    public void addListener(IPVListener listener) {
        listeners.add(listener);
        notificationThread.execute(() -> {
            if (isConnected()) {
                listener.connectionChanged(this);
                if (getValue() != null) {
                    listener.valueChanged(this);
                }
            }
        });
    }

    public void removeListener(IPVListener listener) {
        listeners.remove(listener);
    }

    public String getName() {
        return name;
    }

    /**
     * Get the most recent value of the PV in last update cycle that has values. {@link VTypeHelper} and ValueUtil can
     * be used to get the number or string value, alarm, display, time stamp etc. from the {@link VType} value and help
     * to format the value.
     *
     * @return value of the PV. Will be null if the PV is not started or connected. It can also be null even the PV is
     *         connected. For example, the value is not a VType, not prepared yet or it has null as the initial value.
     */
    public VType getValue() {
        return datasource.getValue(this);
    }

    /**
     * Returns the 'connection' state of an individual PV. If the PV is an aggregate of multiple PVs, the connection
     * state should be determined by the aggregator. For example, the aggregator countConnected(pv1, pv2, pv3,) should
     * always return connected.
     */
    public boolean isConnected() {
        return !invalid && datasource.isConnected(this);
    }

    public void notifyConnectionChange() {
        listeners.forEach(l -> l.connectionChanged(this));
    }

    public void notifyValueChange() {
        if (getValue() != null) {
            listeners.forEach(l -> l.valueChanged(this));
        }
    }

    public void notifyWritePermissionChange() {
        listeners.forEach(l -> l.writePermissionChanged(this));
    }

    /**
     * If the {@link #start()} has been called but {@link #stop()} has not been called. This method tells nothing if the
     * pv is connected. To see if the PV is connected use {@link #isConnected()}.
     *
     * @return true if the pv is started but not stopped.
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     * @return <code>true</code> if the PV is connected and allowed to write.
     */
    public boolean isWriteAllowed() {
        return datasource.isWriteAllowed(this);
    }

    /**
     * Set PV to a given value asynchronously. It will return immediately. Should accept number, number array,
     * <code>String</code>, maybe more.
     */
    public void setValue(Object value) {
        datasource.writeValue(this, value, err -> {
            if (err != null) {
                log.log(Level.SEVERE, "Failed to update value", err);
            }
        });
    }

    public void setInvalid() {
        invalid = true;
        listeners.forEach(l -> l.connectionChanged(this));
    }

    /**
     * Set PV to a given value synchronously. It will block the current thread until write operation was submitted or
     * timeout. Note that this call will only issue the write request, it will not await a put-callback to indicate
     * complete processing of a write on the server.
     *
     * @param value
     *            Value to write to the PV It is not necessary to call {@link #start()} before calling this method,
     *            because it will handle the connection with timeout in this method. Should accept number, number array,
     *            <code>String</code>, maybe more.
     * @param timeout
     *            timeout in millisecond for both pv connection and write operation, so in very rare case, it could take
     *            maximum 2*timeout ms for the timeout.
     * @return true if write successful or false otherwise.
     */
    public boolean setValue(Object value, int timeout) throws Exception {
        AtomicBoolean result = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        datasource.writeValue(this, value, err -> {
            if (err != null) {
                log.log(Level.SEVERE, "Failed to update value", err);
            }
            result.set(err == null);
            latch.countDown();
        });
        if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
            throw new Exception(String.format("Timeout while writing PV value", timeout));
        }
        return result.get();
    }

    /**
     * Start to connect and listen on the PV. To start an already started PV will get an {@link IllegalStateException}.
     */
    public void start() {
        log.fine(String.format("Starting PV %s", this));
        if (!started.getAndSet(true)) {
            starting.set(true);
            // On same thread as where updates are handled (avoid missing events)
            notificationThread.execute(() -> {
                try {
                    datasource.onStarted(this);
                } finally { // Protect against onStarted throwing an exception
                    starting.set(false);
                }
                log.fine(String.format("Start finished for PV %s", this));
            });
        } else {
            throw new IllegalStateException(String.format("PV %s has already been started.", this));
        }
    }

    /**
     * Close the connection while keeping all listeners, so when it is restarted, it will work as before. When the PV is
     * no longer needed, one should stop it to release resources. To stop an already stopped PV or not started PV will
     * do nothing but log a warning message.
     */
    public void stop() {
        log.fine(String.format("Stopping PV %s", this));
        if (!started.get()) {
            log.warning(String.format("PV %s has already been stopped or was not started yet", this));
            return;
        }
        if (starting.get()) { // Start-up is on notification thread. Stop it there as soon as it finishes.
            notificationThread.execute(this::stop);
            return;
        }
        started.set(false);
        datasource.onStopped(this);
    }

    @Override
    public String toString() {
        return name + " (#" + id + ")";
    }
}
