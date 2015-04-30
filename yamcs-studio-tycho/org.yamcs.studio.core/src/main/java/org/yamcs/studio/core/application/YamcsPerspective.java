package org.yamcs.studio.core.application;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class YamcsPerspective implements IPerspectiveFactory {

    public static final String ID = "org.yamcs.studio.core.application.YamcsPerspective";

    private static final String EVENT_LOG_VIEW_ID = "org.yamcs.studio.core.eventlog.EventLogView";
    private static final String ARCHIVE_VIEW_ID = "org.yamcs.studio.core.archive.ArchiveView";
    private static final String TELECOMMAND_VIEW_ID = "org.yamcs.studio.core.commanding.TelecommandView";

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editor = layout.getEditorArea();

        layout.setEditorAreaVisible(false);

        layout.addView(EVENT_LOG_VIEW_ID, IPageLayout.RIGHT, 0.66f, editor);

        IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.50f, EVENT_LOG_VIEW_ID);
        topLeft.addView(ARCHIVE_VIEW_ID);

        IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f, ARCHIVE_VIEW_ID);
        bottomLeft.addView(TELECOMMAND_VIEW_ID);

        layout.getViewLayout(TELECOMMAND_VIEW_ID).setCloseable(false);
        layout.getViewLayout(ARCHIVE_VIEW_ID).setCloseable(false);
        layout.getViewLayout(EVENT_LOG_VIEW_ID).setCloseable(false);

        // Populate the "Window > Show View" menu
        // (in case the user succeeded in closing our uncloseable views (by first detaching them)
        layout.addShowViewShortcut(TELECOMMAND_VIEW_ID);
        layout.addShowViewShortcut(ARCHIVE_VIEW_ID);
        layout.addShowViewShortcut(EVENT_LOG_VIEW_ID);
    }
}
