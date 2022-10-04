/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.dnd;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

/**
 * The listener when a pv name related transfer data is dropped to the viewer.
 */
public abstract class AbstractDropPVTargetListener extends AbstractTransferDropTargetListener {

    public AbstractDropPVTargetListener(EditPartViewer viewer, Transfer xfer) {
        super(viewer, xfer);
    }

    @Override
    protected void updateTargetRequest() {
        ((DropPVRequest) getTargetRequest()).setLocation(getDropLocation());
    }

    @Override
    protected void updateTargetEditPart() {
        super.updateTargetEditPart();
        if (getTargetEditPart() instanceof AbstractBaseEditPart) {
            ((DropPVRequest) getTargetRequest()).setTargetWidget((AbstractBaseEditPart) getTargetEditPart());
        }
    }

    @Override
    protected Request createTargetRequest() {
        return new DropPVRequest();
    }

    @Override
    protected void handleDragOver() {
        getCurrentEvent().detail = DND.DROP_COPY;
        super.handleDragOver();
    }

    @Override
    protected void handleDrop() {
        var pvNames = getPVNamesFromTransfer();
        if (pvNames == null) {
            return;
        }
        ((DropPVRequest) getTargetRequest()).setPvNames(pvNames);
        super.handleDrop();
    }

    /**
     * @return the PV Name array from transfer.
     */
    protected abstract String[] getPVNamesFromTransfer();
}
