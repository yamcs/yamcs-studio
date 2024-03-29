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
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Main class for Symbol Image display.
 */
public abstract class AbstractSymbolImage implements SymbolImage {

    private boolean runMode;

    protected double scale = 1.0;
    protected Rectangle bounds;

    protected String imagePath;

    protected Image image;
    protected ImageData imageData;
    protected ImageData originalImageData;

    private SymbolImageListener listener;

    protected Color currentColor;
    protected Color colorToChange;
    protected Color backgroundColor;

    protected int leftCrop = 0;
    protected int rightCrop = 0;
    protected int bottomCrop = 0;
    protected int topCrop = 0;
    protected boolean stretch = false;
    protected boolean autoSize = true;
    protected PermutationMatrix oldPermutationMatrix = null;
    protected PermutationMatrix permutationMatrix = PermutationMatrix.generateIdentityMatrix();

    protected boolean animationDisabled = false;
    protected boolean alignedToNearestSecond = false;
    protected boolean visible = true;
    protected boolean disposed = false;

    public AbstractSymbolImage(SymbolImageProperties sip, boolean runMode) {
        this.runMode = runMode;
        currentColor = new Color(Display.getCurrent(), new RGB(0, 0, 0));
        colorToChange = new Color(Display.getCurrent(), new RGB(0, 0, 0));
        fillProperties(sip);
    }

    private void fillProperties(SymbolImageProperties sip) {
        if (sip == null) {
            return;
        }
        topCrop = sip.getTopCrop();
        bottomCrop = sip.getBottomCrop();
        leftCrop = sip.getLeftCrop();
        rightCrop = sip.getRightCrop();
        permutationMatrix = sip.getMatrix();
        stretch = sip.isStretch();
        autoSize = sip.isAutoSize();
        animationDisabled = sip.isAnimationDisabled();
        alignedToNearestSecond = sip.isAlignedToNearestSecond();
        backgroundColor = sip.getBackgroundColor() == null ? new Color(Display.getCurrent(), new RGB(0, 0, 0))
                : sip.getBackgroundColor();
        colorToChange = sip.getColorToChange() == null ? new Color(Display.getCurrent(), new RGB(0, 0, 0))
                : sip.getColorToChange();
    }

    @Override
    public String getImagePath() {
        return imagePath;
    }

    @Override
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public ImageData getOriginalImageData() {
        return originalImageData;
    }

    @Override
    public void dispose() {
        disposed = true;
        if (image != null && !image.isDisposed()) {
            image.dispose();
            image = null;
        }
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public boolean isEditMode() {
        return !runMode;
    }

    @Override
    public boolean isEmpty() {
        return originalImageData == null;
    }

    public abstract void resetData();

    @Override
    public void setCurrentColor(Color newColor) {
        if (isEditMode()) {
            return;
        }
        if (newColor == null || (currentColor != null && currentColor.equals(newColor))) {
            return;
        }
        currentColor = newColor;
        resetData();
    }

    @Override
    public void setColorToChange(Color newColor) {
        if (isEditMode()) {
            return;
        }
        if (newColor == null || (colorToChange != null && colorToChange.equals(newColor))) {
            return;
        }
        colorToChange = newColor;
        resetData();
    }

    @Override
    public void setBackgroundColor(Color newColor) {
        if ((backgroundColor == null && newColor == null)
                || (backgroundColor != null && backgroundColor.equals(newColor))) {
            return;
        }
        backgroundColor = newColor;
    }

    @Override
    public abstract Dimension getAutoSizedDimension();

    @Override
    public void resizeImage() {
        resetData();
    }

    @Override
    public void setBounds(Rectangle newBounds) {
        if (newBounds == null || newBounds.equals(bounds) || newBounds.width <= 0 || newBounds.height <= 0) {
            return;
        }
        if (bounds == null) {
            bounds = newBounds.getCopy();
            resizeImage();
            return;
        }
        var oldBounds = bounds.getCopy();
        bounds = newBounds.getCopy();
        if (autoSize) {
            var dim = getAutoSizedDimension();
            if (dim == null) {
                return;
            }
            if (newBounds.width != dim.width || newBounds.height != dim.height) {
                resizeImage();
            }
        } else {
            if (oldBounds.width != newBounds.width || oldBounds.height != newBounds.height) {
                resizeImage();
            }
        }
    }

    @Override
    public void setAbsoluteScale(double newScale) {
        if (scale == newScale) {
            return;
        }
        scale = newScale;
    }

    @Override
    public void setAutoSize(boolean autoSize) {
        if (this.autoSize == autoSize) {
            return;
        }
        this.autoSize = autoSize;
        if (!stretch && autoSize) {
            resizeImage();
        }
    }

    @Override
    public void setStretch(boolean newval) {
        if (stretch == newval) {
            return;
        }
        stretch = newval;
        resizeImage();
    }

    @Override
    public void setLeftCrop(int newval) {
        if (leftCrop == newval || newval < 0) {
            return;
        }
        leftCrop = newval;
        resizeImage();
    }

    @Override
    public void setRightCrop(int newval) {
        if (rightCrop == newval || newval < 0) {
            return;
        }
        rightCrop = newval;
        resizeImage();
    }

    @Override
    public void setBottomCrop(int newval) {
        if (bottomCrop == newval || newval < 0) {
            return;
        }
        bottomCrop = newval;
        resizeImage();
    }

    @Override
    public void setTopCrop(int newval) {
        if (topCrop == newval || newval < 0) {
            return;
        }
        topCrop = newval;
        resizeImage();
    }

    @Override
    public void setPermutationMatrix(PermutationMatrix permutationMatrix) {
        oldPermutationMatrix = this.permutationMatrix;
        this.permutationMatrix = permutationMatrix;
        if (permutationMatrix == null
                || (oldPermutationMatrix != null && oldPermutationMatrix.equals(permutationMatrix))) {
            return;
        }
        resizeImage();
    }

    @Override
    public PermutationMatrix getPermutationMatrix() {
        return permutationMatrix;
    }

    @Override
    public void setAnimationDisabled(boolean stop) {
        if (animationDisabled == stop) {
            return;
        }
        animationDisabled = stop;
    }

    @Override
    public void setAlignedToNearestSecond(boolean aligned) {
        alignedToNearestSecond = aligned;
    }

    @Override
    public void setListener(SymbolImageListener listener) {
        this.listener = listener;
    }

    protected void fireSymbolImageLoaded() {
        if (listener != null) {
            listener.symbolImageLoaded();
        }
    }

    protected void fireSizeChanged() {
        if (listener != null && visible) {
            listener.sizeChanged();
        }
    }

    protected void repaint() {
        if (listener != null && visible) {
            listener.repaintRequested();
        }
    }
}
