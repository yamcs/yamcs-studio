package org.yamcs.studio.core.ui.prefs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.yamcs.studio.core.ui.YamcsUIPlugin;

public class DateFormatPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PREF_DATEFORMAT = "dateFormat";

    private StringFieldEditor format;

    public DateFormatPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(YamcsUIPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        format = new StringFieldEditor(PREF_DATEFORMAT, "Date Format:", parent);
        addField(format);

    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = YamcsUIPlugin.getDefault().getPreferenceStore();

        boolean propertiesChanged = !format.getStringValue().equals(store.getString(PREF_DATEFORMAT));

        // Save to store
        boolean ret = super.performOk();

        if (propertiesChanged) {
            warningApply();
        }

        return ret;
    }

    private static void warningApply() {
        MessageDialog dialog = new MessageDialog(
                null,
                "Apply changes",
                null,
                "To apply preference to views, close and re-open them",
                MessageDialog.INFORMATION, new String[] { "OK" }, 0);
        dialog.open();
    }
}
