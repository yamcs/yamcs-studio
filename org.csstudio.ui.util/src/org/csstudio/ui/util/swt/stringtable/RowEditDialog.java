/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.swt.stringtable;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

/**
 * The RowEditDialog is the abstract superclass of dialogs that are used to edit a row of items in a table
 */
public abstract class RowEditDialog extends Dialog {

    protected String[] rowData;

    /** Initialize Dialog */
    protected RowEditDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Edit Row Data");
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * Set the rowData which will be initially displayed in the Edit Dialog. It must be called prior to open().
     * 
     * @param rowData
     *            the rowData to set
     */
    public void setRowData(String[] rowData) {
        this.rowData = rowData;
    }

    /**
     * @return the rowData
     */
    public String[] getRowData() {
        return rowData;
    }
}
