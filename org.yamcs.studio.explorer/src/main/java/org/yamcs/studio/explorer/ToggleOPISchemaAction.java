package org.yamcs.studio.explorer;

import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class ToggleOPISchemaAction extends Action implements IWorkbenchAction {

    private ISelectionProvider selectionProvider;

    public ToggleOPISchemaAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
        super("Use as OPI Schema");
        this.selectionProvider = selectionProvider;
    }

    @Override
    public int getStyle() {
        return AS_CHECK_BOX;
    }

    @Override
    public void run() {
        var currentSchemaPath = PreferencesHelper.getSchemaOPIPath();
        var currentSchema = ResourceUtil.getIFileFromIPath(currentSchemaPath);

        var selection = (IStructuredSelection) selectionProvider.getSelection();
        for (Object o : selection.toList()) {
            if (o instanceof IFile) {
                if (o.equals(currentSchema)) {
                    // Untoggle
                    PreferencesHelper.setSchemaOPIPath(null);
                } else {
                    var schemaPath = ((IFile) o).getFullPath();
                    PreferencesHelper.setSchemaOPIPath(schemaPath);
                }
            }
        }
    }

    @Override
    public void dispose() {
    }
}
