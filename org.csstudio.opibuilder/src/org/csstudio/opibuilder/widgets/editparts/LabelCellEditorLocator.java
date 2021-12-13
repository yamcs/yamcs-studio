/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

/**
 * The locator of label cell editor.
 */
public class LabelCellEditorLocator implements CellEditorLocator {

    private IFigure labelFigure;

    public LabelCellEditorLocator(IFigure figure) {
        setLabel(figure);
    }

    @Override
    public void relocate(CellEditor celleditor) {
        var text = (Text) celleditor.getControl();
        var rect = labelFigure.getClientArea();
        labelFigure.translateToAbsolute(rect);
        var trim = text.computeTrim(0, 0, 0, 0);
        rect.translate(trim.x, trim.y);
        rect.width += trim.width;
        rect.height += trim.height;
        var fontHeight = FigureUtilities.getTextExtents("H", labelFigure.getFont()).height;
        if (fontHeight > rect.height) {
            rect.height = fontHeight;
        }
        text.setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Returns the stickyNote figure.
     */
    protected IFigure getLabel() {
        return labelFigure;
    }

    /**
     * Sets the Sticky note figure.
     * 
     * @param stickyNote
     *            The stickyNote to set
     */
    protected void setLabel(IFigure stickyNote) {
        labelFigure = stickyNote;
    }
}
