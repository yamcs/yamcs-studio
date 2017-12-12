package org.yamcs.studio.eventlog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor showColumSeqNum;
    private BooleanFieldEditor showColumReception;
    private BooleanFieldEditor showColumnGeneration;
    private IntegerFieldEditor nbMessageLineToDisplay;

    public PreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Set properties for Event Log");
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        nbMessageLineToDisplay = new IntegerFieldEditor(
                "events.nbMessageLineToDisplay",
                "Number of lines per event message (0: unlimited)",
                getFieldEditorParent());
        addField(nbMessageLineToDisplay);

        Label label = new Label(getFieldEditorParent(), SWT.NONE);
        label.setText("Columns to be displayed:");

        showColumSeqNum = new BooleanFieldEditor("events.showColumSeqNum", "Sequence Number", getFieldEditorParent());
        showColumReception = new BooleanFieldEditor("events.showColumReception", "Reception Time",
                getFieldEditorParent());
        showColumnGeneration = new BooleanFieldEditor("events.showColumnGeneration", "Generation Time",
                getFieldEditorParent());
        addField(showColumSeqNum);
        addField(showColumReception);
        addField(showColumnGeneration);

    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        boolean propertiesChanged = showColumSeqNum.getBooleanValue() != store.getBoolean("events.showColumSeqNum");
        propertiesChanged |= showColumReception.getBooleanValue() != store.getBoolean("events.showColumReception");
        propertiesChanged |= showColumnGeneration.getBooleanValue() != store.getBoolean("events.showColumnGeneration");
        propertiesChanged |= nbMessageLineToDisplay.getIntValue() != store.getInt("events.nbMessageLineToDisplay");

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
                "To apply preference to the Event Log, close the Event Log view an re-open it (menu Window->Show View->Event Log)",
                MessageDialog.INFORMATION, new String[] { "OK" }, 0);
        dialog.open();
    }
}
