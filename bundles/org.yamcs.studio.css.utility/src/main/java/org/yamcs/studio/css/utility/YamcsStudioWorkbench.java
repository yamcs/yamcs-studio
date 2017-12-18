package org.yamcs.studio.css.utility;

import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.platform.workspace.RelaunchConstants;
import org.csstudio.startup.module.WorkbenchExtPoint;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.yamcs.CompactFormatter;

public class YamcsStudioWorkbench implements WorkbenchExtPoint {

    @Override
    public Object afterWorkbenchCreation(Display display, IApplicationContext context, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public Object beforeWorkbenchCreation(Display display, IApplicationContext context,
            Map<String, Object> parameters) {
        return null;
    }

    protected WorkbenchAdvisor createWorkbenchAdvisor(Map<String, Object> parameters) {
        return new YamcsStudioWorkbenchAdvisor();
    }

    @Override
    public Object runWorkbench(Display display, IApplicationContext context, Map<String, Object> parameters) {
        configureLogging();

        Logger log = Logger.getLogger(getClass().getName());

        // Run the workbench
        int returnCode = PlatformUI.createAndRunWorkbench(display, createWorkbenchAdvisor(parameters));

        // Plain exit from IWorkbench.close()
        if (returnCode != PlatformUI.RETURN_RESTART) {
            return IApplication.EXIT_OK;
        }

        // IWorkbench.restart() was called.
        Integer exitCode = Integer.getInteger(RelaunchConstants.PROP_EXIT_CODE);
        if (IApplication.EXIT_RELAUNCH.equals(exitCode)) { // RELAUCH with new command line
            log.fine(String.format("RELAUNCH, command line: %s",
                    System.getProperty(RelaunchConstants.PROP_EXIT_DATA)));
            return IApplication.EXIT_RELAUNCH;
        }
        // RESTART without changes
        return IApplication.EXIT_RESTART;
    }

    protected void configureLogging() {
        Logger root = Logger.getLogger("");

        // We use the convention where INFO goes to end-user (via 'Console View' inside Yamcs Studio)
        // And FINE goes to stdout (--> debuggable by end-user if needed, and visible in PDE/UI)

        // By default only allow WARNING messages
        root.setLevel(Level.WARNING);

        // Exceptions only for plugins that do not flood the Console View with INFO messages:
        Logger.getLogger("org.csstudio").setLevel(Level.FINE);
        Logger.getLogger("org.yamcs.studio").setLevel(Level.FINE);

        // At this point in the startup there should be only one handler (for stdout)
        for (Handler handler : root.getHandlers()) {
            handler.setLevel(Level.FINE);
            handler.setFormatter(new CompactFormatter());
        }

        // A second handler will be created by the workbench window advisor when the ConsoleView
        // is available.
    }
}
