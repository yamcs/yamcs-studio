/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.helpers;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

/**
 * Decorates a Combo box to maintain the history.
 * <p>
 * Newly entered items are added to the top of the combo list, dropping last items off the list when reaching a
 * configurable maximum list size. If an item is selected/entered again, it will pop at the beginning of the list (so
 * that continuously used items are not lost).
 * <p>
 * You must
 * <ul>
 * <li>either implement <code>itemSelected()</code> or <code>newSelection()</code> to handle entered/selected values.
 * <li>decide if you want to call loadSettings() to restore the saved values
 * <li>save values via saveSettings, or use the save_on_dispose option of the constructor.
 * </ul>
 */
public class ComboHistoryHelper {

    private static final String TAG = "values";
    private static final int DEFAULT_HISTORY_SIZE = 10;
    private final IDialogSettings settings;
    private final String tag;
    private final Combo combo;
    private final int max;

    /**
     * Attach helper to given combo box, using max list length.
     *
     * @param settings
     *            where to persist the combo box list
     * @param tag
     *            tag used for persistence
     * @param combo
     *            the combo box
     */
    public ComboHistoryHelper(IDialogSettings settings, String tag, Combo combo) {
        this(settings, tag, combo, DEFAULT_HISTORY_SIZE, true);
    }

    /**
     * Attach helper to given combo box, using max list length.
     *
     * @param settings
     *            where to persist the combo box list
     * @param tag
     *            tag used for persistence
     * @param combo
     *            the combo box
     * @param max
     *            number of elements to keep in history
     * @param saveOnDispose
     *            whether current values should be saved at widget disposal
     */
    public ComboHistoryHelper(IDialogSettings settings, String tag, Combo combo, int max, boolean saveOnDispose) {
        this.settings = settings;
        this.tag = tag;
        this.combo = combo;
        this.max = max;

        // React whenever an existing entry is selected,
        // or a new name is entered.
        // New names are also added to the list.
        combo.addSelectionListener(new SelectionListener() {
            // Called after <Return> was pressed
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                var new_entry = ComboHistoryHelper.this.combo.getText();
                addEntry(new_entry);
                ComboHistoryHelper.this.combo.select(0);
                itemSelected(new_entry);
            }

            // Called after existing entry was picked from list
            @Override
            public void widgetSelected(SelectionEvent e) {
                var name = ComboHistoryHelper.this.combo.getText();
                itemSelected(name);
            }
        });

        // Register saving on dispose
        if (saveOnDispose) {
            combo.addDisposeListener(e -> saveSettings());
        }
    }

    private boolean changing_combo = false;

    public void changeSelection(String entry) {
        var index = combo.indexOf(entry);
        if (index != -1) {
            combo.select(index);
        } else {
            addEntry(entry);
            combo.select(0);
        }
    }

    /**
     * Adds a new entry to the list. If entry already there, do nothing.
     *
     * @param newEntry
     *            the new value
     */
    public void addEntry(String newEntry) {
        // Possibly removing an entry below can trigger a selection change.
        // At least on Mac OS X that sometimes resulted in infinite recursion.
        // Avoid such recursion.
        if (changing_combo) {
            return;
        }
        changing_combo = true;
        try {
            // Avoid empty entries
            if (newEntry.trim().isEmpty()) {
                return;
            }

            // Remove if present, so that is re-added on top
            var entry = -1;
            while ((entry = combo.indexOf(newEntry)) >= 0) {
                combo.remove(entry);
            }

            // Maybe remove oldest, i.e. bottom-most, entry
            if (combo.getItemCount() >= max) {
                combo.remove(combo.getItemCount() - 1);
            }

            // Add at the top
            combo.add(newEntry, 0);
        } finally {
            changing_combo = false;
        }
    }

    private String oldSelection = null;

    /**
     * Invoked whenever an item is selected.
     * <p>
     * Default implementation will compare with previous selection, and only invoke <code>newSelection</code> when the
     * selection changed.
     *
     * <p>
     * Override this method to be notified of any selection, including the case where the user re-selects the same
     * entry, or presses 'Return' in the combo's text field without changing its content.
     *
     * @param selection
     *            Selected item, may be <code>null</code>
     */
    public void itemSelected(String selection) {
        if (oldSelection == null) {
            if (selection == null) {
                return;
            }
        } else if (oldSelection.equals(selection)) {
            return;
        }

        oldSelection = selection;
        newSelection(selection);
    }

    /**
     * Invoked whenever a new entry was entered or selected.
     *
     * <p>
     * Override this method to be notified only when the user selects a different item. Re-selection of the same item
     * will be ignored.
     *
     * @param selection
     *            Selected item, may be <code>null</code>
     */
    public void newSelection(String selection) {
        // Default: NOP
    }

    /** Load persisted list values. */
    public void loadSettings() {
        if (settings != null) {
            var pvs = settings.getSection(tag);
            if (pvs == null) {
                return;
            }
            var values = pvs.getArray(TAG);
            if (values != null) {
                for (var i = values.length - 1; i >= 0; i--) {
                    // Load as if they were entered, i.e. skip duplicates
                    addEntry(values[i]);
                }
            }
        }
    }

    /** Save list values to persistent storage. */
    public void saveSettings() {
        if (settings != null) {
            var values = settings.addNewSection(tag);
            values.put(TAG, combo.getItems());
        }
    }
}
