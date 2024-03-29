/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.figures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.csstudio.opibuilder.widgets.FigureTransparencyHelper;
import org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel;
import org.csstudio.swt.widgets.figures.AbstractBoolControlFigure;
import org.csstudio.swt.widgets.symbol.SymbolImage;
import org.csstudio.swt.widgets.symbol.SymbolImageFactory;
import org.csstudio.swt.widgets.symbol.SymbolImageListener;
import org.csstudio.swt.widgets.symbol.SymbolImageProperties;
import org.csstudio.swt.widgets.symbol.util.IImageListener;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

public class ImageBoolButtonFigure extends AbstractBoolControlFigure implements SymbolImageListener {

    /**
     * The image itself.
     */
    private SymbolImage onImage;
    private SymbolImage offImage;
    private SymbolImageProperties symbolProperties;

    private boolean indicatorMode = false;

    private String onImagePath;
    private String offImagePath;

    private AtomicInteger remainingImagesToLoad = new AtomicInteger(0);

    private boolean animationDisabled = false;

    private IImageListener imageListener;

    private AbstractBoolWidgetModel model;

    public ImageBoolButtonFigure() {
        this(false);
    }

    @Override
    protected void updateBoolValue() {
        super.updateBoolValue();
        if (onImage == null || offImage == null) {
            return;
        }
        if (booleanValue) {
            onImage.setVisible(true);
            offImage.setVisible(false);
        } else {
            onImage.setVisible(false);
            offImage.setVisible(true);
        }
        sizeChanged();
    }

    public ImageBoolButtonFigure(boolean indicatorMode) {
        this.indicatorMode = indicatorMode;
        if (!indicatorMode) {
            addMouseListener(buttonPresser);
        }
        add(boolLabel);
    }

    /**
     * Return the current displayed image. If null, returns an empty image.
     */
    public SymbolImage getCurrentImage() {
        var image = booleanValue ? onImage : offImage;
        if (image == null) {
            image = SymbolImageFactory.createEmptyImage(true);
        }
        return image;
    }

    /**
     * Return all mapped images.
     */
    public Collection<SymbolImage> getAllImages() {
        Collection<SymbolImage> list = new ArrayList<>();
        if (onImage != null) {
            list.add(onImage);
        }
        if (offImage != null) {
            list.add(offImage);
        }
        return list;
    }

    /**
     * Dispose the image resources used by this figure.
     */
    public void dispose() {
        if (onImage != null && !onImage.isDisposed()) {
            onImage.dispose();
            onImage = null;
        }
        if (offImage != null && !offImage.isDisposed()) {
            offImage.dispose();
            offImage = null;
        }
    }

    public void setSymbolProperties(SymbolImageProperties symbolProperties, AbstractBoolWidgetModel model) {
        this.symbolProperties = symbolProperties;
        this.model = model;
    }

    public Dimension getAutoSizedDimension() {
        var temp = booleanValue ? onImage : offImage;
        if (temp != null) {
            var dim = temp.getAutoSizedDimension();
            if (dim == null) {
                return null;
            }
            return new Dimension(dim.width + getInsets().left + getInsets().right,
                    dim.height + getInsets().bottom + getInsets().top);
        }
        return null;
    }

    @Override
    public void setBorder(Border b) {
        super.setBorder(b);
        sizeChanged();
    }

    public boolean isLoadingImage() {
        return remainingImagesToLoad.get() > 0;
    }

    public void decrementLoadingCounter() {
        remainingImagesToLoad.decrementAndGet();
    }

    public void incrementLoadingCounter() {
        remainingImagesToLoad.incrementAndGet();
    }

    @Override
    protected void layout() {
        var clientArea = getClientArea().getCopy();
        if (boolLabel.isVisible()) {
            var labelSize = boolLabel.getPreferredSize();
            boolLabel.setBounds(new Rectangle(
                    getLabelLocation(clientArea.x + clientArea.width / 2 - labelSize.width / 2,
                            clientArea.y + clientArea.height / 2 - labelSize.height / 2),
                    new Dimension(labelSize.width, labelSize.height)));
        }
        super.layout();
    }

    @Override
    protected void paintClientArea(Graphics graphics) {
        if (isLoadingImage()) {
            return;
        }
        var clientArea = getClientArea();
        if (clientArea.width <= 0 || clientArea.height <= 0) {
            return;
        }
        if (!isEnabled() && !indicatorMode) {
            graphics.setAlpha(DISABLED_ALPHA);
            graphics.setBackgroundColor(DISABLE_COLOR);
            graphics.fillRectangle(bounds);
        }
        var symbolImage = getCurrentImage();
        symbolImage.setBounds(clientArea);
        symbolImage.setAbsoluteScale(graphics.getAbsoluteScale());
        if (!isEnabled() && !indicatorMode) {
            symbolImage.setBackgroundColor(DISABLE_COLOR);
        } else {
            FigureTransparencyHelper.setBackground(symbolImage, getBackgroundColor(), model);
        }
        symbolImage.paintFigure(graphics);
        super.paintClientArea(graphics);
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        if (!indicatorMode && runMode && value) {
            setCursor(Cursors.HAND);
        }
    }

    public void setOffImagePath(String offImagePath) {
        this.offImagePath = offImagePath;
        if (offImage != null) {
            offImage.dispose();
            offImage = null;
        }
        if (offImagePath != null && !offImagePath.isEmpty()) {
            incrementLoadingCounter();
        }
        offImage = SymbolImageFactory.asynCreateSymbolImage(this.offImagePath, true, symbolProperties, this);
    }

    public void setOnImagePath(String onImagePath) {
        this.onImagePath = onImagePath;
        if (onImage != null) {
            onImage.dispose();
            onImage = null;
        }
        if (onImagePath != null && !onImagePath.isEmpty()) {
            incrementLoadingCounter();
        }
        onImage = SymbolImageFactory.asynCreateSymbolImage(this.onImagePath, true, symbolProperties, this);
    }

    @Override
    public void setRunMode(boolean runMode) {
        super.setRunMode(runMode);
        setCursor((runMode && !indicatorMode) ? Cursors.HAND : null);
    }

    public void setStretch(boolean strech) {
        if (symbolProperties != null) {
            symbolProperties.setStretch(strech);
        }
        for (var si : getAllImages()) {
            si.setStretch(strech);
        }
        repaint();
    }

    @Override
    public void setValue(double value) {
        super.setValue(value);
        revalidate();
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        if (symbolProperties != null) {
            symbolProperties.setBackgroundColor(backgroundColor);
        }
        for (var si : getAllImages()) {
            si.setBackgroundColor(backgroundColor);
        }
        repaint();
    }

    // ************************************************************
    // Animated images
    // ************************************************************

    /**
     * @return the animationDisabled
     */
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
        for (var asi : getAllImages()) {
            asi.setAnimationDisabled(stop);
        }
        repaint();
    }

    public void setAlignedToNearestSecond(boolean aligned) {
        if (symbolProperties != null) {
            symbolProperties.setAlignedToNearestSecond(aligned);
        }
        for (var asi : getAllImages()) {
            asi.setAlignedToNearestSecond(aligned);
        }
        repaint();
    }

    // ************************************************************
    // Symbol Image Listener
    // ************************************************************

    public void setImageLoadedListener(IImageListener listener) {
        imageListener = listener;
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
