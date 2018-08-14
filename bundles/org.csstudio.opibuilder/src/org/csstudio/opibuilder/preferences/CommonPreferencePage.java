/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.preferences;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The preference page for OPIBuilder
 * 
 * @author Xihui Chen
 *
 */
public class CommonPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    // private static final String RESTART_MESSAGE = "Changes only takes effect after restart.";

    public CommonPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(OPIBuilderPlugin.getDefault().getPreferenceStore());
        setMessage("BOY Common Preferences");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        WorkspaceFileFieldEditor colorEditor = new WorkspaceFileFieldEditor(PreferencesHelper.COLOR_FILE,
                "Color File: ", new String[] { "def" }, parent);//$NON-NLS-2$
        addField(colorEditor);

        WorkspaceFileFieldEditor fontEditor = new WorkspaceFileFieldEditor(PreferencesHelper.FONT_FILE,
                "Font File: ", new String[] { "def" }, parent);//$NON-NLS-2$
        addField(fontEditor);

        BooleanFieldEditor noEditModeEditor = new BooleanFieldEditor(PreferencesHelper.NO_EDIT,
                "No-Editing mode", parent);
        addField(noEditModeEditor);

        BooleanFieldEditor advanceGraphicsEditor = new BooleanFieldEditor(PreferencesHelper.DISABLE_ADVANCED_GRAPHICS,
                "Disable Advanced Graphics", parent);
        advanceGraphicsEditor.getDescriptionControl(parent).setToolTipText(
                "This will disable alpha, anti-alias and gradient effect. " +
                        "OPI need to be re-opened to make this take effect.");
        addField(advanceGraphicsEditor);
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
