/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CommandingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PREF_DEFAULT_STACK_WAIT = "defaultStackWait";
    public static final String PREF_PREFERRED_NAMESPACE = "preferredNamespace";

    private IntegerFieldEditor defaultStackWait;
    private StringFieldEditor preferredNamespace;

    public CommandingPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(CommandingPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        var parent = getFieldEditorParent();
        defaultStackWait = new IntegerFieldEditor(PREF_DEFAULT_STACK_WAIT, "Default Stack Wait (ms):", parent);
        addField(defaultStackWait);
        new Label(parent, SWT.NONE);
        var note = new Text(parent, SWT.MULTI | SWT.READ_ONLY);
        note.setBackground(parent.getBackground());
        note.setText("""
                When starting a new stack, use this value as the fixed wait
                time between commands.
                """);

        preferredNamespace = new StringFieldEditor(PREF_PREFERRED_NAMESPACE, "Preferred Namespace:", parent);
        addField(preferredNamespace);

        new Label(parent, SWT.NONE);
        note = new Text(parent, SWT.MULTI | SWT.READ_ONLY);
        note.setBackground(parent.getBackground());
        note.setText("""
                If a command has an alias under this namespace, that alias is
                displayed in Command History instead of the fully qualified Yamcs
                name.

                This setting also determines the name that is used when adding
                commands to a stack.
                """);
    }

    @Override
    public boolean performOk() {
        var store = CommandingPlugin.getDefault().getPreferenceStore();

        var propertiesChanged = defaultStackWait.getIntValue() != store.getInt(PREF_DEFAULT_STACK_WAIT)
                || !preferredNamespace.getStringValue().equals(store.getString(PREF_PREFERRED_NAMESPACE));

        // Save to store
        var ret = super.performOk();

        if (propertiesChanged) {
            var dialog = new MessageDialog(null, "Apply changes", null,
                    "To apply preference to views, close and re-open them", MessageDialog.INFORMATION,
                    new String[] { "OK" }, 0);
            dialog.open();
        }

        return ret;
    }
}
