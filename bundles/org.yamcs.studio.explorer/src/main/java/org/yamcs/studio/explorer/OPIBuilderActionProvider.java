package org.yamcs.studio.explorer;

import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class OPIBuilderActionProvider extends CommonActionProvider {

    private ToggleOPISchemaAction toggleSchemaAction;
    private ICommonViewerWorkbenchSite viewSite;
    private boolean contribute = false;

    @Override
    public void init(ICommonActionExtensionSite aConfig) {
        if (aConfig.getViewSite() instanceof ICommonViewerWorkbenchSite) {
            viewSite = (ICommonViewerWorkbenchSite) aConfig.getViewSite();
            toggleSchemaAction = new ToggleOPISchemaAction(viewSite.getPage(), viewSite.getSelectionProvider());
            contribute = true;
        }
    }

    @Override
    public void fillContextMenu(IMenuManager aMenu) {
        if (!contribute || getContext().getSelection().isEmpty()) {
            return;
        }

        IPerspectiveDescriptor perspective = viewSite.getPage().getPerspective();
        if (perspective.getId().equals("org.csstudio.opibuilder.opieditor")) {
            if (toggleSchemaAction.isEnabled()) {

                toggleSchemaAction.setChecked(false);
                IPath currentSchemaPath = PreferencesHelper.getSchemaOPIPath();
                IFile currentSchema = ResourceUtil.getIFileFromIPath(currentSchemaPath);
                IStructuredSelection selection = (IStructuredSelection) viewSite.getSelectionProvider().getSelection();
                for (Object o : selection.toList()) {
                    if (o instanceof IFile) {
                        if (o.equals(currentSchema)) {
                            toggleSchemaAction.setChecked(true);
                        }
                    }
                }

                aMenu.insertAfter(ICommonMenuConstants.GROUP_ADDITIONS, toggleSchemaAction);
            }
        }
    }
}
