/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * An abstract cell editor, which values should be edited by a custom Dialog.
 */
public abstract class AbstractDialogCellEditor extends CellEditor {

    /**
     * A shell.
     */
    private Shell _shell;

    /**
     * The title for this CellEditor.
     */
    private final String _title;
    /**
     * A boolean representing the open state of the dialog.
     */
    private boolean _dialogIsOpen = false;

    /**
     * Creates a new string cell editor parented under the given control. The cell editor value is a Map of Strings.
     *
     * @param parent
     *            The parent table.
     * @param title
     *            The title for this CellEditor
     */
    public AbstractDialogCellEditor(Composite parent, String title) {
        super(parent, SWT.NONE);
        _shell = parent.getShell();
        _title = title;
    }

    @Override
    public void activate() {
        if (!_dialogIsOpen) {
            _dialogIsOpen = true;
            this.openDialog(_shell, _title);
            if (this.shouldFireChanges()) {
                fireApplyEditorValue();
            }
            _dialogIsOpen = false;
        }
    }

    /**
     * Creates and opens the Dialog.
     * 
     * @param parentShell
     *            The parent shell for the dialog
     * @param dialogTitle
     *            The title for the dialog
     */
    protected abstract void openDialog(Shell parentShell, String dialogTitle);

    /**
     * Returns, if CellEditor.fireApplyEditorValue() should be called.
     * 
     * @return true if CellEditor.fireApplyEditorValue() should be called, false otherwise
     */
    protected abstract boolean shouldFireChanges();

    @Override
    protected final Control createControl(Composite parent) {
        return null;
    }

    @Override
    protected void doSetFocus() {
        // Ignore
    }

}
