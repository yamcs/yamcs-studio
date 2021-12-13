/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util.thread;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 *
 * A singleton back thread which will help to execute tasks in UI thread. This way we avoid slow downs, that occur on
 * several operating systems, when Display.asyncExec() is called very often from background threads.
 *
 * This thread sleeps for a time, which is below the processing capacity of human eyes and brain - so the user will not
 * feel any delay.
 */
public final class UIBundlingThread implements Runnable {
    /**
     * The singleton instance.
     */
    private static UIBundlingThread instance;

    /**
     * A queue, which contains runnables that process the events that occured during the last SLEEP_TIME milliseconds.
     */
    private Queue<Runnable> tasksQueue;

    private Display display;

    /**
     * Standard constructor.
     */
    private UIBundlingThread() {
        tasksQueue = new ConcurrentLinkedQueue<Runnable>();
        display = Display.getCurrent();
        if (display == null) {
            if (PlatformUI.getWorkbench() != null) {
                display = PlatformUI.getWorkbench().getDisplay();
            } else {
                display = Display.getDefault();
            }
        }
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this, 100, 20, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the singleton instance
     */
    public static synchronized UIBundlingThread getInstance() {
        if (instance == null) {
            instance = new UIBundlingThread();
        }

        return instance;
    }

    @Override
    public void run() {
        if (!tasksQueue.isEmpty()) {
            processQueue();
        }
    }

    /**
     * Process the complete queue.
     */
    private synchronized void processQueue() {
        Runnable r;
        while ((r = tasksQueue.poll()) != null) {
            display.asyncExec(r);
        }
    }

    /**
     * Adds the specified runnable to the queue. Should not be used for RAP.
     *
     * @param runnable
     *            the runnable
     */
    public synchronized void addRunnable(Runnable runnable) {
        tasksQueue.add(runnable);
    }

    /**
     * Adds the specified runnable to the queue. Fake method for adapting RAP.
     *
     * @param runnable
     *            the runnable
     */
    public synchronized void addRunnable(Display display, Runnable runnable) {
        addRunnable(runnable);
    }
}
