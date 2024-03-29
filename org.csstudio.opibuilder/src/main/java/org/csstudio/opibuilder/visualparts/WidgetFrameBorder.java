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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.CompoundBorder;
import org.eclipse.draw2d.LabeledBorder;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.draw2d.TitleBarBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

/**
 * Provides for a frame-like border which contains a title bar for holding the title of a Figure.
 */
public class WidgetFrameBorder extends CompoundBorder implements LabeledBorder {

    {
        createBorders();
    }

    /**
     * Constructs a FrameBorder with its label set to the name of the {@link TitleBarBorder} class.
     */
    public WidgetFrameBorder() {
    }

    /**
     * Constructs a FrameBorder with the title set to the passed String.
     *
     * @param label
     *            label or title of the frame.
     */
    public WidgetFrameBorder(String label) {
        setLabel(label);
    }

    /**
     * Creates the necessary borders for this FrameBorder. The inner border is a {@link TitleBarBorder}. The outer
     * border is a {@link SchemeBorder}.
     */
    protected void createBorders() {
        inner = new TitleBarBorder();
        outer = new VersatileLineBorder(ColorConstants.black, 1, SWT.LINE_SOLID);
    }

    /**
     * Returns the inner border of this FrameBorder, which contains the label for the FrameBorder.
     *
     * @return the border holding the label.
     */
    protected LabeledBorder getLabeledBorder() {
        return (LabeledBorder) inner;
    }

    /**
     * @return the label for this border
     */
    @Override
    public String getLabel() {
        return getLabeledBorder().getLabel();
    }

    /**
     * Sets the label for this border.
     */
    @Override
    public void setLabel(String label) {
        getLabeledBorder().setLabel(label);
    }

    /**
     * Sets the font for this border's label.
     */
    @Override
    public void setFont(Font font) {
        getLabeledBorder().setFont(font);
    }
}
