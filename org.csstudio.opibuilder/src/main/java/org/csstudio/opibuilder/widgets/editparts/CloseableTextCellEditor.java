/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * A {@link TextCellEditor} that exposes a method to externally allow accepting a value.
 *
 * This was introduced because we want to have the ability to accept a direct edit in SWT, when for example an action
 * button in Draw2D is clicked (both focus systems are independent, hence this trickery).
 */
public class CloseableTextCellEditor extends TextCellEditor {

    public CloseableTextCellEditor(Composite composite, int style) {
        super(composite, style);
    }

    /**
     * Accept the current value and close the direct editor
     */
    public void acceptValue() {
        super.focusLost();
    }
}
