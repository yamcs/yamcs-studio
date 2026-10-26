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

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CommandingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PREF_DEFAULT_STACK_WAIT = "defaultStackWait";
    public static final String PREF_PREFERRED_NAMESPACE = "preferredNamespace";
    public static final String PREF_STACK_FOLDER = "stackFolder";

    private IntegerFieldEditor defaultStackWait;
    private StringFieldEditor preferredNamespace;
    private DirectoryFieldEditor stackFolder;

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
        addNote(parent, """
                When starting a new stack, use this value as the fixed wait
                time between commands.
                """);

        preferredNamespace = new StringFieldEditor(PREF_PREFERRED_NAMESPACE, "Preferred Namespace:", parent);
        addField(preferredNamespace);

        addNote(parent, """
                If a command has an alias under this namespace, that alias is
                displayed in Command History instead of the fully qualified Yamcs
                name.

                This setting also determines the name that is used when adding
                commands to a stack.
                """);

        stackFolder = new DirectoryFieldEditor(PREF_STACK_FOLDER, "Command Stack Folder:", parent) {
            @Override
            protected boolean doCheckState() {
                // Same as super, but also accept a path starting with ~
                var folder = getStringValue().trim();
                return folder.isEmpty() || new File(CommandingPlugin.expandHome(folder)).isDirectory();
            }
        };
        addField(stackFolder);

        addNote(parent, """
                Initial folder for importing and exporting command stacks.
                Subsequent dialogs reopen the last used folder.
                """);
    }

    /**
     * Adds a note below the previous field editor, aligned with its input. The note spans the remaining columns,
     * because the grid has as many columns as the widest field editor (the directory editor's Browse button adds one).
     */
    private void addNote(Composite parent, String text) {
        new Label(parent, SWT.NONE);
        var note = new Text(parent, SWT.MULTI | SWT.READ_ONLY);
        note.setBackground(parent.getBackground());
        note.setText(text);
        note.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    }

    @Override
    public boolean performOk() {
        var store = CommandingPlugin.getDefault().getPreferenceStore();

        var propertiesChanged = defaultStackWait.getIntValue() != store.getInt(PREF_DEFAULT_STACK_WAIT)
                || !preferredNamespace.getStringValue().equals(store.getString(PREF_PREFERRED_NAMESPACE));

        if (!stackFolder.getStringValue().equals(store.getString(PREF_STACK_FOLDER))) {
            CommandingPlugin.getDefault().resetStackFolder();
        }

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
