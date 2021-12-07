/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.actions;

import org.eclipse.gef.internal.InternalImages;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomOutAction;

/**
 * Tweak {@link ZoomOutAction}
 */
@SuppressWarnings("restriction")
public class PartZoomOutAction extends PartZoomAction {

    /**
     * Constructor for ZoomInAction.
     *
     * @param zoomManager
     *            the zoom manager
     */
    public PartZoomOutAction() {
        super("Zoom &Out", InternalImages.DESC_ZOOM_OUT);
        setToolTipText("Zoom Out");
        setId(GEFActionConstants.ZOOM_OUT);
        setActionDefinitionId(GEFActionConstants.ZOOM_OUT);
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public void run() {
        if (zoomManager != null) {
            zoomManager.zoomOut();
        }
    }

    /**
     * @see org.eclipse.gef.editparts.ZoomListener#zoomChanged(double)
     */
    @Override
    public void zoomChanged(double zoom) {
        setEnabled(zoomManager.canZoomOut());
    }

}
