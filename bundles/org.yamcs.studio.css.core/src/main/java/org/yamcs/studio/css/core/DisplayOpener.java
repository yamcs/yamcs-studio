package org.yamcs.studio.css.core;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.csstudio.opibuilder.runmode.OPIRunnerPerspective;
import org.csstudio.opibuilder.runmode.RunModeService;
import org.csstudio.opibuilder.runmode.RunModeService.DisplayMode;
import org.csstudio.opibuilder.runmode.RunnerInput;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.yamcs.protobuf.Yamcs.Event;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.core.model.EventCatalogue;
import org.yamcs.studio.core.model.EventListener;

/*
 * DisplayOpener on request.
 * Request to open a display comes from incoming event.
 * Event to open a display must have a message content on the form:
 * "OpenDisplay: [displayName]"
 * or
 * "OpenDisplay: [displayName], hostname: [hostname]"
 */
public class DisplayOpener implements EventListener {

    private static DisplayOpener instance = new DisplayOpener();
    private static final Logger log = Logger.getLogger(DisplayOpener.class.getName());

    // Pattern p1 = Pattern.compile("^[a-zA-Z]+([0-9]+).*");
    Pattern pHostName = Pattern.compile("OpenDisplay: (.*?), hostname: (.*)");
    Pattern p = Pattern.compile("OpenDisplay: (.*)");

    private DisplayOpener() {
        if (YamcsPlugin.getDefault() != null && EventCatalogue.getInstance() != null) {
            EventCatalogue.getInstance().addEventListener(this);
        }
    }

    public static DisplayOpener init() {
        return instance;
    }

    @Override
    public void processEvent(Event event) {
        // check if this is an OpenDisplay event.
        String eventMessage = event.getMessage();
        if (!eventMessage.startsWith("OpenDisplay: ")) {
            return;
        }

        Matcher mHost = pHostName.matcher(event.getMessage());
        Matcher m = p.matcher(event.getMessage());

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
        String workstationName = Activator.getDefault().getPreferenceStore().getString("events.workstationName");
        if (hostname != null && !hostname.equals(workstationName)) {
            log.fine("OpenDisplay not targetted for this hostname.");
            return;
        }

        // open the requested display
        String finalDisplayName = displayName;
        Display.getDefault().asyncExec(() -> {
            log.info("Opening display " + finalDisplayName);

            IWorkbenchWindow targetWindow = null;
            for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
                if (window.getActivePage().getPerspective().getId().equals(OPIRunnerPerspective.ID)) {
                    targetWindow = window;
                }
            }

            if (targetWindow == null) {
                try {
                    IWorkbenchPage runnerPage = RunModeService.createNewWorkbenchPage(Optional.empty());
                    targetWindow = runnerPage.getWorkbenchWindow();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Failed to launch display runtime", e);
                    return;
                }
            }

            targetWindow.getShell().setActive();

            IPath path = new Path(finalDisplayName);
            RunnerInput new_input = new RunnerInput(path, null);

            RunModeService.openDisplayInView(targetWindow.getActivePage(), new_input, DisplayMode.NEW_TAB);
        });
    }
}
