/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.widgets.Display;

/**
 * Tooltip label which will show the latest tooltip value from its widget model.
 */
public class TooltipLabel extends Figure {

    private AbstractWidgetModel widgetModel;
    private AbstractBaseEditPart editPart;
    private String tooltipText;

    public TooltipLabel(AbstractWidgetModel widgetModel) {
        this.widgetModel = widgetModel;
    }

    public TooltipLabel(AbstractBaseEditPart editPart) {
        widgetModel = editPart.getWidgetModel();
        this.editPart = editPart;
    }

    @Override
    protected void paintClientArea(Graphics graphics) {
        super.paintClientArea(graphics);
        if (widgetModel == null) {
            return;
        }
        if (tooltipText == null) {
            tooltipText = getConnectionText() + widgetModel.getTooltip();
        }
        graphics.drawText(tooltipText, 1, 1);
    }

    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {

        if (widgetModel == null) {
            return new Dimension(wHint, hHint);
        }
        tooltipText = getConnectionText() + widgetModel.getTooltip();
        return FigureUtilities.getTextExtents(tooltipText, Display.getDefault().getSystemFont()).expand(2, 2);
    }

    private String getConnectionText() {
        if (editPart == null || editPart.getConnectionHandler() == null
                || editPart.getConnectionHandler().getToolTipText() == null) {
            return "";
        }
        return editPart.getConnectionHandler().getToolTipText();
    }
}
