/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.swt.widgets.symbol.SymbolImage;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;

public class FigureTransparencyHelper {
    /** property key */
    public static final String PROP_TRANSPARENCY = "transparency";

    public static void setBackground(SymbolImage symbolImage, Color backgroundColor, AbstractWidgetModel model) {
        var transparency = isTransparency(model);
        if (transparency) {
            symbolImage.setBackgroundColor(null);
        } else {
            symbolImage.setBackgroundColor(backgroundColor);
        }
    }

    public static void addProperty(AbstractWidgetModel model) {
        model.addProperty(new BooleanProperty(PROP_TRANSPARENCY, "Transparent", WidgetPropertyCategory.Display, false));
    }

    public static void addHandler(AbstractBaseEditPart editPart, IFigure figure) {
        editPart.setPropertyChangeHandler(FigureTransparencyHelper.PROP_TRANSPARENCY,
                (oldValue, newValue, figure1) -> {
                    figure1.repaint();
                    return true;
                });
    }

    public static boolean isTransparency(AbstractWidgetModel model) {
        if (model.getProperty(FigureTransparencyHelper.PROP_TRANSPARENCY) != null) {
            return (Boolean) model.getProperty(FigureTransparencyHelper.PROP_TRANSPARENCY).getPropertyValue();
        }
        return false;
    }
}
