package org.yamcs.studio.ui.archive;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.yamcs.studio.core.ui.utils.RCPUtils;

/**
 * Handles the refresh of the archive view
 */
public class RefreshArchiveHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Disable refresh button, to prevent double-click from the user, which might
        // currently break our hornetq client
        RefreshStateProvider provider = RCPUtils.findSourceProvider(event,
                RefreshStateProvider.STATE_KEY_ENABLED, RefreshStateProvider.class);
        provider.setEnabled(false);

        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);

        // We trust on the ArchiveView to re-enable the commandState (on the swt-thread) once it's done
        SwingUtilities.invokeLater(() -> ((ArchiveView) part).refresh());

        return null;
    }
}
