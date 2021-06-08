package org.yamcs.studio.archive;

import javax.swing.SwingUtilities;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handles the enabled state for the zoom-clear command
 */
public class ZoomClearHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
        SwingUtilities.invokeLater(() -> {
            ArchiveView view = (ArchiveView) part;
            view.archivePanel.getDataViewer().clearZoom();
        });

        return null;
    }
}
