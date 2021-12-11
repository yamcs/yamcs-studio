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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.swt.widgets.symbol.util.ImageUtils;
import org.csstudio.swt.widgets.util.AbstractInputStreamRunnable;
import org.csstudio.swt.widgets.util.ResourceUtil;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public class PNGSymbolImage extends AbstractSymbolImage {

    private static final Logger log = Logger.getLogger(PNGSymbolImage.class.getName());

    private Dimension imgDimension = null;

    private boolean loadingImage = false;

    public PNGSymbolImage(SymbolImageProperties sip, boolean runMode) {
        super(sip, runMode);
    }

    public void setOriginalImageData(ImageData originalImageData) {
        this.originalImageData = originalImageData;
        resetData();
    }

    @Override
    public void paintFigure(Graphics gfx) {
        if (disposed || loadingImage || originalImageData == null) {
            return;
        }
        // Generate Data
        if (imageData == null) {
            generatePNGData();
            if (image != null && !image.isDisposed()) {
                image.dispose();
                image = null;
            }
        }
        // Create image
        if (image == null) {
            if (imageData == null) {
                return;
            }
            image = new Image(Display.getDefault(), imageData);
        }
        // Calculate areas
        if (bounds == null || imgDimension == null) {
            return;
        }
        var srcArea = new Rectangle(leftCrop, topCrop, imgDimension.width, imgDimension.height);
        var destArea = new Rectangle(bounds.x, bounds.y, imgDimension.width, imgDimension.height);
        if (backgroundColor != null) {
            gfx.setBackgroundColor(backgroundColor);
            gfx.fillRectangle(destArea);
        }
        // Draw graphic image
        if (image != null) {
            gfx.drawImage(image, srcArea, destArea);
        }
    }

    @Override
    public void resetData() {
        imageData = null;
    }

    private void generatePNGData() {
        if (disposed || originalImageData == null) {
            return;
        }

        imageData = (ImageData) originalImageData.clone();
        if (!colorToChange.equals(currentColor)) {
            imageData = ImageUtils.changeImageColor(currentColor, imageData);
        }
        imageData = ImageUtils.applyMatrix(imageData, permutationMatrix);
        if (stretch && bounds != null) {
            imageData = imageData.scaledTo(bounds.width + leftCrop + rightCrop, bounds.height + topCrop + bottomCrop);
        }
        var imgWidth = imageData.width;
        var imgHeight = imageData.height;

        // Avoid negative number
        topCrop = topCrop > imgHeight ? 0 : topCrop;
        leftCrop = leftCrop > imgWidth ? 0 : leftCrop;
        bottomCrop = (imgHeight - topCrop - bottomCrop) < 0 ? 0 : bottomCrop;
        rightCrop = (imgWidth - leftCrop - rightCrop) < 0 ? 0 : rightCrop;

        // Calculate areas
        var cropedWidth = imageData.width - leftCrop - rightCrop;
        var cropedHeight = imageData.height - bottomCrop - topCrop;
        var newImgDimension = new Dimension(cropedWidth, cropedHeight);
        if (imgDimension == null || newImgDimension.width != imgDimension.width
                || newImgDimension.height != imgDimension.height) {
            fireSizeChanged();
        }
        imgDimension = newImgDimension;
    }

    @Override
    public Dimension getAutoSizedDimension() {
        // if (imgDimension == null)
        // generatePNGData();
        return imgDimension;
    }

    @Override
    public void syncLoadImage() {
        if (imagePath == null) {
            return;
        }
        InputStream stream = null;
        Image tempImage = null;
        try {
            stream = ResourceUtil.pathToInputStream(imagePath);
            tempImage = new Image(Display.getDefault(), stream);
            var imgData = tempImage.getImageData();
            setOriginalImageData(imgData);
        } catch (Exception e) {
            log.log(Level.WARNING, "ERROR loading image " + imagePath, e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (tempImage != null && !tempImage.isDisposed()) {
                    tempImage.dispose();
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "ERROR closing image stream", e);
            }
        }
    }

    @Override
    public void asyncLoadImage() {
        if (imagePath == null) {
            return;
        }
        loadingImage = true;
        AbstractInputStreamRunnable uiTask = new AbstractInputStreamRunnable() {
            @Override
            public void runWithInputStream(InputStream stream) {
                synchronized (PNGSymbolImage.this) {
                    Image tempImage = null;
                    try {
                        tempImage = new Image(Display.getDefault(), stream);
                        var imgData = tempImage.getImageData();
                        setOriginalImageData(imgData);
                    } finally {
                        try {
                            stream.close();
                            if (tempImage != null && !tempImage.isDisposed()) {
                                tempImage.dispose();
                            }
                        } catch (IOException e) {
                            log.log(Level.WARNING, "ERROR closing image stream", e);
                        }
                    }
                    loadingImage = false;
                    Display.getDefault().syncExec(() -> fireSymbolImageLoaded());
                }
            }
        };
        ResourceUtil.pathToInputStreamInJob(imagePath, uiTask, "Loading Image...", e -> {
            loadingImage = false;
            Display.getDefault().syncExec(() -> fireSymbolImageLoaded());
            log.log(Level.WARNING, "ERROR loading image " + imagePath, e);
        });
    }
}
