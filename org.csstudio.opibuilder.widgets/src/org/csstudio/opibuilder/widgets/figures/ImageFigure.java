/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.figures;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.widgets.FigureTransparencyHelper;
import org.csstudio.swt.widgets.introspection.DefaultWidgetIntrospector;
import org.csstudio.swt.widgets.introspection.Introspectable;
import org.csstudio.swt.widgets.symbol.SymbolImage;
import org.csstudio.swt.widgets.symbol.SymbolImageFactory;
import org.csstudio.swt.widgets.symbol.SymbolImageListener;
import org.csstudio.swt.widgets.symbol.SymbolImageProperties;
import org.csstudio.swt.widgets.symbol.util.IImageListener;
import org.csstudio.swt.widgets.symbol.util.ImageUtils;
import org.csstudio.swt.widgets.symbol.util.PermutationMatrix;
import org.csstudio.swt.widgets.util.TextPainter;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;

/**
 * An image figure.
 */
public final class ImageFigure extends Figure implements Introspectable, SymbolImageListener {

    private String filePath;

    private SymbolImage image;
    private SymbolImageProperties symbolProperties;

    private AtomicInteger remainingImagesToLoad = new AtomicInteger(0);

    private boolean animationDisabled = false;

    private IImageListener imageListener;

    private AbstractWidgetModel model;

    /**
     * dispose the resources used by this figure
     */
    public void dispose() {
        if (image != null && !image.isDisposed()) {
            image.dispose();
            image = null;
        }
    }

    public void setSymbolProperties(SymbolImageProperties symbolProperties, AbstractWidgetModel model) {
        this.symbolProperties = symbolProperties;
        this.model = model;
    }

    public void setFilePath(String newval) {
        if (newval == null) {
            return;
        }
        this.filePath = newval;
        if (image != null) {
            image.dispose();
            image = null;
        }
        if (filePath != null && !filePath.isEmpty()) {
            remainingImagesToLoad.incrementAndGet();
        }
        image = SymbolImageFactory.asynCreateSymbolImage(filePath, true, symbolProperties, this);
    }

    public boolean isLoadingImage() {
        return remainingImagesToLoad.get() > 0;
    }

    public void decrementLoadingCounter() {
        remainingImagesToLoad.decrementAndGet();
    }

    @Override
    protected void paintClientArea(Graphics gfx) {
        if (isLoadingImage()) {
            return;
        }
        ImageUtils.crop(bounds, this.getInsets());
        if (bounds.width <= 0 || bounds.height <= 0) {
            return;
        }
        if (image == null || image.isEmpty() || image.getImagePath() == null) {
            String msg = "Could not load image\n" + filePath;
            if (filePath == null || filePath.isEmpty()) {
                msg = "No file specified";
            }

            gfx.setBackgroundColor(getBackgroundColor());
            gfx.setForegroundColor(getForegroundColor());
            gfx.fillRectangle(bounds);
            gfx.translate(bounds.getLocation());
            TextPainter.drawText(gfx, msg, bounds.width / 2, bounds.height / 2, TextPainter.CENTER);
            return;
        }
        image.setBounds(bounds);
        image.setAbsoluteScale(gfx.getAbsoluteScale());
        image.setBackgroundColor(getBackgroundColor());
        FigureTransparencyHelper.setBackground(image, getBackgroundColor(), model);
        image.paintFigure(gfx);
        super.paintClientArea(gfx);
    }

    public void resizeImage() {
        var bounds = getBounds().getCopy();
        if (image != null) {
            image.setBounds(bounds);
        }
        repaint();
    }

    public void setAutoSize(boolean autoSize) {
        if (symbolProperties != null) {
            symbolProperties.setAutoSize(autoSize);
        }
        if (image != null) {
            image.setAutoSize(autoSize);
        }
        repaint();
    }

    /**
     * @return the auto sized widget dimension according to the static imageSize
     */
    public Dimension getAutoSizedDimension() {
        // Widget dimension = Symbol Image + insets
        if (image == null) {
            return null;
        }
        var dim = image.getAutoSizedDimension();
        if (dim == null) {
            return null;
        }
        return new Dimension(dim.width + getInsets().getWidth(), dim.height + getInsets().getHeight());
    }

    public void setLeftCrop(int newval) {
        if (symbolProperties != null) {
            symbolProperties.setLeftCrop(newval);
        }
        if (image != null) {
            image.setLeftCrop(newval);
        }
        repaint();
    }

    public void setRightCrop(int newval) {
        if (symbolProperties != null) {
            symbolProperties.setRightCrop(newval);
        }
        if (image != null) {
            image.setRightCrop(newval);
        }
        repaint();
    }

    public void setBottomCrop(int newval) {
        if (symbolProperties != null) {
            symbolProperties.setBottomCrop(newval);
        }
        if (image != null) {
            image.setBottomCrop(newval);
        }
        repaint();
    }

    public void setTopCrop(int newval) {
        if (symbolProperties != null) {
            symbolProperties.setTopCrop(newval);
        }
        if (image != null) {
            image.setTopCrop(newval);
        }
        repaint();
    }

    public void setStretch(boolean newval) {
        if (symbolProperties != null) {
            symbolProperties.setStretch(newval);
        }
        if (image != null) {
            image.setStretch(newval);
        }
        repaint();
    }

    public void setPermutationMatrix(PermutationMatrix permutationMatrix) {
        if (symbolProperties != null) {
            symbolProperties.setMatrix(permutationMatrix);
        }
        if (image != null) {
            image.setPermutationMatrix(permutationMatrix);
        }
        repaint();
    }

    public PermutationMatrix getPermutationMatrix() {
        if (image == null) {
            return PermutationMatrix.generateIdentityMatrix();
        }
        return image.getPermutationMatrix();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (image != null) {
            image.setVisible(visible);
        }
    }

    /**
     * We want to have local coordinates here.
     *
     * @return True if here should used local coordinates
     */
    @Override
    protected boolean useLocalCoordinates() {
        return true;
    }

    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new DefaultWidgetIntrospector().getBeanInfo(this.getClass());
    }

    public boolean isAnimationDisabled() {
        return animationDisabled;
    }

    public void setAnimationDisabled(boolean stop) {
        if (animationDisabled == stop) {
            return;
        }
        animationDisabled = stop;
        if (symbolProperties != null) {
            symbolProperties.setAnimationDisabled(stop);
        }
        if (image != null) {
            image.setAnimationDisabled(stop);
        }
        repaint();
    }

    public void setAlignedToNearestSecond(boolean aligned) {
        if (symbolProperties != null) {
            symbolProperties.setAlignedToNearestSecond(aligned);
        }
        if (image != null) {
            image.setAlignedToNearestSecond(aligned);
        }
        repaint();
    }

    public void setImageLoadedListener(IImageListener listener) {
        this.imageListener = listener;
    }

    @Override
    public void symbolImageLoaded() {
        decrementLoadingCounter();
        sizeChanged();
        revalidate();
        repaint();
    }

    @Override
    public void repaintRequested() {
        repaint();
    }

    @Override
    public void sizeChanged() {
        if (imageListener != null) {
            imageListener.imageResized(this);
        }
    }
}
