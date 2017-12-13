package org.yamcs.studio.eventlog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PREF_LINECOUNT = "events.nbMessageLineToDisplay";
    public static final String PREF_SHOW_SEQNUM_COL = "events.showColumnSeqNum";
    public static final String PREF_SHOW_GENTIME_COL = "events.showColumnGeneration";
    public static final String PREF_SHOW_RECTIME_COL = "events.showColumnReception";

    private BooleanFieldEditor showColumSeqNum;
    private BooleanFieldEditor showColumReception;
    private BooleanFieldEditor showColumnGeneration;
    private IntegerFieldEditor nbMessageLineToDisplay;

    public PreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        // setDescription("Set properties for Event Log");
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

        Label label = new Label(parent, SWT.NONE);
        label.setText("Columns to be displayed:");

        showColumSeqNum = new BooleanFieldEditor(PREF_SHOW_SEQNUM_COL, "Sequence Number", parent);
        showColumReception = new BooleanFieldEditor(PREF_SHOW_RECTIME_COL, "Reception Time", parent);
        showColumnGeneration = new BooleanFieldEditor(PREF_SHOW_GENTIME_COL, "Generation Time", parent);
        addField(showColumSeqNum);
        addField(showColumReception);
        addField(showColumnGeneration);

    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        boolean propertiesChanged = showColumSeqNum.getBooleanValue() != store.getBoolean(PREF_SHOW_SEQNUM_COL);
        propertiesChanged |= showColumReception.getBooleanValue() != store.getBoolean(PREF_SHOW_RECTIME_COL);
        propertiesChanged |= showColumnGeneration.getBooleanValue() != store.getBoolean(PREF_SHOW_GENTIME_COL);
        propertiesChanged |= nbMessageLineToDisplay.getIntValue() != store.getInt(PREF_LINECOUNT);

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
