/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.dnd;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.swt.dnd.TextTransfer;

/**
 * The Drop PV target listener for text transfer.
 */
public class TextTransferDropPVTargetListener extends AbstractDropPVTargetListener {

    public TextTransferDropPVTargetListener(EditPartViewer viewer) {
        super(viewer, TextTransfer.getInstance());
    }

    @Override
    protected String[] getPVNamesFromTransfer() {
        if (getCurrentEvent().data == null)
            return null;
        String text = (String) getCurrentEvent().data;
        String[] pvNames = text.trim().split("\\s+");
        return pvNames;
    }

}
