/*******************************************************************************
 * Copyright (c) 2025 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.figures;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

/**
 * Figure for the combo widget.
 */
public class ComboFigure extends Label implements ITextFigure {

    public static final int ICON_WIDTH = 15;

    private static final Image downArrow = CustomMediaFactory.getInstance().getImageFromPlugin(
            OPIBuilderPlugin.PLUGIN_ID,
            "icons/downArrow.png");

    public ComboFigure() {
        super();
        setIcon(downArrow);
        setLabelAlignment(PositionConstants.RIGHT);
        setTextPlacement(PositionConstants.WEST);
        setOpaque(true);
        updateLayout();
    }

    @Override
    public void setText(String s) {
        super.setText(s);
        updateLayout();
    }

    @Override
    public void setBounds(Rectangle rect) {
        super.setBounds(rect);
        updateLayout();
    }

    /**
     * Layout the contents of the widget so that, if an icon is displayed, it is right aligned and the text remains
     * centred.
     */
    private void updateLayout() {
        /*
         * In Draw2d there appears to be no way adding a right aligned arrow to a
         * label. We fake the effect here by checking the widths of the text and
         * of the label then adding an appropriate gap so that the text looks as
         * if it has been centred.
         */
        setIconTextGap((getBounds().width - getTextBounds().width - ICON_WIDTH) / 2);
    }
}
