/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.ui.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.widgets.Widget;

/**
 * A utility class to throttle actions caused by user interaction.
 * <p>
 * In some cases, user interaction may trigger actions that require some time to complete. Multiple interactions will
 * need to cancel the previous submitted task in favor of the latest one. This class provides an easy implementation for
 * that case.
 *
 */
public class DelayedNotificator {

    private final long delay;
    private final TimeUnit unit;
    private ScheduledFuture<?> future;

    /**
     * Creates a new notificator, with the delay given.
     * <p>
     * Only one task per notificator can be queue at a time.
     *
     * @param delay
     *            the delay
     * @param unit
     *            the unit of the delay
     */
    public DelayedNotificator(long delay, TimeUnit unit) {
        this.delay = delay;
        this.unit = unit;
    }

    public static ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    /**
     * Submits a command for the given widget. After the timeout, the command is executed on the display thread of the
     * widget. If another command is submitted before the timeout, than the previous action is cancelled.
     *
     * @param widget
     *            the display thread of this widget will be used
     * @param command
     *            the command to execute
     */
    public void delayedExec(Widget widget, Runnable command) {
        if (future != null) {
            if (!future.isDone()) {
                future.cancel(false);
            }
            future = null;
        }

        future = exec.schedule(() -> widget.getDisplay().asyncExec(command), delay, unit);
    }
}
