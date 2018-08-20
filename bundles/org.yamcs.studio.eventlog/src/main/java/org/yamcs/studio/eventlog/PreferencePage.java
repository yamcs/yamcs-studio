package org.yamcs.studio.eventlog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PREF_LINECOUNT = "events.nbMessageLineToDisplay";

    private IntegerFieldEditor nbMessageLineToDisplay;

    public PreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        nbMessageLineToDisplay = new IntegerFieldEditor(PREF_LINECOUNT,
                "Number of lines per event message (0: unlimited)", parent);
        addField(nbMessageLineToDisplay);
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        boolean propertiesChanged = nbMessageLineToDisplay.getIntValue() != store.getInt(PREF_LINECOUNT);

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
                "For Event Log preferences to take effect, close the Event Log view an re-open it (menu Window->Show View->Event Log)",
                MessageDialog.INFORMATION, new String[] { "OK" }, 0);
        dialog.open();
    }
}
