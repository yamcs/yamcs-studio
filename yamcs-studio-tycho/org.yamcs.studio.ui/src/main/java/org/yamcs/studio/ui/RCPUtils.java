package org.yamcs.studio.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Various 'improvements' to RCP code to shorten our code a bit
 */
public class RCPUtils {

    private RCPUtils() {
    }

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
}
