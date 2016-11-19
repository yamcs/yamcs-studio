package org.yamcs.studio.core.ui;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
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
        if (YamcsPlugin.getDefault() != null && EventCatalogue.getInstance() != null)
            EventCatalogue.getInstance().addEventListener(this);

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
        String workstationName = YamcsUIPlugin.getDefault().getPreferenceStore().getString("events.workstationName");
        if (hostname != null && !hostname.equals(workstationName)) {
            log.fine("OpenDisplay not targetted for this hostname.");
            return;
        }

        final String displayFName = displayName;

        // open the requested display
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                log.info("Opening display " + displayFName);
                IWorkbench wb = PlatformUI.getWorkbench();
                IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();

                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(displayFName));
                try {
                    IDE.openEditor(page, file, true);
                } catch (PartInitException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
    }

}
