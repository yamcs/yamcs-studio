package org.yamcs.studio.runtime.base;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class RuntimePerspective implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout pageLayout) {
        String viewId = "org.yamcs.studio.runtime.RuntimeView";
        pageLayout.setEditorAreaVisible(false);
        pageLayout.setFixed(true);
        pageLayout.addStandaloneView(viewId, false, IPageLayout.TOP, 1, IPageLayout.ID_EDITOR_AREA);

        IViewLayout view = pageLayout.getViewLayout(viewId);
        view.setCloseable(false);
        view.setMoveable(false);
    }
}
