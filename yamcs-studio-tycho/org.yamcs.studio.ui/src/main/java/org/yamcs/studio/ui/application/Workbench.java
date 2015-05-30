package org.yamcs.studio.ui.application;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.logging.LogConfigurator;
import org.csstudio.platform.workspace.RelaunchConstants;
import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class Workbench extends org.csstudio.utility.product.Workbench {

    @Override
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
