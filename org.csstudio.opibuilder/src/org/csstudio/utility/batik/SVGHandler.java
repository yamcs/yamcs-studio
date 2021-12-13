/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.utility.batik;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGStylableElement;
import org.apache.batik.anim.timing.TimedDocumentRoot;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.DynamicGVTBuilder;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.ConcreteImageRendererFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.gvt.renderer.ImageRendererFactory;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.lang3.time.DateUtils;
import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.utility.batik.util.ICSSHandler;
import org.csstudio.utility.batik.util.SVGAnimateElementValuesHandler;
import org.csstudio.utility.batik.util.SVGAnimationEngine;
import org.csstudio.utility.batik.util.SVGStylableElementCSSHandler;
import org.csstudio.utility.batik.util.StyleSheetCSSHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimateElement;
import org.w3c.dom.svg.SVGDocument;

/**
 * {@link SVGDocument} handler. Handles render and animation of SVG files.
 */
public class SVGHandler {

    private static final int MILLISEC_IN_SEC = 1000;

    protected SVGHandlerListener handlerListener;

    /**
     * The listener.
     */
    protected Listener listener;

    /**
     * The renderer factory.
     */
    protected ImageRendererFactory rendererFactory = new ConcreteImageRendererFactory();

    /**
     * The current renderer.
     */
    protected ImageRenderer renderer;

    /**
     * The user agent.
     */
    protected UserAgent userAgent;

    /**
     * The current bridge context.
     */
    protected BridgeContext bridgeContext;

    /**
     * The concrete bridge document loader.
     */
    protected DocumentLoader loader;

    protected GraphicsNode gvtRoot;

    /**
     * The update manager.
     */
    protected UpdateManager updateManager;

    protected TimedDocumentRoot timedDocumentRoot;

    protected SVGAnimationEngine svgAnimationEngine;

    protected AnimatedSVGCache cache;

    /**
     * The current SVG document.
     */
    protected SVGDocument svgDocument;

    protected SVGDocument originalSVGDocument;

    protected Dimension2D originalDimension;

    /**
     * Whether the current document has dynamic features.
     */
    protected boolean isDynamicDocument;

    /**
     * The animation limiting mode.
     */
    protected int animationLimitingMode;

    /**
     * The amount of animation limiting.
     */
    protected float animationLimitingAmount;

    protected GVTBuilder builder;

    private RenderingHints renderingHints;
    private float canvasWidth = -1, canvasHeight = -1;
    private Color colorToChange, colorToApply;
    private double[][] matrix = new double[][] { { 1, 0 }, { 0, 1 } };

    private boolean needRender = true;
    private boolean disposed = false;

    private boolean alignedToNearestSecond = false;
    private boolean started = false;
    private boolean suspended = true;

    private final Display swtDisplay;

    /*
     * Enable caching of images provided by Batik SVG animation process until the animation has repeated once and then
     * take over. Instead of always disposing the previous image, all images will be disposed when the figure is
     * disposed. WARNING: Batik is not built to work with a cache, using this one can generate hazards.
     */
    private boolean useCache = false;

    private int cacheMaxSize = 100;

    private List<Image> imageBuffer;
    private int maxBufferSize;

    public SVGHandler(SVGDocument doc, Display display) {
        swtDisplay = display;
        imageBuffer = Collections.synchronizedList(new ArrayList<Image>());
        maxBufferSize = cacheMaxSize;

        listener = new Listener();
        userAgent = createUserAgent();
        renderingHints = new RenderingHints(null);

        var impl = SVGDOMImplementation.getDOMImplementation();
        originalSVGDocument = (SVGDocument) DOMUtilities.deepCloneDocument(doc, impl);
        bridgeContext = createBridgeContext((SVGOMDocument) originalSVGDocument);
        isDynamicDocument = bridgeContext.isDynamicDocument(originalSVGDocument);
        // As we update the DOM, it has to be considered as dynamic
        bridgeContext.setDynamicState(BridgeContext.DYNAMIC);
        builder = new DynamicGVTBuilder();
        // Build to calculate original dimension
        gvtRoot = builder.build(bridgeContext, originalSVGDocument);
        originalDimension = bridgeContext.getDocumentSize();

        svgDocument = (SVGDocument) createWrapper(originalSVGDocument);
        builder.build(bridgeContext, svgDocument);
        buildElementsToUpdateList(bridgeContext, svgDocument);
        setAnimationLimitingFPS(10);
    }

