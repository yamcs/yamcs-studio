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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.time.DateUtils;
import org.csstudio.java.thread.ExecutionService;
import org.csstudio.swt.widgets.symbol.util.ImageUtils;
import org.csstudio.swt.widgets.util.AbstractInputStreamRunnable;
import org.csstudio.swt.widgets.util.ResourceUtil;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class GIFSymbolImage extends AbstractSymbolImage {

    private static final Logger log = Logger.getLogger(GIFSymbolImage.class.getName());

    private static final int MILLISEC_IN_SEC = 1000;

    private Dimension imgDimension = null;

    private volatile boolean loadingImage = false;

    /**
     * If this is an animated image
     */
    private boolean animated = false;
    private boolean refreshing = false;
    private boolean startAnimationRequested = false;

    private ImageLoader loader = new ImageLoader();

    private int repeatCount;

    private int animationIndex = 0;
    private long lastUpdateTime;
    private long interval_ms;
    private ScheduledFuture<?> scheduledFuture;

    /**
     * The imaged data array for animated image
     */
    private ImageData[] imageDataArray;
    private ImageData[] originalImageDataArray;

    /**
     * The index in image data array
     */
    private int showIndex = 0;
    private Image[] imageArray;

    public GIFSymbolImage(SymbolImageProperties sip, boolean runMode) {
        super(sip, runMode);
    }

    @Override
    public void dispose() {
        super.dispose();
        stopAnimation();
        if (imageArray != null) {
            for (var image : imageArray) {
                if (image != null && !image.isDisposed()) {
                    image.dispose();
                }
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    @Override
    public void paintFigure(Graphics gfx) {
        if (disposed || loadingImage || originalImageData == null) {
            return;
        }
        // Generate Data
        if (imageData == null) {
            generateAnimatedData();
            if (image != null && !image.isDisposed()) {
                image.dispose();
                image = null;
            }
            if (imageArray != null) {
                for (var image : imageArray) {
                    if (image != null && !image.isDisposed()) {
                        image.dispose();
                    }
                }
                imageArray = null;
            }
            if (animated) {
                startAnimation();
            }
        }
        // Create image
        if (image == null) {
            image = new Image(Display.getDefault(), imageData);
            if (animated && imageArray == null) {
                imageArray = new Image[imageDataArray.length];
                for (var index = 0; index < imageDataArray.length; index++) {
                    imageArray[index] = generateImage(index);
                }
            }
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
        if (animated) { // draw refreshing image
            if (startAnimationRequested) {
                realStartAnimation();
            }
            gfx.drawImage(imageArray[showIndex], srcArea, destArea);
        } else { // draw static image
            if (animated && animationDisabled && imageArray != null && showIndex != 0) {
                gfx.drawImage(imageArray[showIndex], srcArea, destArea);
            } else if (image != null) {
                gfx.drawImage(image, srcArea, destArea);
            }
        }
    }

    @Override
    public void resetData() {
        imageData = null;
    }

    private Image generateImage(int index) {
        var offScreenImage = new Image(Display.getDefault(), image.getBounds().width, image.getBounds().height);
        var offScreenImageGC = new GC(offScreenImage);

        var imageData = imageDataArray[index];
        var refresh_image = new Image(Display.getDefault(), imageData);
        switch (imageData.disposalMethod) {
        case SWT.DM_FILL_BACKGROUND:
            /* Fill with the background color before drawing. */
            if (backgroundColor != null) {
                offScreenImageGC.setBackground(backgroundColor);
            }
            offScreenImageGC.fillRectangle(imageData.x, imageData.y, imageData.width, imageData.height);
            break;
        case SWT.DM_FILL_PREVIOUS:
            /* Restore the previous image before drawing. */
            var startImage = new Image(Display.getDefault(), imageDataArray[0]);
            offScreenImageGC.drawImage(startImage, 0, 0, imageData.width, imageData.height, imageData.x, imageData.y,
                    imageData.width, imageData.height);
            startImage.dispose();
            break;
        }
        offScreenImageGC.drawImage(refresh_image, 0, 0, imageData.width, imageData.height, imageData.x, imageData.y,
                imageData.width, imageData.height);
        refresh_image.dispose();
        offScreenImageGC.dispose();
        return offScreenImage;
    }

    private void generateAnimatedData() {
        if (disposed) {
            return;
        }
        if (animated) {
            imageDataArray = new ImageData[originalImageDataArray.length];
            for (var i = 0; i < originalImageDataArray.length; i++) {
                imageDataArray[i] = (ImageData) originalImageDataArray[i].clone();
                if (!colorToChange.equals(currentColor)) {
                    imageDataArray[i] = ImageUtils.changeImageColor(currentColor, imageDataArray[i]);
                }
                imageDataArray[i] = ImageUtils.applyMatrix(imageDataArray[i], permutationMatrix);
                if (stretch && bounds != null) {
                    imageDataArray[i] = imageDataArray[i].scaledTo(bounds.width + leftCrop + rightCrop,
                            bounds.height + topCrop + bottomCrop);
                }
            }
            imageData = imageDataArray[0];
        } else {
            imageData = (ImageData) originalImageData.clone();
            if (!colorToChange.equals(currentColor)) {
                imageData = ImageUtils.changeImageColor(currentColor, imageData);
            }
            imageData = ImageUtils.applyMatrix(imageData, permutationMatrix);
            if (stretch && bounds != null) {
                imageData = imageData.scaledTo(bounds.width + leftCrop + rightCrop,
                        bounds.height + topCrop + bottomCrop);
            }
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
    public void resizeImage() {
        super.resizeImage();
        if (refreshing && animated) {
            stopAnimation();
            startAnimation();
        }
    }

    @Override
    public Dimension getAutoSizedDimension() {
        // if (imgDimension == null)
        // generateAnimatedData();
        return imgDimension;
    }

    @Override
    public void setAnimationDisabled(boolean stop) {
        super.setAnimationDisabled(stop);
        if (stop) {
            stopAnimation();
        } else if (animated) {
            startAnimation();
        }
    }

    @Override
    public void setAlignedToNearestSecond(boolean aligned) {
        super.setAlignedToNearestSecond(aligned);
        if (refreshing && animated) {
            stopAnimation();
            startAnimation();
        }
    }

    /**
     * Start animation. The request will be pended until figure painted for the first time.
     */
    public void startAnimation() {
        startAnimationRequested = true;
        repaint();
    }

    /**
     * stop the animation if the image is an animated GIF image.
     */
    public void stopAnimation() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        refreshing = false;
        showIndex = 0;
    }

    /**
     * start the animation if the image is an animated GIF image.
     */
    public synchronized void realStartAnimation() {
        startAnimationRequested = false;
        if (animated && !refreshing && !animationDisabled) {
            repeatCount = loader.repeatCount;
            // animationIndex = 0;
            lastUpdateTime = 0;
            interval_ms = 0;
            refreshing = true;
            Runnable animationTask = () -> UIBundlingThread.getInstance().addRunnable(() -> {
                synchronized (GIFSymbolImage.this) {
                    if (refreshing && (loader.repeatCount == 0 || repeatCount > 0)) {
                        var currentTime = System.currentTimeMillis();
                        // use Math.abs() to ensure that the system
                        // time adjust won't cause problem
                        if (Math.abs(currentTime - lastUpdateTime) >= interval_ms) {
                            setShowIndex(animationIndex);
                            lastUpdateTime = currentTime;
                            var ms = originalImageDataArray[animationIndex].delayTime * 10;
                            animationIndex = (animationIndex + 1) % originalImageDataArray.length;
                            if (ms < 20) {
                                ms += 30;
                            }
                            if (ms < 30) {
                                ms += 10;
                            }
                            interval_ms = ms;
                            /*
                             * If we have just drawn the last image, decrement the repeat count and start
                             * again.
                             */
                            if (loader.repeatCount > 0 && animationIndex == originalImageDataArray.length - 1) {
                                repeatCount--;
                            }
                        }
                    } else if (loader.repeatCount > 0 && repeatCount <= 0) {
                        // stop thread when animation finished
                        if (scheduledFuture != null) {
                            scheduledFuture.cancel(true);
                            scheduledFuture = null;
                        }
                    }
                }
            });
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
                scheduledFuture = null;
            }
            var initialDelay = 100L;
            if (alignedToNearestSecond) {
                var now = new Date();
                var nearestSecond = DateUtils.round(now, Calendar.SECOND);
                initialDelay = nearestSecond.getTime() - now.getTime();
                if (initialDelay < 0) {
                    initialDelay = MILLISEC_IN_SEC + initialDelay;
                }
            }
            scheduledFuture = ExecutionService.getInstance().getScheduledExecutorService()
                    .scheduleAtFixedRate(animationTask, initialDelay, 10, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void setShowIndex(int showIndex) {
        if (showIndex >= imageDataArray.length || this.showIndex == showIndex) {
            return;
        }
        this.showIndex = showIndex;
        repaint();
    }

    @Override
    public void syncLoadImage() {
        if (imagePath == null) {
            return;
        }
        loadingImage = true;
        if (animated) {
            stopAnimation();
            showIndex = 0;
            animationIndex = 0;
        }
        InputStream stream = null;
        Image tempImage = null;
        try {
            stream = ResourceUtil.pathToInputStream(imagePath);
            var dataArray = loader.load(stream);
            if (dataArray == null || dataArray.length < 1) {
                return;
            }
            originalImageDataArray = dataArray;
            originalImageData = originalImageDataArray[0];
            animated = originalImageDataArray.length > 1;
        } catch (Exception e) {
            log.log(Level.WARNING, "ERROR in loading PNG image " + imagePath, e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (tempImage != null && !tempImage.isDisposed()) {
                    tempImage.dispose();
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "ERROR in closing GIF image stream ", e);
            }
        }
        loadingImage = false;
        resetData();
        if (animated) {
            startAnimation();
        }
    }

    @Override
    public void asyncLoadImage() {
        if (imagePath == null) {
            return;
        }
        loadingImage = true;
        if (animated) {
            stopAnimation();
            showIndex = 0;
            animationIndex = 0;
        }
        AbstractInputStreamRunnable uiTask = new AbstractInputStreamRunnable() {
            @Override
            public void runWithInputStream(InputStream stream) {
                synchronized (GIFSymbolImage.this) {
                    var dataArray = loader.load(stream);
                    if (dataArray == null || dataArray.length < 1) {
                        return;
                    }
                    originalImageDataArray = dataArray;
                    originalImageData = originalImageDataArray[0];
                    animated = originalImageDataArray.length > 1;
                    loadingImage = false;
                    resetData();
                    if (animated) {
                        startAnimation();
                    }
                    Display.getDefault().syncExec(() -> fireSymbolImageLoaded());
                }
            }
        };
        ResourceUtil.pathToInputStreamInJob(imagePath, uiTask, "Loading GIF Image...", e -> {
            loadingImage = false;
            Display.getDefault().syncExec(this::fireSymbolImageLoaded);
            log.log(Level.WARNING, "ERROR in loading GIF image " + imagePath, e);
        });
    }
}
