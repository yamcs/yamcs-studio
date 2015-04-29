package org.yamcs.studio.product;

import static org.eclipse.core.runtime.Platform.getBundle;
import static org.eclipse.core.runtime.Platform.getPreferencesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class YamcsPerspective implements IPerspectiveFactory {

    public final static String ID = "org.yamcs.studio.product.YamcsPerspective";

    /** Suffix for matching View IDs when multiple instances are allowed */
    private final static String MULTIPLE = ":*";

    @Override
    public void createInitialLayout(IPageLayout layout) {
        // left | editor
        //      |
        //      |
        //      +-------------
        //      | bottom

        String editor = layout.getEditorArea();
        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.25f, editor);
        IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.66f, editor);

        layout.setEditorAreaVisible(false);

        bottom.addView("org.yamcs.studio.core.commanding.TelecommandView");
        bottom.addView("org.yamcs.studio.core.archive.ArchiveView");
        bottom.addView("org.yamcs.studio.core.eventlog.EventLogView");

        layout.getViewLayout("org.yamcs.studio.core.commanding.TelecommandView").setCloseable(false);
        layout.getViewLayout("org.yamcs.studio.core.archive.ArchiveView").setCloseable(false);
        layout.getViewLayout("org.yamcs.studio.core.eventlog.EventLogView").setCloseable(false);

        for (Entry<String, Integer> entry : findViews().entrySet()) {
            switch (entry.getValue()) {
            case IPageLayout.LEFT:
                left.addPlaceholder(entry.getKey());
                break;
            case IPageLayout.BOTTOM:
                bottom.addPlaceholder(entry.getKey());
                break;
            default:
                break;
            }
        }

        // Populate the "Window > Open Perspective" menu
        layout.addPerspectiveShortcut(ID);
        layout.addPerspectiveShortcut("org.csstudio.opibuilder.opieditor");
        //layout.addPerspectiveShortcut("org.csstudio.opibuilder.OPIRunner");

        // Populate the "Window > Show View" menu
        layout.addShowViewShortcut("org.csstudio.diag.pvmanager.probe");
    }

    /**
     * Searches for views that require placeholders added to the CS-Studio perspective Key is the
     * viewId and the value is the IPageLayout location
     */
    private Map<String, Integer> findViews() {
        Map<String, Integer> viewPlaceholderMap = new HashMap<String, Integer>();

        // Defaults
        viewPlaceholderMap.put(IPageLayout.ID_PROGRESS_VIEW, IPageLayout.BOTTOM);

        // Views from preferences
        String csStudioPerspectivePreference = getPreferencesService()
                .getString("org.csstudio.utility.product",
                        "cs_studio_perspective",
                        "",
                        null);
        for (String viewPlaceholderInfoPref : Arrays.asList(csStudioPerspectivePreference.split(";"))) {
            String[] viewPlaceholderInfo = viewPlaceholderInfoPref.split(":");
            if (viewPlaceholderInfo.length == 4) {
                if (isPluginAvailable(viewPlaceholderInfo[0].trim())) {
                    int location;
                    switch (viewPlaceholderInfo[2].trim()) {
                    case "left":
                        location = IPageLayout.LEFT;
                        break;
                    case "bottom":
                        location = IPageLayout.BOTTOM;
                        break;
                    case "right":
                        location = IPageLayout.RIGHT;
                        break;
                    default:
                        location = IPageLayout.BOTTOM;
                        break;
                    }

                    if (viewPlaceholderInfo[3].trim().equalsIgnoreCase("multiple")) {
                        viewPlaceholderMap.put(viewPlaceholderInfo[0].trim(), location);
                        viewPlaceholderMap.put(viewPlaceholderInfo[0].trim() + MULTIPLE, location);
                    } else {
                        viewPlaceholderMap.put(viewPlaceholderInfo[0].trim(), location);
                    }
                }
            } else {
                // syntax error in preference describing view placeholder
            }
        }
        ;
        return viewPlaceholderMap;
    }

    /**
     * Get a list of Ids of the perspectives to be added to the open perspective shortcut
     */
    private List<String> getPerspectiveShortcutIds() {
        List<String> perspectiveIds = new ArrayList<String>();
        String[] perspectiveShortcut = getPreferencesService()
                .getString("org.csstudio.utility.product",
                        "perspective_shortcut",
                        "",
                        null).split(";");
        for (String perspectiveInfoPref : Arrays.asList(perspectiveShortcut)) {
            String[] perspectiveInfo = perspectiveInfoPref.split(":");
            if (perspectiveInfo.length == 2) {
                if (isPluginAvailable(perspectiveInfo[0].trim())) {
                    perspectiveIds.add(perspectiveInfo[1].trim());
                }
            }
        }
        return perspectiveIds;
    }

    private boolean isPluginAvailable(final String plugin_id) {
        return getBundle(plugin_id) != null;
    }
}
