/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.preferences;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The preference page for OPIBuilder
 */
public class OPIEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public OPIEditorPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(OPIBuilderPlugin.getDefault().getPreferenceStore());

        setMessage("OPI Editor Preferences");
    }

    @Override
    protected void createFieldEditors() {
        var parent = getFieldEditorParent();

        var schemaOPIEditor = new WorkspaceFileFieldEditor(PreferencesHelper.SCHEMA_OPI, "Schema OPI: ",
                new String[] { "opi" }, parent);
        schemaOPIEditor.getTextControl(parent)
                .setToolTipText("The opi file that defines the default widget properties value");
        addField(schemaOPIEditor);

        var autoSaveEditor = new BooleanFieldEditor(PreferencesHelper.AUTOSAVE,
                "Automatically save file before running.", parent);
        addField(autoSaveEditor);
    }

    @Override
    public void init(IWorkbench workbench) {

    }

    @Override
    public boolean performOk() {
        if (!isValid()) {
            return false;
        }
        return super.performOk();
    }
}
