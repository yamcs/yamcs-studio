/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.yamcs.studio.autocomplete.AutoCompletePlugin;

/**
 * Preference Page, registered in plugin.xml
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public PreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, AutoCompletePlugin.PLUGIN_ID));
        setMessage("Auto-complete Settings");
    }

    @Override
    public void init(IWorkbench workbench) {
        // NOP
    }

    @Override
    protected void createFieldEditors() {
        var parent = getFieldEditorParent();

        addField(new StringFieldEditor(Preferences.HISTORY_SIZE, "History size", parent));

        var clearHistory = new Button(parent, SWT.PUSH);
        clearHistory.setText("Clear history");
        clearHistory.setLayoutData(new GridData());
        clearHistory.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AutoCompletePlugin.getDefault().clearSettings();
            }
        });

        var noteWrapper = new Composite(parent, SWT.NONE);
        noteWrapper.setLayoutData(new GridData());
        noteWrapper.setLayout(new GridLayout(2, false));

        var noteLabel = new Label(noteWrapper, SWT.NONE);
        var fontData = noteLabel.getFont().getFontData()[0];
        fontData.setStyle(SWT.BOLD);
        noteLabel.setFont(new Font(parent.getDisplay(), fontData));
        noteLabel.setText("Note: ");

        var note = new Text(noteWrapper, SWT.MULTI | SWT.READ_ONLY);
        note.setBackground(parent.getBackground());
        note.setText(
                "The 'History size' value is the maximum number of entries in the History.\nEach entry is stored only once and the entries of the History are sorted \naccording to their occurrence.");
    }
}
