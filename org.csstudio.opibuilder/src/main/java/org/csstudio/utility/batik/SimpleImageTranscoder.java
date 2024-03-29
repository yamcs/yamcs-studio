/********************************************************************************
 * Copyright (c) 2008, 2021 Borland Software Corporation and others
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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.anim.dom.SVGStylableElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.GenericCDATASection;
import org.apache.batik.dom.GenericText;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.gvt.renderer.StaticRenderer;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.CSSConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SimpleImageTranscoder extends SVGAbstractTranscoder {

    private BufferedImage bufferedImage;
    private Document originalDocument, document;
    private int canvasWidth = -1, canvasHeight = -1;
    private Rectangle2D canvasAOI;
    private RenderingHints renderingHints;
    private Color colorToChange, appliedColor, colorToApply;
    private double[][] matrix;

    public SimpleImageTranscoder(Document document) {
        this.document = document;
        originalDocument = document;
        renderingHints = new RenderingHints(null);
    }

    public final Document getDocument() {
        return document;
    }

    public final RenderingHints getRenderingHints() {
        return renderingHints;
    }

    public final int getCanvasWidth() {
        return canvasWidth;
    }

    public final int getCanvasHeight() {
        return canvasHeight;
    }

    public void setCanvasSize(int width, int height) {
        if (canvasWidth == width && canvasHeight == height) {
            return;
        }
        canvasWidth = width;
        canvasHeight = height;
        contentChanged();
    }

    public final Rectangle2D getCanvasAreaOfInterest() {
        if (canvasAOI == null) {
            return null;
        }
        Rectangle2D result = new Rectangle2D.Float();
        result.setRect(canvasAOI);
        return result;
    }

    public void setCanvasAreaOfInterest(Rectangle2D value) {
        if (value == null) {
            if (canvasAOI == null) {
                return;
            }
            canvasAOI = null;
            contentChanged();
            return;
        }
        if (value.equals(canvasAOI)) {
            return;
        }
        canvasAOI = new Rectangle2D.Float();
        canvasAOI.setRect(value);
        contentChanged();
    }

    /**
     * Call before querying for CSS properties. If document has CSS engine installed returns null. Client is responsible
     * to dispose bridge context if it was returned by this method.
     */
    public BridgeContext initCSSEngine() {
        if (document == null) {
            return null;
        }
        var sd = (SVGOMDocument) document;
        if (sd.getCSSEngine() != null) {
            return null;
        }
        class BridgeContextEx extends BridgeContext {
            public BridgeContextEx() {
                super(SimpleImageTranscoder.this.userAgent);
                BridgeContextEx.this.setDocument(SimpleImageTranscoder.this.document);
                BridgeContextEx.this.initializeDocument(SimpleImageTranscoder.this.document);
            }
        }
        return new BridgeContextEx();
    }

    public void contentChanged() {
        bufferedImage = null;
    }

    private void updateImage() {
        if (document == null) {
            return;
        }
        if (colorToApply != null) {
            if (appliedColor == null) {
                appliedColor = colorToChange != null ? colorToChange
                        : new Color(Display.getCurrent(), 0, 0, 0);
            }
            changeColor(document, appliedColor, colorToApply);
            appliedColor = colorToApply;
        }
        try {
            if (canvasWidth > 0) {
                addTranscodingHint(ImageTranscoder.KEY_WIDTH, Float.valueOf(canvasWidth));
            } else {
                removeTranscodingHint(ImageTranscoder.KEY_WIDTH);
            }
            if (canvasHeight > 0) {
                addTranscodingHint(ImageTranscoder.KEY_HEIGHT, Float.valueOf(canvasHeight));
            } else {
                removeTranscodingHint(ImageTranscoder.KEY_HEIGHT);
            }
            if (canvasAOI != null) {
                addTranscodingHint(ImageTranscoder.KEY_AOI, canvasAOI);
            } else {
                removeTranscodingHint(ImageTranscoder.KEY_AOI);
            }
            transcode(new TranscoderInput(document), new TranscoderOutput());
        } catch (TranscoderException e) {
        }
    }

    @Override
    protected void transcode(Document document, String uri, TranscoderOutput output) throws TranscoderException {
        super.transcode(document, uri, output);
        var w = (int) (width + 0.5);
        var h = (int) (height + 0.5);
        var renderer = createImageRenderer();
        renderer.updateOffScreen(w, h);
        // curTxf.translate(0.5, 0.5);
        renderer.setTransform(curTxf);
        renderer.setTree(root);
        root = null; // We're done with it...
        try {
            Shape raoi = new Rectangle2D.Float(0, 0, width, height);
            // Warning: the renderer's AOI must be in user space
            renderer.repaint(curTxf.createInverse().createTransformedShape(raoi));
            bufferedImage = renderer.getOffScreen();
        } catch (Exception ex) {
            throw new TranscoderException(ex);
        }
    }

    protected ImageRenderer createImageRenderer() {
        var renderer = new StaticRenderer();
        renderer.getRenderingHints().add(renderingHints);
        return renderer;
    }

    public final BufferedImage getBufferedImage() {
        if (bufferedImage == null) {
            updateImage();
        }
        return bufferedImage;
    }

    public Color getColor() {
        return colorToApply;
    }

    public void setColor(Color newColor) {
        if (newColor == null || (colorToApply != null && newColor.equals(colorToApply))) {
            return;
        }
        colorToApply = newColor;
        contentChanged();
    }

    public void setColorToChange(Color newColor) {
        if (newColor == null || (colorToChange != null && newColor.equals(colorToChange))) {
            return;
        }
        colorToChange = newColor;
    }

    public double[][] getTransformMatrix() {
        return matrix;
    }

    public void setTransformMatrix(double[][] newMatrix) {
        if (newMatrix == null) {
            return;
        }
        matrix = newMatrix;
        document = applyMatrix(matrix);
        // Transformed document is based on original => reset color
        appliedColor = null;
        contentChanged();
    }

    public Dimension getDocumentSize() {
        var svgElmt = ((SVGOMDocument) document).getRootElement();
        double width = svgElmt.getWidth().getBaseVal().getValue();
        double height = svgElmt.getHeight().getBaseVal().getValue();
        return new Dimension((int) Math.round(width), (int) Math.round(height));
    }

    private void changeColor(Document doc, Color oldColor, Color newColor) {
        if (oldColor.equals(newColor)) {
            return;
        }

        Matcher matcher = null;
        var svgOldColor = toHexString(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue());
        var svgNewColor = toHexString(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
        var fillPattern = Pattern.compile("(?i)" + CSSConstants.CSS_FILL_PROPERTY + ":" + svgOldColor);
        var strokePattern = Pattern.compile("(?i)" + CSSConstants.CSS_STROKE_PROPERTY + ":" + svgOldColor);
        var fillReplace = CSSConstants.CSS_FILL_PROPERTY + ":" + svgNewColor;
        var strokeReplace = CSSConstants.CSS_STROKE_PROPERTY + ":" + svgNewColor;

        // Search for global style element <style type="text/css"></style>
        var styleList = doc.getElementsByTagName("style");
        for (var i = 0; i < styleList.getLength(); i++) {
            var style = (Element) styleList.item(i);
            var childList = style.getChildNodes();
            if (childList != null) {
                for (var j = 0; j < childList.getLength(); j++) {
                    var child = childList.item(j);
                    if (child instanceof GenericText || child instanceof GenericCDATASection) {
                        var cdata = (CharacterData) child;
                        var data = cdata.getData();
                        matcher = fillPattern.matcher(data);
                        data = matcher.replaceAll(fillReplace);
                        matcher = strokePattern.matcher(data);
                        data = matcher.replaceAll(strokeReplace);
                        data = replaceRGB(oldColor, newColor, data);
                        cdata.setData(data);
                    }
                }
            }
        }
        recursiveCC(doc.getDocumentElement(), oldColor, newColor, fillPattern, strokePattern, fillReplace,
                strokeReplace);
    }

    private void recursiveCC(Element elmt, Color oldColor, Color newColor, Pattern fillPattern, Pattern strokePattern,
            String fillReplace, String strokeReplace) {
        if (elmt == null) {
            return;
        }
        Matcher matcher = null;
        var styleList = elmt.getChildNodes();
        if (styleList != null) {
            for (var i = 0; i < styleList.getLength(); i++) {
                var child = styleList.item(i);
                if (child instanceof SVGStylableElement) {
                    recursiveCC((Element) child, oldColor, newColor, fillPattern, strokePattern, fillReplace,
                            strokeReplace);
                }
            }
        }
        if (elmt instanceof SVGStylableElement) {
            var style = elmt.getAttribute("style");
            matcher = fillPattern.matcher(style);
            style = matcher.replaceAll(fillReplace);
            matcher = strokePattern.matcher(style);
            style = matcher.replaceAll(strokeReplace);
            style = replaceRGB(oldColor, newColor, style);
            elmt.setAttribute("style", style);
        }
    }

    private String replaceRGB(Color oldColor, Color newColor, String data) {
        var rgbPattern = Pattern.compile("(?i)rgb\\(([0-9]+\\.?[0-9]*)%,([0-9]+\\.?[0-9]*)%,([0-9]+\\.?[0-9]*)%\\)");
        var nr = Math.round(newColor.getRed() / 255f * 100);
        var ng = Math.round(newColor.getGreen() / 255f * 100);
        var nb = Math.round(newColor.getBlue() / 255f * 100);
        var rgbReplace = "rgb(" + nr + "%," + ng + "%," + nb + "%)";
        var matcher = rgbPattern.matcher(data);
        var sb = new StringBuilder();
        var previousEnd = 0;
        while (matcher.find()) {
            var r = Math.round(Float.valueOf(matcher.group(1)) * 255 / 100);
            var g = Math.round(Float.valueOf(matcher.group(2)) * 255 / 100);
            var b = Math.round(Float.valueOf(matcher.group(3)) * 255 / 100);
            if (r == oldColor.getRed() && g == oldColor.getGreen() && b == oldColor.getBlue()) {
                var newStart = matcher.start();
                var newEnd = matcher.end();
                sb.append(data.subSequence(previousEnd, newStart));
                sb.append(rgbReplace);
                previousEnd = newEnd;
            }
        }
        sb.append(data.subSequence(previousEnd, data.length()));
        return sb.toString();
    }

    private String toHexString(int r, int g, int b) {
        return "#" + toSVGHexValue(r) + toSVGHexValue(g) + toSVGHexValue(b);
    }

    private String toSVGHexValue(int number) {
        var builder = new StringBuilder(Integer.toHexString(number & 0xff));
        while (builder.length() < 2) {
            builder.insert(0, '0'); // pad with leading zero if needed
        }
        return builder.toString().toUpperCase();
    }

    private Document applyMatrix(double[][] matrix) {
        // creation of the SVG document
        var impl = SVGDOMImplementation.getDOMImplementation();
        var svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        var newDocument = impl.createDocument(svgNS, "svg", null);

        // get the root element (the 'svg' element).
        var svgRoot = newDocument.getDocumentElement();

        // get the original document size
        var svgElmt = ((SVGOMDocument) originalDocument).getRootElement();
        var width = 30D;
        var height = 30D;
        try {
            width = svgElmt.getWidth().getBaseVal().getValue();
            height = svgElmt.getHeight().getBaseVal().getValue();
        } catch (NullPointerException e) {
            // FIXME
            // this is a dirty workaround for the RAP problem, which doesn't know how to
            // transform between units and pixels. Here we assume that all units are inches
            // and 96 dpi is used.
            var length = svgElmt.getWidth().getBaseVal();
            double value = length.getValueInSpecifiedUnits();
            width = value * 25.4 / 0.26458333333333333333333333333333;
            length = svgElmt.getHeight().getBaseVal();
            value = length.getValueInSpecifiedUnits();
            height = value * 25.4 / 0.26458333333333333333333333333333;
        }

        // current Transformation Matrix
        double[][] CTM = { { matrix[0][0], matrix[0][1], 0 }, { matrix[1][0], matrix[1][1], 0 }, { 0, 0, 1 } };

        // apply permutation to viewBox corner points
        var a = transformP(0.0, 0.0, 1.0, CTM);
        var b = transformP(width, 0.0, 1.0, CTM);
        var c = transformP(width, height, 1.0, CTM);
        var d = transformP(0.0, height, 1.0, CTM);

        // find new points
        var minX = findMin(a[0], b[0], c[0], d[0]);
        var minY = findMin(a[1], b[1], c[1], d[1]);
        var maxX = findMax(a[0], b[0], c[0], d[0]);
        var maxY = findMax(a[1], b[1], c[1], d[1]);
        var newWidth = maxX - minX;
        var newHeight = maxY - minY;

        // set the width and height attributes on the root 'svg' element.
        svgRoot.setAttributeNS(null, "width", String.valueOf(newWidth));
        svgRoot.setAttributeNS(null, "height", String.valueOf(newHeight));
        var vbs = minX + " " + minY + " " + newWidth + " " + newHeight;
        svgRoot.setAttributeNS(null, "viewBox", vbs);
        svgRoot.setAttributeNS(null, "preserveAspectRatio", "none");

        // Create the transform matrix
        var sb = new StringBuilder();
        // a c e
        // b d f
        // 0 0 1
        sb.append("matrix(");
        sb.append(CTM[0][0] + ",");
        sb.append(CTM[1][0] + ",");
        sb.append(CTM[0][1] + ",");
        sb.append(CTM[1][1] + ",");
        sb.append(CTM[0][2] + ",");
        sb.append(CTM[1][2] + ")");
        var graphic = newDocument.createElementNS(svgNS, "g");
        graphic.setAttributeNS(null, "transform", sb.toString());

        // Attach the transform to the root 'svg' element.
        var copiedRoot = newDocument.importNode(originalDocument.getDocumentElement(), true);
        graphic.appendChild(copiedRoot);
        svgRoot.appendChild(graphic);

        // TODO: remove this part => debug
        // Write to file
        // try {
        // TransformerFactory factory = TransformerFactory.newInstance();
        // Transformer transformer = factory.newTransformer();
        // FileWriter writer = new FileWriter("/home/ITER/arnaudf/perso/testX.svg");
        // Source source = new DOMSource(newDocument);
        // Result result = new StreamResult(writer);
        // transformer.transform(source, result);
        // writer.close();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        return newDocument;
    }

    // apply transformation to point { x, y, z } (affine transformation)
    private double[] transformP(double x, double y, double z, double[][] matrix) {
        double[] p = { x, y, z };
        var pp = new double[3];
        for (var a = 0; a < 3; a++) {
            for (var b = 0; b < 3; b++) {
                pp[a] += matrix[a][b] * p[b];
            }
        }
        return pp;
    }

    private double findMax(double a, double b, double c, double d) {
        var result = Math.max(a, b);
        result = Math.max(result, c);
        result = Math.max(result, d);
        return result;
    }

    private double findMin(double a, double b, double c, double d) {
        var result = Math.min(a, b);
        result = Math.min(result, c);
        result = Math.min(result, d);
        return result;
    }
}
