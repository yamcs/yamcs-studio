/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;


/**
 * An object representing the PVReader. It contains all elements that are common
 * to all PVs of all type. The payload is specified by the generic type,
 * and is returned by {@link #getValue()}. Changes in
 * values are notified through the {@link PVReaderListener}. Listeners
 * can be registered from any thread.
 * <p>
 * The implementation is thread safe, meaning any element (e.g. value, connection, exception, ...)
 * can be accessed from any thread,
 * but there is no guarantee on the atomicity. The only way to work on a consistent
 * snapshot is to use a listener and to process the event on the thread
 * of the listener. If the event is forwarded on another thread, one loses
 * the atomicity and also the other safeguards (like rate throttling).
 * The callback does not lock the object, so other threads can still access
 * the object while listeners are executing.
 * <p>
 * Note that <b>you may get one event after pausing or closing the reader</b>, and there
 * is no plan to address this. The event processing starts with the clearing
 * of the queues. From that moment on, the event processing cannot be stopped
 * without dropping data. The pause could arrive at any moment after that, and
 * at that point the framework has only two choices: send the event anyway
 * or dropping the data which would be forever lost. The first choice is preferable.
 * The third way would be to allow a "roll-back" of the event processing to the
 * state before the event processing started: since each element in the event
 * processing may be stateful, this becomes a non-trivial task. It would also not
 * cover a much simpler hole: the processing may be finished and the pause/stop
 * arrive during the use event callback. At that point, the framework has no control, so
 * you would still have a small chance of part of the callback executed after
 * pausing/closing. The solution there would be to lock the pause/close until
 * after all callbacks are done. This, which was the original implementation, has
 * a severe drawback: it exposes the internal locks and, in conjuction with
 * other badly placed user locks, would introduce deadlocks (user lock -&gt; close[internal lock],
 * callback[internal lock] -&gt; user lock). Given that the point
 * of the library is to make it relative simple for beginners to write multi-threaded
 * correct code, the choice is to avoid the chance of deadlock and let the user
 * deal with the extra possible event.
 *
 * @author carcassi
 * @param <T> the type of the PVReader.
 */
public interface PVReader<T> {

    /**
     * Adds a listener to the value.
     * <p>
     * This method is thread-safe.
     *
     * @param listener a new listener
     */
    public void addPVReaderListener(PVReaderListener<? super T> listener);

    /**
     * Adds a listener to the value, which is notified only if the value is
     * of a given type.
     * <p>
     * This method is thread-safe.
     *
     * @param clazz type to filter notifications for
     * @param listener a new listener
     * @deprecated this method was rarely used and the functionalities
     * can be replicated with an ad-hoc listener; with the addition of generic
     * typing in the listener, this pollutes the interface for the remove
     */
    @Deprecated
    public void addPVReaderListener(final Class<?> clazz, final PVReaderListener listener);

    /**
     * Removes a listener to the value.
     * <p>
     * This method is thread-safe.
     *
     * @param listener the old listener
     */
    public void removePVReaderListener(PVReaderListener<? super T> listener);

    /**
     * Returns the name of the PVReader.
     * <p>
     * This method is thread safe.
     *
     * @return the value of name
     */
    public String getName();

    /**
     * Returns the value of the PVReader.
     * <p>
     * This method is thread-safe.
     *
     * @return the value of value
     */
    public T getValue();

    /**
     * De-registers all listeners, stops all notifications and closes all
     * connections from the data sources needed by this. Once the PVReader
     * is closed, it can't be re-opened. Subsequent calls to close do not
     * do anything.
     */
    public void close();

    /**
     * True if no more notifications are going to be sent for this PVReader.
     *
     * @return true if closed
     */
    public boolean isClosed();

    /**
     * Returns the last exception that was generated preparing the value
     * for this PVReader and clears it (subsequent call will return null).
     * <p>
     * This method is thread-safe.
     *
     * @return the last generated exception or null
     */
    public Exception lastException();

    /**
     * Pauses or resumes the reader notifications.
     * <p>
     * Note that since notifications may still be in flight, you may receive
     * notifications after setting the pause state to on. The paused flag on the
     * reader, though, is changed immediately.
     * <p>
     * This method is thread-safe.
     *
     * @param paused whether the reader should be paused or not
     */
    public void setPaused(boolean paused);

    /**
     * Whether the reader is paused. If a reader is paused, all the notifications
     * are skipped. While the channels remains open, and data is still being collected,
     * the computation after the collectors is suspended, which saves computation
     * resources.
     * <p>
     * This method is thread-safe.
     *
     * @return true if it is paused
     */
    public boolean isPaused();

    /**
     * True if the reader is connected. <b>Do not use this method to display connection status
     * if using vTypes defined in org.epics.pvmanager.data.</b>
     * <p>
     * Currently, a reader is defined as connected if <b>all</b> the channels
     * are connected. This means that you still may get updates even if
     * this method returns false. You can use this method to determine whether
     * your notification comes from a complete set.
     * <p>
     * When using <code>VType</code>s, you should use the <code>Alarm</code> interface to
     * get the connection status. This scales when you get aggregates, such
     * as lists or maps of channels. This method does obviously not scale functionally
     * since, in an aggregate, it can't tell you which channel of the set
     * is connected or not.
     * <p>
     * This method is thread-safe.
     *
     * @return true if reader is connected
     */
    public boolean isConnected();
}