    public void dispose() {
        disposed = true;
        if (updateManager != null) {
            updateManager.removeUpdateManagerListener(listener);
            updateManager.interrupt();
            updateManager = null;
        } else if (bridgeContext != null) {
            bridgeContext.dispose();
            bridgeContext = null;
        }
        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
        if (cache != null && !cache.isDisposed()) {
            cache.dispose();
            cache = null;
        }
    }

    /**
     * Creates a new user agent.
     */
    protected UserAgent createUserAgent() {
        return new UserAgentAdapter();
    }

    /**
     * Creates a new bridge context.
     */
    protected BridgeContext createBridgeContext(SVGOMDocument doc) {
        if (loader == null) {
            loader = new DocumentLoader(userAgent);
        }
        BridgeContext result = null;
        if (doc.isSVG12()) {
            result = new org.csstudio.utility.batik.util.SVG12BridgeContext(userAgent, loader);
        } else {
            result = new org.csstudio.utility.batik.util.BridgeContext(userAgent, loader);
        }
        return result;
    }

    /**
     * Creates a new renderer.
     */
    protected ImageRenderer createImageRenderer() {
        ImageRenderer renderer = null;
        if (isDynamicDocument) {
            renderer = rendererFactory.createDynamicImageRenderer();
        } else {
            renderer = rendererFactory.createStaticImageRenderer();
        }
        renderer.getRenderingHints().add(renderingHints);
        return renderer;
    }

    protected void handleException(Exception e) {
        OPIBuilderPlugin.getLogger().log(Level.SEVERE, e.getMessage());
    }

    // //////////////////////////////////////////////////////////////////////
    // Getters/setters
    // //////////////////////////////////////////////////////////////////////

    public SVGDocument getOriginalDocument() {
        return originalSVGDocument;
    }

    public void setRenderListener(SVGHandlerListener renderListener) {
        handlerListener = renderListener;
    }

    public void setRenderingHint(Object key, Object value) {
        renderingHints.put(key, value);
        refreshContent();
    }

    public void removeRenderingHint(Object key) {
        renderingHints.remove(key);
        refreshContent();
    }

    public void setCanvasSize(int width, int height) {
        if (canvasWidth == width && canvasHeight == height) {
            return;
        }
        canvasWidth = width;
        canvasHeight = height;
        refreshContent();
    }

    public void setColorToApply(Color newColor) {
        if ((newColor == null && colorToApply == null)
                || (colorToApply != null && colorToApply.equals(newColor))) {
            return;
        }
        colorToApply = newColor;
        refreshContent();
    }

    public void setColorToChange(Color newColor) {
        if ((newColor == null && colorToChange == null)
                || (colorToChange != null && colorToChange.equals(newColor))) {
            return;
        }
        colorToChange = newColor;
        refreshContent();
    }

    public void setTransformMatrix(double[][] newMatrix) {
        if ((newMatrix == null && matrix == null) || (matrix != null && matrix.equals(newMatrix))) {
            return;
        }
        matrix = newMatrix;
        if (newMatrix == null) {
            // set identity matrix
            matrix = new double[][] { { 1, 0 }, { 0, 1 } };
        }
        refreshContent();
    }

    public void refreshContent() {
        needRender = true;
    }

    public boolean isDynamicDocument() {
        return isDynamicDocument;
    }

    /**
     * @return document size after applying matrix.
     */
    public Dimension getDocumentSize() {
        var aoi = calculateShape();
        var docWidth = aoi.getBounds().getWidth();
        var docHeight = aoi.getBounds().getHeight();
        return new Dimension((int) Math.round(docWidth), (int) Math.round(docHeight));
    }

    public void setAlignedToNearestSecond(boolean alignedToNearestSecond) {
        if (this.alignedToNearestSecond == alignedToNearestSecond) {
            return;
        }
        this.alignedToNearestSecond = alignedToNearestSecond;
        if (cache != null) {
            cache.setAlignedToNearestSecond(alignedToNearestSecond);
        }
        if (alignedToNearestSecond && isDynamicDocument && started) {
            alignTimeToNearestSecond();
        }
    }

