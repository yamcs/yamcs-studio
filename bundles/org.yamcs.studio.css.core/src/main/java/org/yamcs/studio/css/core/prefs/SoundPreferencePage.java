package org.yamcs.studio.css.core.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.yamcs.studio.css.core.Activator;
import org.yamcs.studio.css.core.PVManagerSubscriptionHandler;

public class SoundPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor beepWarning;
    private BooleanFieldEditor beepCritical;
    private ComboFieldEditor triggerBeep;

    public SoundPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {

        // separator
        Label label = new Label(getFieldEditorParent(), SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 3, 1));

        // beep fields
        String[][] typeOfBeepTriggers = { { "No beep", "NONE" },
                { "Increasing severity", "FIRST" },
                { "Each out-of-limit update", "EACH" } };
        triggerBeep = new ComboFieldEditor("triggerBeep", "Beep on event:",
                typeOfBeepTriggers, getFieldEditorParent());
        addField(triggerBeep);

        beepWarning = new BooleanFieldEditor("beepWarning", "Beep on warning",
                getFieldEditorParent());
        addField(beepWarning);

        beepCritical = new BooleanFieldEditor("beepCritical",
                "Beep on critical", getFieldEditorParent());
        addField(beepCritical);
    }

    @Override
    public boolean performOk() {
        // Save to store
        boolean ret = super.performOk();

        // Apply preference in Severity Handler Sound class
        PVManagerSubscriptionHandler.getInstance().getBeeper().updatePreference();

        return ret;
    }
}
