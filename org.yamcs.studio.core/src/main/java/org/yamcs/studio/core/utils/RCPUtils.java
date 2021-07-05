package org.yamcs.studio.core.utils;

import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.yamcs.studio.core.YamcsPlugin;

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
        ISourceProviderService service = (ISourceProviderService) locator.getService(ISourceProviderService.class);
        return (T) service.getSourceProvider(sourceName);
    }

    public static void runCommand(String commandId) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
        IEvaluationService evaluationService = (IEvaluationService) window.getService(IEvaluationService.class);
        try {
            Command cmd = commandService.getCommand(commandId);
            if (cmd.isEnabled()) {
                cmd.executeWithChecks(
                        new ExecutionEvent(cmd, new HashMap<String, String>(), null,
                                evaluationService.getCurrentState()));
            }
        } catch (Exception exception) {
            log.log(Level.SEVERE, "Could not execute command " + commandId, exception);
        }
    }

    /**
     * Sets a message in the lower left status line. These messages are by rcp-design associated with a view.
     *
     * @param viewId
     *            the view from which the message originates
     */
    public static void setStatusMessage(String viewId, String message) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IViewSite site = window.getActivePage().findView(viewId).getViewSite();
        IStatusLineManager mgr = site.getActionBars().getStatusLineManager();
        mgr.setMessage(message);
    }

    /**
     * Sets an error message in the lower left status line. These messages are by rcp-design associated with a view.
     *
     * @param viewId
     *            the view from which the message originates
     */
    public static void setStatusErrorMessage(String viewId, String message) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IViewSite site = window.getActivePage().findView(viewId).getViewSite();
        IStatusLineManager mgr = site.getActionBars().getStatusLineManager();
        mgr.setErrorMessage(message);
    }

    public static ImageDescriptor getImageDescriptor(Class<?> classFromBundle, String path) {
        Bundle bundle = FrameworkUtil.getBundle(classFromBundle);
        return ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path(path), null));
    }

    public static Instant toInstant(DateTime dateWidget, DateTime timeWidget) {
        Calendar cal = Calendar.getInstance(YamcsPlugin.getTimeZone());
        cal.set(dateWidget.getYear(), dateWidget.getMonth(), dateWidget.getDay());
        cal.set(Calendar.HOUR_OF_DAY, timeWidget.getHours());
        cal.set(Calendar.MINUTE, timeWidget.getMinutes());
        cal.set(Calendar.SECOND, timeWidget.getSeconds());
        cal.set(Calendar.MILLISECOND, 0);
        return cal.toInstant();
    }

    public static void monitorCancellableFuture(IProgressMonitor monitor, Future<?> future)
            throws InterruptedException, ExecutionException {
        while (!monitor.isCanceled() && !future.isDone()) {
            try {
                future.get(200, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // Keep trying until cancelled or done
            }
        }

        if (monitor.isCanceled()) {
            future.cancel(true);
        }
    }
}
