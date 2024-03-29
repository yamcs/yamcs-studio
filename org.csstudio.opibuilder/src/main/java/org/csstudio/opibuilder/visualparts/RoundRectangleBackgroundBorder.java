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

import org.eclipse.draw2d.AbstractBackground;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * A special border which fills the background with a round rectangle.
 */
public class RoundRectangleBackgroundBorder extends AbstractBackground {

    private static final int ARC_SIZE = 8;

    private int lineWidth;

    private Color backgroundColor;

    public RoundRectangleBackgroundBorder(Color backgroundColor, int lineWidth) {
        this.backgroundColor = backgroundColor;
        this.lineWidth = lineWidth;
    }

    @Override
    public Insets getInsets(IFigure figure) {
        return new Insets(lineWidth > 0 ? 2 * lineWidth : 2);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public void paint(IFigure figure, Graphics graphics, Insets insets) {
        if (lineWidth <= 0) {
            return;
        }
        graphics.pushState();
        graphics.setAntialias(SWT.ON);
        tempRect.setBounds(getPaintRectangle(figure, insets));
        if ((lineWidth & 1) == 1) {
            tempRect.width--;
            tempRect.height--;
        }
        tempRect.shrink(lineWidth / 2, lineWidth / 2);
        graphics.setLineWidth(lineWidth);
        graphics.setForegroundColor(backgroundColor);// ColorConstants.buttonDarker);
        graphics.drawRoundRectangle(tempRect, ARC_SIZE, ARC_SIZE);
        graphics.popState();
    }

    @Override
    public void paintBackground(IFigure figure, Graphics graphics, Insets insets) {
        super.paintBackground(figure, graphics, insets);
        graphics.pushState();
        graphics.setAntialias(SWT.ON);
        tempRect.setBounds(getPaintRectangle(figure, insets));
        // if(backgroundColor != null)
        // graphics.setBackgroundColor(backgroundColor);
        tempRect.shrink(lineWidth <= 1 ? 1 : lineWidth / 2, lineWidth <= 1 ? 1 : lineWidth / 2);
        graphics.fillRoundRectangle(tempRect, ARC_SIZE, ARC_SIZE);
        graphics.popState();
    }
}
