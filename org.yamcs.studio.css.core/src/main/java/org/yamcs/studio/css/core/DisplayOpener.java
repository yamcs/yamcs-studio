/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.css.core;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.csstudio.opibuilder.runmode.OPIRunnerPerspective;
import org.csstudio.opibuilder.runmode.RunModeService;
import org.csstudio.opibuilder.runmode.RunModeService.DisplayMode;
import org.csstudio.opibuilder.runmode.RunnerInput;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Yamcs.Event;

/*
 * DisplayOpener on request.
 * Request to open a display comes from incoming event.
 * Event to open a display must have a message content on the form:
 * "OpenDisplay: [displayName]"
 * or
 * "OpenDisplay: [displayName], hostname: [hostname]"
 */
public class DisplayOpener {

    private static DisplayOpener instance = new DisplayOpener();
    private static final Logger log = Logger.getLogger(DisplayOpener.class.getName());

    // Pattern p1 = Pattern.compile("^[a-zA-Z]+([0-9]+).*");
    Pattern pHostName = Pattern.compile("OpenDisplay: (.*?), hostname: (.*)");
    Pattern p = Pattern.compile("OpenDisplay: (.*)");

    public static DisplayOpener init() {
        return instance;
    }

    public void processEvent(Event event) {
        // check if this is an OpenDisplay event.
        var eventMessage = event.getMessage();
        if (!eventMessage.startsWith("OpenDisplay: ")) {
            return;
        }

        var mHost = pHostName.matcher(event.getMessage());
        var m = p.matcher(event.getMessage());

        String displayName = null;
        String hostname = null;

        if (mHost.find()) {
            displayName = mHost.group(1);
            hostname = mHost.group(2);
        } else if (m.find()) {
            displayName = m.group(1);
        } else {
            log.warning("Malformed OpenDisplay event received.");
            return;
        }

        // check if hostname matches the event request
        var workstationName = Activator.getDefault().getPreferenceStore().getString("events.workstationName");
        if (hostname != null && !hostname.equals(workstationName)) {
            log.fine("OpenDisplay not targetted for this hostname.");
            return;
        }

        // open the requested display
        var finalDisplayName = displayName;
        Display.getDefault().asyncExec(() -> {
            log.info("Opening display " + finalDisplayName);

            IWorkbenchWindow targetWindow = null;
            for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                if (window.getActivePage().getPerspective().getId().equals(OPIRunnerPerspective.ID)) {
                    targetWindow = window;
                }
            }

            if (targetWindow == null) {
                try {
                    var runnerPage = RunModeService.createNewWorkbenchPage(Optional.empty());
                    targetWindow = runnerPage.getWorkbenchWindow();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Failed to launch display runtime", e);
                    return;
                }
            }

            targetWindow.getShell().setActive();

            var path = new Path(finalDisplayName);
            var new_input = new RunnerInput(path, null);

            RunModeService.openDisplayInView(targetWindow.getActivePage(), new_input, DisplayMode.NEW_TAB);
        });
    }
}
