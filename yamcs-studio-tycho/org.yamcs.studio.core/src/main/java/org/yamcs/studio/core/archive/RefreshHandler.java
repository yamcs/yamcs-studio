package org.yamcs.studio.core.archive;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Handels the refresh of the archive view. The most tricky thing here is that we need to disable
 * the refresh button until it's actually refreshed, to prevent a double click from the user, which
 * would otherwise break our hornetq client.
 */
public class RefreshHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Retrieve our custom state variable that was declared in plugin.xml
        ISourceProviderService sourceProviderService = (ISourceProviderService) HandlerUtil
                .getActiveWorkbenchWindow(event).getService(ISourceProviderService.class);
        RefreshCommandState commandState = (RefreshCommandState) sourceProviderService
                .getSourceProvider(RefreshCommandState.STATE_KEY_ENABLED);

        // After that intermezzo.... deactivate this command
        commandState.setEnabled(false);

        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);

        // We trust on the ArchiveView to re-enable the commandState (on the swt-thread) once it's done
        SwingUtilities.invokeLater(() -> ((ArchiveView) part).refresh());

        return null;
    }
}
