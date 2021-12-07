/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.core.ui.prefs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.yamcs.studio.core.YamcsPlugin;

public class DateFormatPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public static final String PREF_DATEFORMAT = "dateFormat";

    private StringFieldEditor format;

    public DateFormatPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(YamcsPlugin.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        var parent = getFieldEditorParent();
        format = new StringFieldEditor(PREF_DATEFORMAT, "Date Format:", parent);
        addField(format);
    }

    @Override
    public boolean performOk() {
        var store = YamcsPlugin.getDefault().getPreferenceStore();

        var propertiesChanged = !format.getStringValue().equals(store.getString(PREF_DATEFORMAT));

        // Save to store
        var ret = super.performOk();

        if (propertiesChanged) {
            YamcsPlugin.getDefault().setDateFormat(format.getStringValue());
            warningApply();
        }

        return ret;
    }

    private static void warningApply() {
        var dialog = new MessageDialog(null, "Apply changes", null,
                "To apply preference to views, close and re-open them", MessageDialog.INFORMATION,
                new String[] { "OK" }, 0);
        dialog.open();
    }
}
