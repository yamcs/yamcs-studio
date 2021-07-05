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
    public static final String PREF_RULES = "rules.list";

    private IntegerFieldEditor messageLineCount;

    public PreferencePage() {
        super(GRID);
        setPreferenceStore(EventLogPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();

        ColoringRulesFieldEditor ruleEditor = new ColoringRulesFieldEditor(PREF_RULES,
                parent);
        addField(ruleEditor);

        messageLineCount = new IntegerFieldEditor(PREF_LINECOUNT,
                "Number of lines per event message (0: unlimited)", parent);
        addField(messageLineCount);
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = EventLogPlugin.getDefault().getPreferenceStore();

        boolean propertiesChanged = messageLineCount.getIntValue() != store.getInt(PREF_LINECOUNT);

        // Save to store
        boolean ret = super.performOk();

        if (propertiesChanged) {
            MessageDialog dialog = new MessageDialog(getShell(), "Apply changes", null,
                    "For Event Log preferences to take effect, close the Event Log view an re-open it (menu Window->Show View->Event Log)",
                    MessageDialog.INFORMATION, new String[] { "OK" }, 0);
            dialog.open();
        }

        return ret;
    }
}
