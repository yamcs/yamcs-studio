/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.csstudio.java.thread.ExecutionService;

/**
 * A customized timer for the internal use of OPI builder. It will execute a task after certain delay. The timer can be
 * reseted or stopped during the delay period. The timer will stop and dispose automatically when it is due.
 *
 * <pre>
 *   |------------|---------------|
 * Start      Due/start task  task done
 * </pre>
 */
public class OPITimer {

    private Runnable task;

    private long delay;

    private boolean due = true;

    private ScheduledFuture<?> dueTaskFuture, scheduledTaskFuture;

    private final Runnable dueTask = () -> due = true;

    /**
     * Schedules the specified task for execution after the specified delay.
     *
     * @param task
     *            task to be scheduled.
     * @param delay
     *            delay in milliseconds before task is to be executed.
     * @throws IllegalArgumentException
     *             if {@code delay} is negative, or {@code delay + System.currentTimeMillis()} is negative.
     * @throws IllegalStateException
     *             if task was already scheduled or canceled, or timer was canceled.
     */
    public synchronized void start(Runnable task, long delay) {
        this.delay = delay;
        this.task = task;
        if (!due) {
            stop();
        }

        // mark it as due before task started
        dueTaskFuture = ExecutionService.getInstance().getScheduledExecutorService().schedule(dueTask, delay - 1,
                TimeUnit.MILLISECONDS);

        // start task
        scheduledTaskFuture = ExecutionService.getInstance().getScheduledExecutorService().schedule(task, delay,
                TimeUnit.MILLISECONDS);

        due = false;
    }

    /**
     * Reset the timer to start from zero again.
     */
    public synchronized void reset() {
        if (!due) {
            start(task, delay);
        }
    }

    /**
     * @return true if timer is due
     */
    public synchronized boolean isDue() {
        return due;
    }

    /**
     * Stop the timer. Cancel the scheduled task.
     */
    public synchronized void stop() {
        if (dueTaskFuture != null) {
            dueTaskFuture.cancel(false);
        }
        if (scheduledTaskFuture != null) {
            scheduledTaskFuture.cancel(false);
        }
        due = true;
    }
}
