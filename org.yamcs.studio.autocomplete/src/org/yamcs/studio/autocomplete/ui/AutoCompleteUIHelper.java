/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui;

import java.util.List;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Auto complete Widget helper to manage with special field editor.
 */
public class AutoCompleteUIHelper {

    public static TextCellEditor createAutoCompleteTextCellEditor(Composite parent, String type) {
        return new AutoCompleteTextCellEditor(parent, type);
    }

    public static TextCellEditor createAutoCompleteTextCellEditor(Composite parent, String type,
            List<Control> historyHandlers) {
        return new AutoCompleteTextCellEditor(parent, type, historyHandlers);
    }

    public static void handleSelectEvent(Control control, AutoCompleteWidget autocompleteWidget) {
        autocompleteWidget.getHistory().installListener(control);
    }

}
