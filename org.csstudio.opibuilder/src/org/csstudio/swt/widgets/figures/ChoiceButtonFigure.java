/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Toggle;
import org.eclipse.draw2d.ToggleButton;

/**
 * The figure of choice button.
 */
public class ChoiceButtonFigure extends AbstractChoiceFigure {

    public ChoiceButtonFigure(boolean runMode) {
        super(runMode);
        selectedColor = ColorConstants.buttonLightest;
    }

    @Override
    protected Toggle createToggle(String text) {
        return new ColorToggleButton(text);
    }

    class ColorToggleButton extends ToggleButton {

        /**
         * Constructs a ToggleButton with the passed string as its text.
         *
         * @param text
         *            the text to be displayed on the button
         * @since 2.0
         */
        public ColorToggleButton(String text) {
            super(text, null);
            if (runMode) {
                setCursor(Cursors.HAND);
            }
        }

        /**
         * Draws a checkered pattern to emulate a toggle button that is in the selected state.
         * 
         * @param graphics
         *            The Graphics object used to paint
         */
        @Override
        protected void fillCheckeredRectangle(Graphics graphics) {
            graphics.setBackgroundColor(selectedColor);
            graphics.setForegroundColor(ColorConstants.buttonLightest);
            graphics.fillRectangle(getClientArea());

            graphics.restoreState();
        }
    }

}
