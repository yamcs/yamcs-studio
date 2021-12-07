/********************************************************************************
 * Copyright (c) 2008 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.csstudio.java.string.StringSplitter;
import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.eclipse.osgi.util.NLS;

/**
 * Helper for executing a (system) command. On Unix, that could be anything in the PATH.
 * <p>
 * Several things can happen:
 * <ul>
 * <li>Command finishes OK right away
 * <li>Command gives error right away
 * <li>Command runs for a long time, eventually giving error or OK.
 * </ul>
 * The command executor waits a little time to see if the command finishes, and calls back in case of an error. When the
 * command finishes right away OK or runs longer, we leave it be.
 */
public final class CommandExecutor {
    final private String dir_name;
    final private String command;
    final private int wait;

    /**
     * Initialize
     * 
     * @param command
     *            Command to run. Format depends on OS.
     * @param dir_name
     *            Directory where to run the command
     * @param wait
     *            Time to wait for completion in seconds
     */
    public CommandExecutor(String command, String dir_name, int wait) {
        this.command = command;
        this.dir_name = dir_name;
        this.wait = wait;
        var t = new Thread((Runnable) () -> runAndCheckCommand(), "CommandExecutor");
        t.start();
    }

    private void runAndCheckCommand() {
        // Execute command in a certain directory
        var dir = new File(dir_name);
        Process process;
        try {
            var cmd = StringSplitter.splitIgnoreInQuotes(command, ' ', true);
            process = Runtime.getRuntime().exec(cmd, null, dir);
        } catch (Throwable ex) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, ex.getMessage());
            OPIBuilderPlugin.getLogger().log(Level.INFO,
                    NLS.bind("Command \"{0}\" finished with exit code: FAILED", command));

            return;
        }

        // create a thread for listening on error output
        var errorThread = new Thread(() -> {
            // .. with error; check error output
            try (var br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    OPIBuilderPlugin.getLogger().log(Level.SEVERE, command + " error: " + line);
                }
            } catch (IOException e1) {
                ErrorHandlerUtil.handleError("Command error", e1);
                return;
            }
        });
        errorThread.start();

        // write output to console
        var inputThread = new Thread(() -> {
            try (var br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    OPIBuilderPlugin.getLogger().log(Level.INFO, line);
                }
            } catch (IOException e1) {
                ErrorHandlerUtil.handleError("Command error", e1);
                return;
            }
        });
        inputThread.start();

        // Poll exit code during 'wait' time
        Integer exit_code = null;
        for (var w = 0; w < wait; ++w) {

            try {
                exit_code = process.exitValue();
                break;
            } catch (IllegalThreadStateException ex) { // still running...
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) { // Ignore
                }
            }
        }

        OPIBuilderPlugin.getLogger().log(Level.INFO, NLS.bind("Command \"{0}\" finished with exit code: ", command)
                + (exit_code == null ? "NULL" : (exit_code == 0 ? "OK" : "FAILED")));
        // Process runs so long that we no longer care
        if (exit_code == null) {
            return;
        }

        // Process ended
        if (exit_code == 0) {
            return;
        }
    }
}
