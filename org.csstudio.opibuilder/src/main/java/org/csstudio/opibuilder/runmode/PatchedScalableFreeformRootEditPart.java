/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;

/**
 * Patch {@link ScalableFreeformRootEditPart} to change the zoom combo items sort to have predefined zoom contributions
 * on top.
 */
public class PatchedScalableFreeformRootEditPart extends ScalableFreeformRootEditPart {

    private ZoomManager zoomManager;

    public PatchedScalableFreeformRootEditPart() {
        zoomManager = new ZoomManager((ScalableFigure) getScaledLayers(), ((Viewport) getFigure())) {
            @Override
            public String[] getZoomLevelsAsText() {
                var originItems = super.getZoomLevelsAsText();
                if (getZoomLevelContributions() != null) {
                    var result = new String[originItems.length];
                    var contriSize = getZoomLevelContributions().size();
                    for (var i = 0; i < originItems.length; i++) {
                        result[i] = originItems[(originItems.length - contriSize + i) % originItems.length];
                    }
                    return result;
                } else {
                    return super.getZoomLevelsAsText();
                }
            }
        };
    }

    @Override
    public ZoomManager getZoomManager() {
        return zoomManager;
    }
}
