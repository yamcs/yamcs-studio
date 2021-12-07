package org.csstudio.opibuilder.editor;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class OPIEditorPerspective implements IPerspectiveFactory {

    public static final String ID = "org.csstudio.opibuilder.opieditor";

    private static final String ID_LEFT_BOTTOM = "leftBottom";
    private static final String ID_BOTTOM = "bottom";
    private static final String ID_RIGHT = "right";
    private static final String ID_LEFT = "left";
    private static final String ID_CONSOLE_VIEW = "org.eclipse.ui.console.ConsoleView";
    private static final String ID_HELP_VIEW = "org.eclipse.help.ui.HelpView";

    private static final String ID_FUNCTIONS_VIEW = "org.yamcs.studio.editor.base.views.FunctionsView";
    private static final String ID_PARAMETERS_VIEW = "org.yamcs.studio.editor.base.views.ParametersView";
    private static final String ID_EXPLORER = "org.yamcs.studio.explorer.view";

    @Override
    public void createInitialLayout(IPageLayout layout) {
        var editor = layout.getEditorArea();

        var left = layout.createFolder(ID_LEFT, IPageLayout.LEFT, 0.2f, editor);
        var right = layout.createFolder(ID_RIGHT, IPageLayout.RIGHT, 0.75f, editor);
        var bottom = layout.createFolder(ID_BOTTOM, IPageLayout.BOTTOM, 0.75f, editor);
        var leftBottom = layout.createFolder(ID_LEFT_BOTTOM, IPageLayout.BOTTOM, 0.7f, ID_LEFT);

        left.addView(ID_EXPLORER);
        leftBottom.addView(IPageLayout.ID_OUTLINE);

        right.addView(IPageLayout.ID_PROP_SHEET);

        bottom.addView(ID_CONSOLE_VIEW);
        bottom.addView(ID_FUNCTIONS_VIEW);
        // bottom.addView(ID_PARAMETERS_VIEW);
        bottom.addPlaceholder(IPageLayout.ID_PROGRESS_VIEW);

        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(ID_CONSOLE_VIEW);
        layout.addShowViewShortcut(ID_EXPLORER);
        layout.addShowViewShortcut(ID_FUNCTIONS_VIEW);
        layout.addShowViewShortcut(ID_HELP_VIEW);
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
        layout.addNewWizardShortcut("org.csstudio.opibuilder.wizards.newOPIWizard");
        layout.addNewWizardShortcut("org.csstudio.opibuilder.wizards.newJSWizard");
    }
}
