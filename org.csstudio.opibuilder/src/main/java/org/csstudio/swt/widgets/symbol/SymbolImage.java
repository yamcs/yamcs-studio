/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.symbol;

import org.csstudio.swt.widgets.symbol.util.PermutationMatrix;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;

public interface SymbolImage {

    Color DISABLE_COLOR = CustomMediaFactory.getInstance().getColor(CustomMediaFactory.COLOR_GRAY);

    /** The alpha (0 is transparency and 255 is opaque) for disabled paint */
    int DISABLED_ALPHA = 100;

    void setImagePath(String imagePath);

    String getImagePath();

    ImageData getOriginalImageData();

    /**
     * Dispose the resource used by this figure
     */
    void dispose();

    void setVisible(boolean visible);

    boolean isDisposed();

    boolean isEditMode();

    boolean isEmpty();

    void setCurrentColor(Color newColor);

    void setColorToChange(Color newColor);

    void setBackgroundColor(Color newColor);

    /**
     * The main drawing routine.
     *
     * @param gfx
     *            The {@link Graphics} to use
     */
    void paintFigure(Graphics gfx);

    void setBounds(Rectangle newArea);

    void setAbsoluteScale(double newScale);

    /**
     * Resizes the image.
     */
    void resizeImage();

    /**
     * Automatically adjust the widget bounds to fit the size of the static image
     *
     * @param autoSize
     */
    void setAutoSize(boolean autoSize);

    /**
     * Set the stretch state for the image.
     *
     * @param newval
     *            true, if it should be stretched, false otherwise)
     */
    void setStretch(boolean newval);

    /**
     * Get the auto sized widget dimension according to the static image size.
     *
     * @return The auto sized widget dimension.
     */
    Dimension getAutoSizedDimension();

    /**
     * Sets the amount of pixels, which are cropped from the left.
     */
    void setLeftCrop(int newval);

    /**
     * Sets the amount of pixels, which are cropped from the right.
     */
    void setRightCrop(int newval);

    /**
     * Sets the amount of pixels, which are cropped from the bottom.
     */
    void setBottomCrop(int newval);

    /**
     * Sets the amount of pixels, which are cropped from the top.
     */
    void setTopCrop(int newval);

    void setPermutationMatrix(PermutationMatrix permutationMatrix);

    PermutationMatrix getPermutationMatrix();

    void setAnimationDisabled(boolean stop);

    void setListener(SymbolImageListener listener);

    void syncLoadImage();

    void asyncLoadImage();

    void setAlignedToNearestSecond(boolean aligned);
}
