package org.yamcs.studio.css.utility;

import java.util.Map;
import java.util.logging.Logger;

import org.csstudio.logging.LogConfigurator;
import org.csstudio.platform.workspace.RelaunchConstants;
import org.csstudio.startup.module.WorkbenchExtPoint;
import org.csstudio.utility.product.ApplicationWorkbenchAdvisor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * Forked from org.csstudio.utility.product.Workbench
 * <p>
 * If StartupParameters#SHARE_LINK_PARAM and ProjectExtPoint#PROJECTS parameters are provided, a link to that shared
 * folder will be created.
 * <p>
 * Uses LoginExtPoint#USERNAME and LoginExtPoint#PASSWORD to attempt authentication.
 * <p>
 * Runs workbench using the {@link ApplicationWorkbenchAdvisor}.
 *
 * @see YamcsStudioStartupParameters for startup parameters as well as class loader notes.
 */
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
        // Configure Logging
        try {
            LogConfigurator.configureFromPreferences();
        } catch (Exception e) {
            e.printStackTrace();
            // Continue without customized log configuration
        }
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
}
