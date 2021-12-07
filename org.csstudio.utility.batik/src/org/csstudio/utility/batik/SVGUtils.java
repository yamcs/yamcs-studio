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

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.logging.Level;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

public class SVGUtils {

    public static ImageData loadSVG(IPath fullPath, InputStream is, int width, int height) {
        if (fullPath == null || is == null) {
            return null;
        }
        SimpleImageTranscoder transcoder = null;
        var parser = XMLResourceDescriptor.getXMLParserClassName();
        var factory = new SAXSVGDocumentFactory(parser);
        try {
            var svgDocument = factory.createDocument(fullPath.toOSString(), is);
            transcoder = new SimpleImageTranscoder(svgDocument);
            transcoder.getRenderingHints().put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            transcoder.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            transcoder.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            transcoder.getRenderingHints().put(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            transcoder.getRenderingHints().put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            transcoder.setCanvasSize(width, height);
            var awtImage = transcoder.getBufferedImage();
            if (awtImage != null) {
                return toSWT(Display.getCurrent(), awtImage);
            }
        } catch (Exception e) {
            Activator.getLogger().log(Level.WARNING, "Error loading SVG file" + fullPath, e);
        }
        return null;
    }

    /**
     * Converts an AWT based buffered image into an SWT <code>Image</code>. This will always return an
     * <code>Image</code> that has 24 bit depth regardless of the type of AWT buffered image that is passed into the
     * method.
     *
     * @param awtImage
     *            the {@link java.awt.image.BufferedImage} to be converted to an <code>Image</code>
     * @return an <code>Image</code> that represents the same image data as the AWT <code>BufferedImage</code> type.
     */
    public static ImageData toSWT(Device device, BufferedImage awtImage) {
        // We can force bit depth to be 24 bit because BufferedImage getRGB
        // allows us to always retrieve 24 bit data regardless of source color depth.
        var palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
        var swtImageData = new ImageData(awtImage.getWidth(), awtImage.getHeight(), 24, palette);
        // Ensure scan size is aligned on 32 bit.
        var scansize = (((awtImage.getWidth() * 3) + 3) * 4) / 4;
        var alphaRaster = awtImage.getAlphaRaster();
        var alphaBytes = new byte[awtImage.getWidth()];
        for (var y = 0; y < awtImage.getHeight(); y++) {
            var buff = awtImage.getRGB(0, y, awtImage.getWidth(), 1, null, 0, scansize);
            swtImageData.setPixels(0, y, awtImage.getWidth(), buff, 0);
            if (alphaRaster != null) {
                var alpha = alphaRaster.getPixels(0, y, awtImage.getWidth(), 1, (int[]) null);
                for (var i = 0; i < awtImage.getWidth(); i++) {
                    alphaBytes[i] = (byte) alpha[i];
                }
                swtImageData.setAlphas(0, y, awtImage.getWidth(), alphaBytes, 0);
            }
        }
        return swtImageData;
    }
}
