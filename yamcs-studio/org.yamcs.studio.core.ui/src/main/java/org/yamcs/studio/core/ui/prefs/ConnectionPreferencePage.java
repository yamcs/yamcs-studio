package org.yamcs.studio.core.ui.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class ConnectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor singleConnectionMode;
    private StringFieldEditor connectionString;

    public ConnectionPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(YamcsUIPlugin.getDefault().getPreferenceStore());
        setDescription("Yamcs Connection Mode");
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {

        // separator
        Label label = new Label(getFieldEditorParent(), SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 3, 1));

        singleConnectionMode = new BooleanFieldEditor("singleConnectionMode", "Single Connection Mode", getFieldEditorParent());
        addField(singleConnectionMode);

        connectionString = new StringFieldEditor("connectionString", "Connection String (for single connection mode)", getFieldEditorParent());
        addField(connectionString);
    }

    @Override
    public boolean performOk() {
        // Save to store
        return super.performOk();
    }
}
