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

import java.util.ArrayList;
import java.util.List;

import org.csstudio.csdata.ProcessVariable;
import org.csstudio.ui.util.dnd.SerializableItemTransfer;
import org.eclipse.gef.EditPartViewer;

/**
 * The Drop PV target listener for process variable name transfer.
 */
public class ProcessVariableNameTransferDropPVTargetListener extends AbstractDropPVTargetListener {

    public ProcessVariableNameTransferDropPVTargetListener(EditPartViewer viewer) {
        super(viewer, SerializableItemTransfer.getTransfer(ProcessVariable[].class));
    }

    @Override
    protected String[] getPVNamesFromTransfer() {
        if (getCurrentEvent().data == null) {
            return null;
        }
        var pvArray = (ProcessVariable[]) getCurrentEvent().data;
        List<String> pvList = new ArrayList<String>();
        for (ProcessVariable pv : pvArray) {
            pvList.add(pv.getName());
        }
        return pvList.toArray(new String[pvList.size()]);
    }
}
