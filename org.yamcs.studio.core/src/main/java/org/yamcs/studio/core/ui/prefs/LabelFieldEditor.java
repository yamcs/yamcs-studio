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

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A field editor for displaying labels not associated with other widgets.
 */
public class LabelFieldEditor extends FieldEditor {

    private Label label;

    /**
     * All labels can use the same preference name since they don't store any preference.
     */
    public LabelFieldEditor(String value, Composite parent) {
        super("label", value, parent);
    }

    /**
     * Adjusts the field editor to be displayed correctly for the given number of columns.
     */
    @Override
    protected void adjustForNumColumns(int numColumns) {
        ((GridData) label.getLayoutData()).horizontalSpan = numColumns;
    }

    /**
     * Fills the field editor's controls into the given parent.
     */
    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        label = getLabelControl(parent);

        var gridData = new GridData();
        gridData.horizontalSpan = numColumns;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = false;
        gridData.verticalAlignment = GridData.CENTER;
        gridData.grabExcessVerticalSpace = false;

        label.setLayoutData(gridData);
    }

    /**
     * Returns the number of controls in the field editor.
     */
    @Override
    public int getNumberOfControls() {
        return 1;
    }

    // Labels do not persist any preferences, so these methods are empty.
    @Override
    protected void doLoad() {
    }

    @Override
    protected void doLoadDefault() {
    }

    @Override
    protected void doStore() {
    }
}
