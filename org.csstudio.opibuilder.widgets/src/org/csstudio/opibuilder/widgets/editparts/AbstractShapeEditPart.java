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

import org.csstudio.opibuilder.datadefinition.LineStyle;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.AbstractShapeModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.SWT;

/**
 * Abstract EditPart controller for the shape widgets.
 */
public abstract class AbstractShapeEditPart extends AbstractPVWidgetEditPart {

    @Override
    public AbstractShapeModel getWidgetModel() {
        return (AbstractShapeModel) getModel();
    }

    @Override
    protected IFigure createFigure() {
        var shape = (Shape) super.createFigure();
        var model = getWidgetModel();
        shape.setOutline(model.getLineWidth() != 0);
        shape.setLineWidth(model.getLineWidth());
        shape.setLineStyle(model.getLineStyle());

        if (model.getAlpha() < 255) {
            shape.setAlpha(model.getAlpha());
        } else {
            shape.setAlpha(null);
        }
        shape.setAntialias(model.isAntiAlias() ? SWT.ON : null);
        return shape;
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        // line width
        IWidgetPropertyChangeHandler lineWidthHandler = (oldValue, newValue, refreshableFigure) -> {
            var shape = (Shape) refreshableFigure;
            if (((Integer) newValue).equals(0)) {
                shape.setOutline(false);
            } else {
                shape.setOutline(true);
                shape.setLineWidth((Integer) newValue);
            }

            return true;
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_LINE_WIDTH, lineWidthHandler);

        // line style
        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            var shape = (Shape) refreshableFigure;
            shape.setLineStyle(LineStyle.values()[(Integer) newValue].getStyle());
            return true;
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_LINE_STYLE, handler);

        handler = (oldValue, newValue, figure) -> {
            ((Shape) figure).setAntialias(((Boolean) newValue) ? SWT.ON : null);
            return false;
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_ANTIALIAS, handler);

        handler = (oldValue, newValue, figure) -> {
            if ((Integer) newValue < 255) {
                ((Shape) figure).setAlpha((Integer) newValue);
            } else {
                ((Shape) figure).setAlpha(null);
            }
            return false;
        };
        setPropertyChangeHandler(AbstractShapeModel.PROP_ALPHA, handler);

    }

}
