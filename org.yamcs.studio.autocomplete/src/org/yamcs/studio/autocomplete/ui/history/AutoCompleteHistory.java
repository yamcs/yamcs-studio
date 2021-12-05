/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.autocomplete.ui.history;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yamcs.studio.autocomplete.AutoCompletePlugin;
import org.yamcs.studio.autocomplete.ui.AutoCompleteTypes;
import org.yamcs.studio.autocomplete.ui.preferences.Preferences;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 * Handles history of auto-completed fields.
 */
public class AutoCompleteHistory {

    private final Control control;
    private final IControlContentAdapter controlContentAdapter;

    public AutoCompleteHistory(Control control, IControlContentAdapter adapter) {
        this.control = control;
        this.controlContentAdapter = adapter;

        installListener(control);
    }

    /**
     * Install listeners on specified control to add an entry in the history when a {@link SelectionEvent} is raised.
     *
     * @param control
     */
    public void installListener(final Control control) {
        if (control == null || control.isDisposed()) {
            return;
        }
        if (control instanceof Combo) {
            ((Combo) control).addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    handleSelection();
                }
            });
        } else if (control instanceof Button) {
            ((Button) control).addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    handleSelection();
                }
            });
        } else {
            control.addListener(SWT.DefaultSelection, e -> handleSelection());
        }
    }

    private void handleSelection() {
        if (!control.isDisposed()) {
            String new_entry = controlContentAdapter.getControlContents(control);
            addEntry(new_entry);
        }
    }

    /**
     * Add an entry to the history. History contains unique values. If a value is already in the history, this value is
     * bring to the first place in the file. The maximum number of entries in the history is defined by preferences.
     *
     * @param newEntry
     */
    public synchronized void addEntry(final String newEntry) {
        // Avoid empty entries
        if (newEntry == null || newEntry.trim().isEmpty()) {
            return;
        }
        // Entry => type
        Map<String, String> entries = new HashMap<>();
        if (newEntry.startsWith("=")) {
            entries.put(newEntry, AutoCompleteTypes.Formula);
            Pattern quotedVariable = Pattern.compile("'([^']+)'");
            Matcher m = quotedVariable.matcher(newEntry);
            while (m.find()) {
                entries.put(m.group(1), AutoCompleteTypes.PV);
            }
        } else {
            entries.put(newEntry, AutoCompleteTypes.PV);
        }
        for (Entry<String, String> entry : entries.entrySet()) {
            updateHistory(entry.getKey(), entry.getValue());
        }
    }

    private void updateHistory(String newEntry, String entryType) {
        if (entryType == null || entryType.isEmpty()) {
            return;
        }
        LinkedList<String> fifo = AutoCompletePlugin.getDefault().getHistory(entryType);
        if (fifo == null) {
            return;
        }
        if (Preferences.getHistorySize() == 0) {
            fifo.clear();
            return;
        }
        // Remove if present, so that is re-added on top
        int index = -1;
        while ((index = fifo.indexOf(newEntry)) >= 0) {
            fifo.remove(index);
        }

        // Maybe remove oldest, i.e. bottom-most, entry
        while (fifo.size() >= Preferences.getHistorySize()) {
            fifo.removeLast();
        }

        // Add at the top
        fifo.addFirst(newEntry);
    }

}
