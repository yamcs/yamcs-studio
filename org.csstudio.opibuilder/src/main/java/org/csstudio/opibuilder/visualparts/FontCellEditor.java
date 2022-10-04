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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A table cell editor for values of type Font.
 * <p>
 * There is already a FontCellEditor, but when activated, it adds another step where it only displays a small color
 * patch, the Font indices and then a button to start the dialog.
 * <p>
 * That's a waste of real estate, adds another 'click' to the editing of fonts, plus the overall layout was really poor
 * on Mac OS X, where the button didn't fully show.
 * <p>
 * This implementation, based on the CheckboxCellEditor sources, jumps right into the font dialog.
 */
public final class FontCellEditor extends CellEditor {
    /**
     * A shell.
     */
    private Shell _shell;

    /**
     * The current RGB value.
     */
    private FontData _value;

    /**
     * Creates a new font cell editor parented under the given control. The cell editor value is an SWT Font value.
     *
     * @param parent
     *            The parent table.
     */
    public FontCellEditor(Composite parent) {
        super(parent, SWT.NONE);
        _shell = parent.getShell();
    }

    @Override
    public void activate() {
        var dialog = new FontDialog(_shell);
        if (_value != null) {
            dialog.setFontList(new FontData[] { _value });
        }
        _value = dialog.open();
        if (_value != null) {
            fireApplyEditorValue();
        }
    }

    @Override
    protected Control createControl(Composite parent) {
        return null;
    }

    @Override
    protected Object doGetValue() {
        return _value;
    }

    @Override
    protected void doSetFocus() {
        // Ignore
    }

    @Override
    protected void doSetValue(Object value) {
        Assert.isTrue(value instanceof FontData);
        _value = (FontData) value;
    }
}
