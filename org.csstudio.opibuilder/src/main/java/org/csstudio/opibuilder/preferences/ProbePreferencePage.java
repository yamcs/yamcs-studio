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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ProbePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ProbePreferencePage() {
        super(GRID);
        setPreferenceStore(OPIBuilderPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        var parent = getFieldEditorParent();

        var probeOPIEditor = new WorkspaceFileFieldEditor(PreferencesHelper.PROBE_OPI, "Probe OPI: ",
                new String[] { "opi" }, parent);
        addField(probeOPIEditor);

        // Empty
        new Label(parent, SWT.NONE);

        var note = new Text(parent, SWT.MULTI | SWT.READ_ONLY);
        note.setBackground(parent.getBackground());
        note.setText("This is the display used by the Probe pop-up dialog available\n"
                + "from a PV widget's context menu. This OPI can dynamically fetch the\n"
                + "context-specific PV name via the macro $(probe_pv).\n\n"
                + "If undefined, a default Probe OPI is used containing a Gauge and an\n"
                + "XY Graph widget for the applicable PV.");
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
