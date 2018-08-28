package org.csstudio.opibuilder.preferences;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MainPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public MainPreferencePage() {
        super(GRID);
        setPreferenceStore(OPIBuilderPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
    }

    @Override
    public void init(IWorkbench workbench) {

    }

    @Override
    public boolean performOk() {
        if (!isValid()) {
            return false;
        }
        return super.performOk();
    }
}
