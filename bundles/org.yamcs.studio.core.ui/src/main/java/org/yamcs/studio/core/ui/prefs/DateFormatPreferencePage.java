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

    public static final String PREF_DATEFORMAT = "views.dateFormat";

    private StringFieldEditor nbMessageLineToDisplay;

    public DateFormatPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(YamcsUIPlugin.getDefault().getPreferenceStore());
        setDescription("Set properties for standart date format");
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        nbMessageLineToDisplay = new StringFieldEditor(PREF_DATEFORMAT,
                "Standart Date Format:", parent);
        addField(nbMessageLineToDisplay);

    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = YamcsUIPlugin.getDefault().getPreferenceStore();

        boolean propertiesChanged = !nbMessageLineToDisplay.getStringValue().equals(store.getString(PREF_DATEFORMAT));

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
                "To apply preference to the Views, close the views and re-open it",
                MessageDialog.INFORMATION, new String[] { "OK" }, 0);
        dialog.open();
    }
}
