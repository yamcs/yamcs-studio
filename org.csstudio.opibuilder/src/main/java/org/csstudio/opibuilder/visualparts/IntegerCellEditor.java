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

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * A cell editor that manages a <code>java.lang.Integer</code> entry field.
 */
public final class IntegerCellEditor extends TextCellEditor {

    public IntegerCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected void doSetValue(Object value) {
        if (value == null) {
            super.doSetValue(String.valueOf(Integer.valueOf(0)));
        } else {
            super.doSetValue(String.valueOf(value.toString()));
        }
    }
}
