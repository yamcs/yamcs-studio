/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.figureparts;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;

/**
 * A label whose background could be set with alpha. Alpha may range from 0 to 255. A value of 0 is completely
 * transparent
 */
public class AlphaLabel extends Label {

    private int alpha = 100;

    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    protected void paintFigure(Graphics graphics) {
        graphics.pushState();
        graphics.setAlpha(alpha);
        graphics.fillRectangle(bounds);
        graphics.popState();
        super.paintFigure(graphics);
    }

    /**
     * @param alpha
     *            the alpha to set
     */
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
