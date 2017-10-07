package org.yamcs.studio.product.utility;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.logging.LogConfigurator;
import org.csstudio.platform.workspace.RelaunchConstants;
import org.csstudio.startup.application.OpenDocumentEventProcessor;
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
 * If StartupParameters#SHARE_LINK_PARAM and ProjectExtPoint#PROJECTS parameters are provided, a
 * link to that shared folder will be created.
 * <p>
 * Uses LoginExtPoint#USERNAME and LoginExtPoint#PASSWORD to attempt authentication.
 * <p>
 * Runs workbench using the {@link ApplicationWorkbenchAdvisor}.
 *
 * @see StartupParameters for startup parameters as well as class loader notes.
 */
public class Workbench implements WorkbenchExtPoint {

    @Override
    public Object afterWorkbenchCreation(Display display, IApplicationContext context, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public Object beforeWorkbenchCreation(Display display, IApplicationContext context, Map<String, Object> parameters) {
        return null;
    }

    /**
     * Creates a workbench advisor to be used by the created workbench.
     *
     * @param parameters
     *            the parameters that may give hints on how to create the advisor
     * @return a new advisor instance
     */
    protected WorkbenchAdvisor createWorkbenchAdvisor(final Map<String, Object> parameters) {
        OpenDocumentEventProcessor openDocProcessor =
                (OpenDocumentEventProcessor) parameters.get(OpenDocumentEventProcessor.OPEN_DOC_PROCESSOR);
        return new YamcsStudioWorkbenchAdvisor(openDocProcessor);
    }

    /** {@inheritDoc} */
    @Override
    public Object runWorkbench(final Display display, final IApplicationContext context,
            final Map<String, Object> parameters)
    {
        // Configure Logging
        try
        {
            LogConfigurator.configureFromPreferences();
        } catch (Exception ex)
        {
            ex.printStackTrace();
            // Continue without customized log configuration
        }
        final Logger logger = Logger.getLogger(getClass().getName());

        //authenticate user
        //    LoginJob.forCurrentUser().schedule();

        // Run the workbench
        final int returnCode = PlatformUI.createAndRunWorkbench(display,
                createWorkbenchAdvisor(parameters));

        // Plain exit from IWorkbench.close()
        if (returnCode != PlatformUI.RETURN_RESTART)
            return IApplication.EXIT_OK;

        // Something called IWorkbench.restart().
        // Is this supposed to be a RESTART or RELAUNCH?
        final Integer exit_code =
                Integer.getInteger(RelaunchConstants.PROP_EXIT_CODE);
        if (IApplication.EXIT_RELAUNCH.equals(exit_code))
        { // RELAUCH with new command line
            logger.log(Level.FINE, "RELAUNCH, command line: {0}", //$NON-NLS-1$
                    System.getProperty(RelaunchConstants.PROP_EXIT_DATA));
            return IApplication.EXIT_RELAUNCH;
        }
        // RESTART without changes
        return IApplication.EXIT_RESTART;
    }
}
