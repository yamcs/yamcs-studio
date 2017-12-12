package org.yamcs.studio.products.runtime;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class Perspective implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String viewId = "org.yamcs.studio.ui.eventlog.EventLogView";
        layout.setEditorAreaVisible(false);
        layout.setFixed(true);
        layout.addStandaloneView(viewId, false, IPageLayout.TOP, 1, IPageLayout.ID_EDITOR_AREA);
        IViewLayout view = layout.getViewLayout(viewId);
        view.setCloseable(false);
        view.setMoveable(false);
    }
}
