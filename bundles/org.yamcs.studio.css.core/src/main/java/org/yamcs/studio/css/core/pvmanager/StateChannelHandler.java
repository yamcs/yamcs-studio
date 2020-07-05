package org.yamcs.studio.css.core.pvmanager;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.diirt.datasource.ChannelWriteCallback;
import org.diirt.datasource.MultiplexedChannelHandler;

public abstract class StateChannelHandler extends MultiplexedChannelHandler<Object, Object> {

    private final Runnable task = new Runnable() {

        @Override
        public void run() {
            // Protect the timer thread for possible problems.
            try {
                Object newValue = createValue();

                if (newValue != null) {
                    processMessage(newValue);
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Data simulation problem", ex);
            }
        }
    };
    private static final Logger log = Logger.getLogger(StateChannelHandler.class.getName());
    private ScheduledFuture<?> taskFuture;

    protected abstract Object createValue();

    public StateChannelHandler(String channelName) {
        super(channelName);
    }

    @Override
    public void connect() {
        taskFuture = StateDataSource.getScheduledExecutorService().scheduleWithFixedDelay(task, 0, 1,
                TimeUnit.SECONDS);
        processConnection(new Object());
    }

    @Override
    public void disconnect() {
        taskFuture.cancel(false);
        taskFuture = null;
        processConnection(null);
    }

    @Override
    public void write(Object newValue, ChannelWriteCallback callback) {
        throw new UnsupportedOperationException("Can't write to state channel.");
    }

    @Override
    public boolean isConnected(Object connection) {
        return taskFuture != null;
    }

    @Override
    protected boolean saveMessageAfterDisconnect() {
        return true;
    }
}
