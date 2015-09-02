package org.yamcs.studio.ui;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Various 'improvements' to RCP code to shorten our code a bit
 */
public class RCPUtils {

    private static final Logger log = Logger.getLogger(RCPUtils.class.getName());

    /**
     * Finds a source provider within the active workbench for the execution event
     */
    public static <T> T findSourceProvider(ExecutionEvent evt, String sourceName, Class<T> expectedClass) {
        IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(evt);
        return findSourceProvider(workbenchWindow, sourceName, expectedClass);
    }

    /**
     * Finds a source provider using the specified locator (for example, a workbench window)
     */
    @SuppressWarnings("unchecked")
    public static <T> T findSourceProvider(IServiceLocator locator, String sourceName, Class<T> expectedClass) {
        ISourceProviderService service = (ISourceProviderService) locator
                .getService(ISourceProviderService.class);
        return (T) service.getSourceProvider(sourceName);
    }

    public static void runCommand(String commandId) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
        IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
        try {
            Command cmd = commandService.getCommand(commandId);
            cmd.executeWithChecks(new ExecutionEvent(cmd, new HashMap<String, String>(), null, evaluationService.getCurrentState()));
        } catch (Exception exception) {
            log.log(Level.SEVERE, "Could not execute command", exception);
        }
    }
}
