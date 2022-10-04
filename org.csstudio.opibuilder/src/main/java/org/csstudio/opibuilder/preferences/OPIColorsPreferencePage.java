/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.preferences;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class OPIColorsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public OPIColorsPreferencePage() {
        super(GRID);
        setPreferenceStore(OPIBuilderPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        var parent = getFieldEditorParent();

        var colorEditor = new PredefinedColorsFieldEditor("colors.list", parent);
        addField(colorEditor);
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
