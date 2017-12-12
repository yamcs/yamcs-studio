package org.yamcs.studio.css.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.yamcs.studio.css.Activator;
import org.yamcs.studio.css.SeverityHandlerSound;

public class SoundPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor beepWarning;
    private BooleanFieldEditor beepCritical;
    private ComboFieldEditor triggerBeep;

    public SoundPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Set sound properties for Yamcs Studio");
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
        String[][] typeOfBeepTriggers = { { "Do Not Beep", "NONE" },
                { "Parameter First Out-of-Limit Value", "FIRST" },
                { "Parameter Each Out-of-Limit Value", "EACH" } };
        triggerBeep = new ComboFieldEditor("trigerBeep", "Beep on event:",
                typeOfBeepTriggers, getFieldEditorParent());
        addField(triggerBeep);

        beepWarning = new BooleanFieldEditor("beepWarning", "Beep on Warning",
                getFieldEditorParent());
        addField(beepWarning);

        beepCritical = new BooleanFieldEditor("beepCritical",
                "Beep on Critical", getFieldEditorParent());
        addField(beepCritical);
    }

    @Override
    public boolean performOk() {
        // Save to store
        boolean ret = super.performOk();

        // Apply preference in Severity Handler Sound class
        SeverityHandlerSound.updatePrefence();

        return ret;
    }

}