    private void alignTimeToNearestSecond() {
        Runnable startTask = () -> {
            if (disposed) {
                return;
            }
            var now = new Date();
            var nearestSecond = DateUtils.round(now, Calendar.SECOND);
            var initialDelay = nearestSecond.getTime() - now.getTime();
            if (initialDelay < 0) {
                initialDelay = MILLISEC_IN_SEC + initialDelay;
            }
            try {
                Thread.sleep(initialDelay);
            } catch (InterruptedException e) {
                OPIBuilderPlugin.getLogger().log(Level.WARNING, "SVG animation FAILED to align to nearest second");
            }
            resetDocumentTime();
            // restart/reset cache
            if (useCache && cache.isRunning()) {
                cache.restartProcessing();
            }
        };
        new Thread(startTask).start();
    }

    private void resetDocumentTime() {
        if (svgAnimationEngine != null && timedDocumentRoot.getDocumentBeginTime() != null) {
            svgAnimationEngine.setCurrentTime(0);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Processing methods
    // //////////////////////////////////////////////////////////////////////

    /**
     * Resumes the processing of the current document.
     */
    public void resumeProcessing() {
        if (updateManager != null && started && suspended) {
            if (useCache && cache.isFilled() && !cache.isRunning()) {
                cache.startProcessing();
            } else if (svgAnimationEngine.isPaused()) {
                updateManager.manageUpdates(renderer);
                svgAnimationEngine.unpause();
            }
            suspended = false;
            OPIBuilderPlugin.getLogger().log(Level.FINE, "SVG animation RESUMED");
        }
    }

    /**
     * Suspend the processing of the current document.
     */
    public void suspendProcessing() {
        if (updateManager != null && started) {
            svgAnimationEngine.pause();
            updateManager.suspend();
            if (useCache) {
                cache.stopProcessing();
            }
            suspended = true;
            OPIBuilderPlugin.getLogger().log(Level.FINE, "SVG animation SUSPENDED");
        }
    }

    /**
     * Start the processing of the current document.
     */
    public void startProcessing() {
        if (disposed) {
            return;
        }
        if (started) {
            resumeProcessing();
            return;
        }
        if (needRender) {
            doRender();
        }
        Runnable startTask = () -> {
            try {
                if (disposed) {
                    return;
                }
                updateManager = new UpdateManager(bridgeContext, gvtRoot, svgDocument);
                updateManager.addUpdateManagerListener(listener);
                updateManager.manageUpdates(renderer);
                svgAnimationEngine = (SVGAnimationEngine) bridgeContext.getAnimationEngine();
                timedDocumentRoot = svgAnimationEngine.getTimedDocumentRoot();
                if (useCache) {
                    resetCache();
                }
                var initialDelay = 0L;
                if (alignedToNearestSecond) {
                    var now = new Date();
                    var nearestSecond = DateUtils.round(now, Calendar.SECOND);
                    initialDelay = nearestSecond.getTime() - now.getTime();
                    if (initialDelay < 0) {
                        initialDelay = MILLISEC_IN_SEC + initialDelay;
                    }
                }
                try {
                    Thread.sleep(initialDelay);
                } catch (InterruptedException e1) {
                    OPIBuilderPlugin.getLogger().log(Level.WARNING,
                            "SVG animation FAILED to align to nearest second");
                }
                // This will call SVGAnimationEngine.start(long documentStartTime)
                // with System.currentTimeMillis()
                updateManager.dispatchSVGLoadEvent();
                started = true;
                suspended = false;
                OPIBuilderPlugin.getLogger().log(Level.FINE, "SVG animation STARTED");
            } catch (Exception e2) {
                handleException(e2);
                return;
            } catch (OutOfMemoryError e3) {
                OPIBuilderPlugin.getLogger().log(Level.SEVERE, "ERROR starting SVG animation: " + e3.getMessage());
                return;
            }
        };
        new Thread(startTask).start();
    }

    // //////////////////////////////////////////////////////////////////////
    // Animation methods
    // //////////////////////////////////////////////////////////////////////

    /**
     * Sets the animation limiting mode to "none".
     */
    public void setAnimationLimitingNone() {
        animationLimitingMode = 0;
        if (bridgeContext != null) {
            setBridgeContextAnimationLimitingMode();
        }
    }

    /**
     * Sets the animation limiting mode to a percentage of CPU.
     *
     * @param pc
     *            the maximum percentage of CPU to use (0 &lt; pc ≤ 1)
     */
    public void setAnimationLimitingCPU(float pc) {
        animationLimitingMode = 1;
        animationLimitingAmount = pc;
        if (bridgeContext != null) {
            setBridgeContextAnimationLimitingMode();
        }
    }

    /**
     * Sets the animation limiting mode to a number of frames per second.
     *
     * @param fps
     *            the maximum number of frames per second (fps &gt; 0)
     */
    public void setAnimationLimitingFPS(float fps) {
        animationLimitingMode = 2;
        animationLimitingAmount = fps;
        if (bridgeContext != null) {
            setBridgeContextAnimationLimitingMode();
        }
    }

    /**
     * Sets the animation limiting mode on the current bridge context.
     */
    protected void setBridgeContextAnimationLimitingMode() {
        if (bridgeContext == null) {
            return;
        }
        switch (animationLimitingMode) {
        case 0: // unlimited
            bridgeContext.setAnimationLimitingNone();
            break;
        case 1: // %cpu
            bridgeContext.setAnimationLimitingCPU(animationLimitingAmount);
            break;
        case 2: // fps
            bridgeContext.setAnimationLimitingFPS(animationLimitingAmount);
            break;
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Rendering methods
    // //////////////////////////////////////////////////////////////////////

    /**
     * @return SVG static image.
     */
    public BufferedImage getOffScreen() {
        if (disposed) {
            return null;
        }
        if (needRender) {
            render();
        }
        return renderer.getOffScreen();
    }

    protected void render() {
        if (isDynamicDocument) {
            var isRunning = started && !suspended;
            if (isRunning) {
                suspendProcessing();
            }
            if (cache != null) {
                cache.flush();
            }
            doRender();
            // resetDocumentTime();
            if (isRunning) {
                resumeProcessing();
            }
        } else {
            doRender();
        }
    }

    protected void doRender() {
        if (disposed) {
            return;
        }
        updateMatrix();
        changeColor(colorToChange, colorToApply);
        gvtRoot = builder.build(bridgeContext, svgDocument);

        // get the 'width' and 'height' attributes of the SVG document
        float width = 400, height = 400;
        var docWidth = (float) bridgeContext.getDocumentSize().getWidth();
        var docHeight = (float) bridgeContext.getDocumentSize().getHeight();
        if (canvasWidth > 0 && canvasHeight > 0) {
            width = canvasWidth;
            height = canvasHeight;
        } else if (canvasHeight > 0) {
            width = (docWidth * canvasHeight) / docHeight;
            height = canvasHeight;
        } else if (canvasWidth > 0) {
            width = canvasWidth;
            height = (docHeight * canvasWidth) / docWidth;
        } else {
            width = docWidth;
            height = docHeight;
        }

        // compute the preserveAspectRatio matrix
        AffineTransform renderingTransform = null;
        AffineTransform Px = null;
        var root = svgDocument.getRootElement();
        var viewBox = root.getAttributeNS(null, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE);
        if (viewBox != null && viewBox.length() != 0) {
            var aspectRatio = root.getAttributeNS(null, SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE);
            Px = ViewBox.getPreserveAspectRatioTransform(root, viewBox, aspectRatio, width, height, bridgeContext);
        } else {
            // no viewBox has been specified, create a scale transform
            var xscale = width / docWidth;
            var yscale = height / docHeight;
            var scale = Math.min(xscale, yscale);
            Px = AffineTransform.getScaleInstance(scale, scale);
        }
        Shape curAOI = new Rectangle2D.Float(0, 0, width, height);
        var cgn = getCanvasGraphicsNode(gvtRoot);
        if (cgn != null) {
            cgn.setViewingTransform(Px);
            renderingTransform = new AffineTransform();
        } else {
            renderingTransform = Px;
        }

        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
        renderer = createImageRenderer();

        var w = (int) (curAOI.getBounds().width + 0.5);
        var h = (int) (curAOI.getBounds().height + 0.5);
        renderer.updateOffScreen(w, h);
        renderer.setTree(gvtRoot);
        renderer.setTransform(renderingTransform);
        renderer.setDoubleBuffered(false);
        renderer.clearOffScreen();
        renderer.repaint(curAOI);

        if (updateManager != null) {
            updateManager.setGVTRoot(gvtRoot);
        }
        needRender = false;
    }

    protected CanvasGraphicsNode getCanvasGraphicsNode(GraphicsNode gn) {
        if (!(gn instanceof CompositeGraphicsNode)) {
            return null;
        }
        var cgn = (CompositeGraphicsNode) gn;
        List<?> children = cgn.getChildren();
        if (children.size() == 0) {
            return null;
        }
        gn = (GraphicsNode) children.get(0);
        if (!(gn instanceof CanvasGraphicsNode)) {
            return null;
        }
        return (CanvasGraphicsNode) gn;
    }

    /**
     * To hide the listener methods.
     */
    protected class Listener implements UpdateManagerListener {

        @Override
        public void managerStarted(UpdateManagerEvent e) {
        }

        @Override
        public void managerSuspended(UpdateManagerEvent e) {
        }

        @Override
        public void managerResumed(UpdateManagerEvent e) {
        }

        @Override
        public void managerStopped(UpdateManagerEvent e) {
        }

        @Override
        public void updateStarted(UpdateManagerEvent e) {
        }

        @Override
        public void updateCompleted(UpdateManagerEvent e) {
            if (e.getImage() == null) {
                return;
            }
            if (useCache && cache != null) {
                var newImage = cache.addImage(e.getImage());
                if (cache.isFilled()) {
                    OPIBuilderPlugin.getLogger().log(Level.FINE,
                            "SVG cache FILLED with " + cache.getSize() + " images");
                    svgAnimationEngine.pause();
                    updateManager.suspend();
                    updateManager.getScriptingEnvironment().interrupt();
                    if (!suspended) {
                        cache.startProcessing();
                    }
                } else {
                    notifyNewImage(newImage);
                }
            } else if (!suspended) {
                var imageData = SVGUtils.toSWT(swtDisplay, e.getImage());
                var newImage = new Image(swtDisplay, imageData);
                notifyNewImage(newImage);
            }
        }

        @Override
        public void updateFailed(UpdateManagerEvent e) {
        }
    }

    protected void resetCache() {
        AnimatedSVGCache copy;
        synchronized (this) {
            copy = cache;
            cache = new AnimatedSVGCache(swtDisplay, timedDocumentRoot,
                    this::notifyNewImage, cacheMaxSize);
        }
        if (copy != null && !copy.isDisposed()) {
            copy.dispose();
        }
    }

    protected void addToBuffer(Image image) {
        if (imageBuffer.size() == maxBufferSize) {
            List<Image> entriesCopy = new ArrayList<>(imageBuffer);
            imageBuffer.clear();
            Runnable flushTask = () -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                for (var entry : entriesCopy) {
                    entry.dispose();
                }
                entriesCopy.clear();
                OPIBuilderPlugin.getLogger().log(Level.FINE, "SVG image buffer FLUSHED");
            };
            new Thread(flushTask).start();
        }
        imageBuffer.add(image);
    }

    protected void notifyNewImage(Image newImage) {
        if (handlerListener != null && newImage != null && !suspended) {
            swtDisplay.asyncExec(() -> {
                if (suspended) {
                    return;
                }
                handlerListener.newImage(newImage);
                if (!useCache) {
                    addToBuffer(newImage);
                }
            });
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Change color & matrix private methods
    // //////////////////////////////////////////////////////////////////////

    private List<ICSSHandler> elementsToUpdate = new ArrayList<>();

    private void changeColor(Color colorToChange, Color newColor) {
        var it = elementsToUpdate.iterator();
        while (it.hasNext()) {
            it.next().updateCSSColor(colorToChange, newColor);
        }
        ((SVGOMDocument) svgDocument).clearViewCSS();
    }

    private void buildElementsToUpdateList(BridgeContext ctx, Document doc) {
        if (doc == null) {
            return;
        }
        elementsToUpdate.clear();
        var cssEngine = (SVGCSSEngine) ctx.getCSSEngineForElement(doc.getDocumentElement());
        if (cssEngine == null) {
            return;
        }
        List<?> styleSheetsList = cssEngine.getStyleSheetNodes();
        for (var node : styleSheetsList) {
            if (node instanceof CSSStyleSheetNode) {
                var cssNode = (CSSStyleSheetNode) node;
                var styleSheet = cssNode.getCSSStyleSheet();
                elementsToUpdate.add(new StyleSheetCSSHandler(cssEngine, styleSheet));
            }
        }
        rBuidElementsList(cssEngine, doc.getDocumentElement());
    }

    private void rBuidElementsList(SVGCSSEngine cssEngine, Element elmt) {
        if (elmt == null) {
            return;
        }
        var styleList = elmt.getChildNodes();
        if (styleList != null) {
            for (var i = 0; i < styleList.getLength(); i++) {
                var child = styleList.item(i);
                if (child instanceof Element) {
                    rBuidElementsList(cssEngine, (Element) child);
                }
            }
        }
        if (elmt instanceof SVGStylableElement) {
            elementsToUpdate.add(new SVGStylableElementCSSHandler(cssEngine, (SVGStylableElement) elmt));
        } else if (elmt instanceof SVGAnimateElement) {
            elementsToUpdate.add(new SVGAnimateElementValuesHandler(cssEngine, (SVGAnimateElement) elmt));
        }
    }

    private Element mainGraphicNode;
    private Element svgRootNode;

    private Document createWrapper(SVGDocument doc) {
        // creation of the SVG document
        var svgNamespace = SVGDOMImplementation.SVG_NAMESPACE_URI;
        var impl = SVGDOMImplementation.getDOMImplementation();
        var newDocument = impl.createDocument(svgNamespace, "svg", null);

        // get the root element
        svgRootNode = newDocument.getDocumentElement();
        mainGraphicNode = newDocument.createElementNS(svgNamespace, "g");

        // attach the root of original doc to transform to the root
        var copiedRoot = newDocument.importNode(doc.getDocumentElement(), true);
        mainGraphicNode.appendChild(copiedRoot);
        svgRootNode.appendChild(mainGraphicNode);
        updateMatrix();
        return newDocument;
    }

    private Shape calculateShape() {
        var width = originalDimension.getWidth();
        var height = originalDimension.getHeight();

        var flatmatrix = new double[] { matrix[0][0], matrix[1][0], matrix[0][1], matrix[1][1] };
        var at = new AffineTransform(flatmatrix);
        Shape curAOI = new Rectangle2D.Double(0, 0, width, height);
        return at.createTransformedShape(curAOI);
    }

    private void updateMatrix() {
        var newAOI = calculateShape();
        double newX = newAOI.getBounds().x;
        double newY = newAOI.getBounds().y;
        double newWidth = newAOI.getBounds().width;
        double newHeight = newAOI.getBounds().height;

        // set the width and height attributes on the root element
        svgRootNode.setAttributeNS(null, "width", String.valueOf(newWidth));
        svgRootNode.setAttributeNS(null, "height", String.valueOf(newHeight));
        var vbs = newX + " " + newY + " " + newWidth + " " + newHeight;
        svgRootNode.setAttributeNS(null, "viewBox", vbs);
        svgRootNode.setAttributeNS(null, "preserveAspectRatio", "none");

        // current Transformation Matrix
        double[][] CTM = { { matrix[0][0], matrix[0][1], 0 }, { matrix[1][0], matrix[1][1], 0 }, { 0, 0, 1 } };
        // create the transform matrix
        var sb = new StringBuilder();
        sb.append("matrix(");
        sb.append(CTM[0][0] + ",");
        sb.append(CTM[1][0] + ",");
        sb.append(CTM[0][1] + ",");
        sb.append(CTM[1][1] + ",");
        sb.append(CTM[0][2] + ",");
        sb.append(CTM[1][2] + ")");

        mainGraphicNode.setAttributeNS(null, "transform", sb.toString());
    }
}
