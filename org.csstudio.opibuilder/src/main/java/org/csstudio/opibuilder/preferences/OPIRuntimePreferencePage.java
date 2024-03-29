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
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jdom2.Verifier;

/**
 * The preference page for OPIBuilder
 */
public class OPIRuntimePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private String wrongMacroName = "";

    private StringTableFieldEditor macrosEditor;

    public OPIRuntimePreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(OPIBuilderPlugin.getDefault().getPreferenceStore());
        setMessage("OPI Runtime Preferences");
    }

    @Override
    protected void createFieldEditors() {
        var parent = getFieldEditorParent();

        macrosEditor = new StringTableFieldEditor(PreferencesHelper.RUN_MACROS, "Macros: ", parent,
                new String[] { "Name", "Value" }, new boolean[] { true, true }, new MacroEditDialog(parent.getShell()),
                new int[] { 120, 120 }) {
            @Override
            public boolean isValid() {
                String reason;
                for (var row : items) {
                    reason = Verifier.checkElementName(row[0]);
                    if (reason != null) {
                        wrongMacroName = row[0];
                        return false;
                    }
                }
                return true;
            }

            @Override
            protected void doStore() {
                if (!isValid()) {
                    return;
                }
                super.doStore();
            }

            @Override
            protected void doFillIntoGrid(Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                tableEditor.getTableViewer().getTable().addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        var valid = isValid();
                        fireStateChanged(IS_VALID, !valid, valid);
                    }
                });
                tableEditor.getTableViewer().getTable().addFocusListener(new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        var valid = isValid();
                        fireStateChanged(IS_VALID, !valid, valid);
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                        var valid = isValid();
                        fireStateChanged(IS_VALID, !valid, valid);
                    }
                });
            }
        };
        addField(macrosEditor);

        var guiRefreshCycleEditor = new IntegerFieldEditor(PreferencesHelper.OPI_GUI_REFRESH_CYCLE,
                "OPI GUI Refresh Cycle (ms)", parent);
        guiRefreshCycleEditor.setValidRange(10, 5000);
        guiRefreshCycleEditor.getTextControl(parent)
                .setToolTipText("The fastest refresh cycle for OPI GUI in millisecond");
        addField(guiRefreshCycleEditor);

        var pulsingMinorPeriodFieldEditor = new IntegerFieldEditor(PreferencesHelper.PULSING_ALARM_MINOR_PERIOD,
                "Time period of MINOR alarm if pulsing alarm selected (ms)", parent);
        pulsingMinorPeriodFieldEditor.setValidRange(100, 10000);
        pulsingMinorPeriodFieldEditor.getTextControl(parent)
                .setToolTipText("If the pulsing alarm box is checked for a widget that monitors a PV, "
                        + "then what is the time period of the pulse with the PV is in MINOR alarm severity");
        addField(pulsingMinorPeriodFieldEditor);

        var pulsingMajorPeriodFieldEditor = new IntegerFieldEditor(PreferencesHelper.PULSING_ALARM_MAJOR_PERIOD,
                "Time period of MAJOR alarm if pulsing alarm selected (ms)", parent);
        pulsingMajorPeriodFieldEditor.setValidRange(100, 10000);
        pulsingMajorPeriodFieldEditor.getTextControl(parent)
                .setToolTipText("If the pulsing alarm box is checked for a widget that monitors a PV, "
                        + "then what is the time period of the pulse with the PV is in MAJOR alarm severity");
        addField(pulsingMajorPeriodFieldEditor);

        var pythonPathEditor = new StringFieldEditor(PreferencesHelper.PYTHON_PATH, "PYTHONPATH", parent);
        pythonPathEditor.getTextControl(parent).setToolTipText("The path to search python modules");
        addField(pythonPathEditor);

        var showFullScreenDialogEditor = new BooleanFieldEditor(PreferencesHelper.SHOW_FULLSCREEN_DIALOG,
                "Show tip dialog about how to exit fullscreen", parent);
        addField(showFullScreenDialogEditor);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        var src = event.getSource();
        if (src instanceof FieldEditor) {
            var prefName = ((FieldEditor) src).getPreferenceName();
            if (prefName.equals(PreferencesHelper.RUN_MACROS)) {
                if ((Boolean) event.getNewValue()) {
                    setMessage(null);
                } else {
                    setMessage(wrongMacroName + " is not a valid Macro name!", ERROR);
                }
            }
        }
    }

    @Override
    public boolean performOk() {
        macrosEditor.tableEditor.getTableViewer().getTable().forceFocus();
        if (!isValid()) {
            return false;
        }
        return super.performOk();
    }
}
