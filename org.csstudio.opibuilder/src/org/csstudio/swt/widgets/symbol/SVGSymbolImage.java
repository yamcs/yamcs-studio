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

import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.csstudio.swt.widgets.util.AbstractInputStreamRunnable;
import org.csstudio.swt.widgets.util.ResourceUtil;
import org.csstudio.utility.batik.SVGHandler;
import org.csstudio.utility.batik.SVGUtils;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 * Manages display of {@link SVGDocument} using {@link SVGHandler}.
 */
public class SVGSymbolImage extends AbstractSymbolImage {

    private static final Logger log = Logger.getLogger(SVGSymbolImage.class.getName());

    private Dimension imgDimension = null;

    private boolean loadingImage = false;
    private boolean failedToLoadDocument = false;
    private SVGHandler svgHandler;
    private Document svgDocument;

    private boolean needRender = true;

    private Image animatedImage;

    /**
     * If <code>true</code>, the repaint was called by animated SVG thread.
     */
    private boolean repaintAnimated = false;

    public SVGSymbolImage(SymbolImageProperties sip, boolean runMode) {
        super(sip, runMode);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (svgHandler != null) {
            svgHandler.dispose();
            svgHandler = null;
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (svgHandler == null) {
            return;
        }
        if (visible) {
            svgHandler.resumeProcessing();
        } else {
            svgHandler.suspendProcessing();
        }
    }

    @Override
    public void paintFigure(Graphics gfx) {
        if (disposed || loadingImage || originalImageData == null) {
            return;
        }
        // Generate Data
        if (needRender) {
            generateSVGData();
            if (image != null && !image.isDisposed()) {
                image.dispose();
                image = null;
            }
            if (svgHandler != null && svgHandler.isDynamicDocument() && !animationDisabled) {
                svgHandler.startProcessing();
            }
        }
        // Create image
        if (image == null) {
            if (imageData == null) {
                return;
            }
            image = new Image(Display.getCurrent(), imageData);
        }
        // Calculate areas
        if (bounds == null || imgDimension == null) {
            return;
        }
        var cropedWidth = imageData.width - (int) Math.round(scale * (leftCrop + rightCrop));
        var cropedHeight = imageData.height - (int) Math.round(scale * (bottomCrop + topCrop));
        var srcArea = new Rectangle((int) Math.round(scale * leftCrop), (int) Math.round(scale * topCrop), cropedWidth,
                cropedHeight);
        var destArea = new Rectangle(bounds.x, bounds.y, imgDimension.width, imgDimension.height);
        if (backgroundColor != null) {
            gfx.setBackgroundColor(backgroundColor);
            gfx.fillRectangle(destArea);
        }
        // Draw graphic image
        if (repaintAnimated && animatedImage != null && !animatedImage.isDisposed()) {
            try {
                gfx.drawImage(animatedImage, srcArea, destArea);
            } catch (IllegalArgumentException e) { // Image disposed
            }
            repaintAnimated = false;
        } else if (image != null) {
            gfx.drawImage(image, srcArea, destArea);
        }
    }

    @Override
    public void resetData() {
        needRender = true;
    }

    private void generateSVGData() {
        if (disposed) {
            return;
        }
        // Load document if do not exist
        var document = getDocument();
        if (document == null) {
            return;
        }
        svgHandler.setColorToChange(colorToChange);
        if (!isEditMode() && !colorToChange.equals(currentColor)) {
            svgHandler.setColorToApply(currentColor);
        }
        if (permutationMatrix != null) {
            svgHandler.setTransformMatrix(permutationMatrix.getMatrix());
        }

        // Scale image
        var dims = svgHandler.getDocumentSize();
        var imgWidth = dims.width;
        var imgHeight = dims.height;
        if (stretch) {
            if (bounds != null && !bounds.equals(0, 0, 0, 0)) {
                imgWidth = bounds.width;
                imgHeight = bounds.height;
            }
        }
        // Avoid negative number
        topCrop = topCrop > imgHeight ? 0 : topCrop;
        leftCrop = leftCrop > imgWidth ? 0 : leftCrop;
        bottomCrop = (imgHeight - topCrop - bottomCrop) < 0 ? 0 : bottomCrop;
        rightCrop = (imgWidth - leftCrop - rightCrop) < 0 ? 0 : rightCrop;
        imgWidth = (int) Math.round(scale * (imgWidth + leftCrop + rightCrop));
        imgHeight = (int) Math.round(scale * (imgHeight + bottomCrop + topCrop));
        svgHandler.setCanvasSize(imgWidth, imgHeight);

        var awtImage = svgHandler.getOffScreen();
        if (awtImage != null) {
            imageData = SVGUtils.toSWT(Display.getCurrent(), awtImage);
        }

        // Calculate areas
        var cropedWidth = imgWidth - (int) Math.round(scale * (leftCrop + rightCrop));
        var cropedHeight = imgHeight - (int) Math.round(scale * (bottomCrop + topCrop));

        var newImgDimension = new Dimension((int) Math.round(cropedWidth / scale),
                (int) Math.round(cropedHeight / scale));
        if (imgDimension == null || newImgDimension.width != imgDimension.width
                || newImgDimension.height != imgDimension.height) {
            fireSizeChanged();
        }
        imgDimension = newImgDimension;
        needRender = false;
    }

    @Override
    public void setAbsoluteScale(double newScale) {
        var oldScale = scale;
        super.setAbsoluteScale(newScale);
        if (oldScale != newScale) {
            resizeImage();
        }
    }

    @Override
    public Dimension getAutoSizedDimension() {
        // if (imgDimension == null)
        // generateSVGData();
        return imgDimension;
    }

    @Override
    public void setAnimationDisabled(boolean stop) {
        super.setAnimationDisabled(stop);
        if (svgHandler == null) {
            return;
        }
        if (stop) {
            svgHandler.suspendProcessing();
            // display static image
            svgHandler.refreshContent();
            resetData();
        } else if (svgHandler.isDynamicDocument()) {
            svgHandler.startProcessing();
        }
    }

    @Override
    public void setAlignedToNearestSecond(boolean aligned) {
        super.setAlignedToNearestSecond(aligned);
        if (svgHandler == null) {
            return;
        }
        svgHandler.setAlignedToNearestSecond(aligned);
    }

    @Override
    public void syncLoadImage() {
        svgHandler = null;
        failedToLoadDocument = false;
        try {
            var inputStream = ResourceUtil.pathToInputStream(imagePath);
            loadDocument(inputStream);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error loading SVG image " + imagePath, e);
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
                synchronized (SVGSymbolImage.this) {
                    try {
                        loadDocument(stream);
                    } finally {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            log.log(Level.WARNING, "ERROR in closing SVG image stream ", e);
                        }
                    }
                    loadingImage = false;
                    Display.getCurrent().syncExec(() -> fireSymbolImageLoaded());
                }
            }
        };
        ResourceUtil.pathToInputStreamInJob(imagePath, uiTask, "Loading SVG Image...", exception -> {
            loadingImage = false;
            Display.getDefault().syncExec(() -> fireSymbolImageLoaded());
            log.log(Level.WARNING, "ERROR in loading SVG image " + imagePath, exception);
        });
    }

    private void loadDocument(InputStream inputStream) {
        svgHandler = null;
        failedToLoadDocument = true;
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        var parser = XMLResourceDescriptor.getXMLParserClassName();
        var factory = new SAXSVGDocumentFactory(parser);
        try {
            var workSpacePath = ResourceUtil.workspacePathToSysPath(new Path("/"));
            var uri = "file://" + (workSpacePath == null ? "" : workSpacePath.toOSString()) + imagePath.toString();
            svgDocument = factory.createDocument(uri, inputStream);
            svgHandler = new SVGHandler((SVGDocument) svgDocument, Display.getCurrent());
            svgHandler.setAlignedToNearestSecond(alignedToNearestSecond);
            initRenderingHints();
            var awtImage = svgHandler.getOffScreen();
            if (awtImage != null) {
                this.originalImageData = SVGUtils.toSWT(Display.getCurrent(), awtImage);
                resetData();
            }
            svgHandler.setRenderListener(image -> {
                if (disposed) {
                    return;
                }
                animatedImage = image;
                repaintAnimated = true;
                repaint();
            });
            needRender = true;
            failedToLoadDocument = false;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error loading SVG image " + imagePath, e);
        }
    }

    private final Document getDocument() {
        if (failedToLoadDocument) {
            return null;
        }
        if (svgHandler == null) {
            syncLoadImage();
        }
        return svgHandler == null ? null : svgHandler.getOriginalDocument();
    }

    private void initRenderingHints() {
        svgHandler.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        svgHandler.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        svgHandler.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svgHandler.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        svgHandler.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
