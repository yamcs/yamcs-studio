/********************************************************************************
 * Copyright (c) 2011, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.core.ui.logging;

import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Log handler that displays messages in the Eclipse Console view.
 *
 * <p>
 * The {@link MessageConsoleStream} description mentions buffering and appears thread-safe, but lockups have been
 * observed if multiple threads try to access the console streams. This handler therefore logs in the UI thread, using
 * the {@link Display} that was available on initialization. This is suitable for RCP, but not RAP.
 */
public class ConsoleViewHandler extends Handler {
    /** Flag to prevent multiple instances */
    private static boolean have_console = false;

    /** Display used for performing console access in the UI thread */
    final Display display;

    /**
     * Connection to Console View. Set to <code>null</code> when console support shuts down
     */
    private volatile MessageConsole console;

    /** Printable, color-coded stream of the <code>console</code> */
    final private MessageConsoleStream severe_stream, warning_stream, info_stream, basic_stream;

    /**
     * Add console view to the (root) logger.
     * <p>
     * To be called from Eclipse application's <code>WorkbenchWindowAdvisor.postWindowCreate()</code>. Calling it
     * earlier is not possible because the necessary console view infrastructure is not available, yet.
     * <p>
     * Calling it much later means log messages are lost.
     * <p>
     * Only the first call has an effect. Subsequent calls as they can happen when opening multiple windows of the same
     * Eclipse instance will have no effect.
     * 
     * @return
     */
    public static synchronized ConsoleViewHandler install() {
        if (have_console) {
            return null;
        }

        try {
            var display = Display.getCurrent();
            if (display == null) {
                throw new Exception("No display");
            }

            var handler = new ConsoleViewHandler(display);
            Logger.getLogger("").addHandler(handler);
            Logger.getLogger("").setLevel(Level.INFO);
            have_console = true;
            return handler;
        } catch (Throwable ex) {
            have_console = true;
            Logger.getLogger(ConsoleViewHandler.class.getName()).log(Level.WARNING,
                    "Cannot configure console view: {0}", ex.getMessage());
            return null;
        }
    }

    /**
     * Initialize, hook into console view
     *
     * Private to prevent multiple instances
     * 
     * @see #addToLogger()
     */
    private ConsoleViewHandler(Display display) {
        this.display = display;

        // Allocate a console for text messages.
        console = new MessageConsole("Log Messages", null);
        // Values are from https://bugs.eclipse.org/bugs/show_bug.cgi?id=46871#c5
        console.setWaterMarks(80000, 100000);

        // Add to the 'Console' View
        var consolePlugin = ConsolePlugin.getDefault();
        consolePlugin.getConsoleManager().addConsoles(new IConsole[] { console });

        // Streams to the MessageConsole
        severe_stream = console.newMessageStream();
        warning_stream = console.newMessageStream();
        info_stream = console.newMessageStream();
        basic_stream = console.newMessageStream();

        // Setting the color of a stream while it's in use is hard:
        // Has to happen on SWT UI thread, and changes will randomly
        // affect only the next message or the whole Console View.
        // Using different streams for the color-coded message levels
        // seem to work OK.

        severe_stream.setColor(display.getSystemColor(SWT.COLOR_RED));
        warning_stream.setColor(display.getSystemColor(SWT.COLOR_RED));
        info_stream.setColor(display.getSystemColor(SWT.COLOR_BLACK));
        basic_stream.setColor(display.getSystemColor(SWT.COLOR_BLACK));

        // Suppress log messages when the Eclipse console system shuts down.
        // Unclear how to best do that. Console plugin will 'remove' all consoles on shutdown,
        // so listen for that. But it actually closes the console streams just before that,
        // so a 'publish' call could arrive exactly between the console system shutting down
        // and the 'consolesRemoved' event that notifies us about that fact, resulting
        // in an exception inside 'publish' when it tries to write to the dead console stream.
        consolePlugin.getConsoleManager().addConsoleListener(new IConsoleListener() {
            @Override
            public void consolesAdded(IConsole[] consoles) {
                // NOP
            }

            @Override
            public void consolesRemoved(IConsole[] consoles) {
                // Check if it's this console
                for (IConsole console : consoles) {
                    if (console == ConsoleViewHandler.this.console) { // Mark as closed/detached
                        ConsoleViewHandler.this.console = null;
                        return;
                    }
                }
            }
        });
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }

        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        // Print in UI thread to avoid lockups
        if (display.isDisposed()) {
            return;
        }
        display.asyncExec(() -> {
            try {
                // Console might already be closed/detached
                if (console == null) {
                    return;
                }
                var stream = getStream(record.getLevel());
                if (stream.isClosed()) {
                    return;
                }
                // During shutdown, error is possible because 'document' of console view
                // was already closed. Unclear how to check for that.
                stream.print(msg);
            } catch (Exception ex) {
                reportError(null, ex, ErrorManager.WRITE_FAILURE);
            }
        });
    }

    /**
     * @param level
     *            Message {@link Level}
     * @return Suggested stream for that Level or <code>null</code>
     */
    private MessageConsoleStream getStream(Level level) {
        if (level.intValue() >= Level.SEVERE.intValue()) {
            return severe_stream;
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            return warning_stream;
        } else if (level.intValue() >= Level.INFO.intValue()) {
            return info_stream;
        } else {
            return basic_stream;
        }
    }

    @Override
    public void flush() {
        // Flush in UI thread to avoid lockups
        display.asyncExec(() -> {
            try {
                severe_stream.flush();
                warning_stream.flush();
                info_stream.flush();
                basic_stream.flush();
            } catch (Exception ex) {
                reportError(null, ex, ErrorManager.FLUSH_FAILURE);
            }
        });
    }

    /**
     * Usually called by JRE when Logger shuts down, i.e. way after the Eclipse shutdown has already closed the console
     * view
     */
    @Override
    public void close() throws SecurityException {
        // Mark as detached from console
        var console_copy = console;
        if (console_copy == null) {
            return;
        }
        console = null;
        // Remove from 'Console' view
        console_copy.clearConsole();
        var consolePlugin = ConsolePlugin.getDefault();
        consolePlugin.getConsoleManager().removeConsoles(new IConsole[] { console_copy });
    }
}
