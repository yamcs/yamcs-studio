/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.symbol.util;

import java.awt.GradientPaint;
import java.awt.image.BufferedImage;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * Utility class to change image behavior like color, shape, rotation management, ...
 */
public final class ImageUtils {

    /**
     * Constructor cannot be call because of static invocation.
     */
    private ImageUtils() {
    }

    /**
     * Apply the specified {@link PermutationMatrix} to the given {@link ImageData}.
     */
    public static ImageData applyMatrix(ImageData srcData, PermutationMatrix pm) {
        if (srcData == null || pm == null || pm.equals(PermutationMatrix.generateIdentityMatrix())) {
            return srcData;
        }
        var matrix = pm.getMatrix();

        // point to rotate about => center of image
        var x0 = 0D;
        var y0 = 0D;

        // apply permutation to 4 corners
        var a = translate(0, 0, x0, y0, matrix);
        var b = translate(srcData.width - 1, 0, x0, y0, matrix);
        var c = translate(srcData.width - 1, srcData.height - 1, x0, y0, matrix);
        var d = translate(0, srcData.height - 1, x0, y0, matrix);

        // find new point
        var minX = findMin(a[0], b[0], c[0], d[0]);
        var minY = findMin(a[1], b[1], c[1], d[1]);
        var maxX = findMax(a[0], b[0], c[0], d[0]);
        var maxY = findMax(a[1], b[1], c[1], d[1]);
        var newWidth = (int) Math.round(maxX - minX);
        var newHeight = (int) Math.round(maxY - minY);

        var newImageData = new ImageData(newWidth, newHeight, srcData.depth, srcData.palette);
        for (var destX = 0; destX < newImageData.width; destX++) {
            for (var destY = 0; destY < newImageData.height; destY++) {
                if (srcData.transparentPixel >= 0) {
                    newImageData.setPixel(destX, destY, srcData.transparentPixel);
                }
                newImageData.setAlpha(destX, destY, 0);
            }
        }

        for (var srcX = 0; srcX < srcData.width; srcX++) {
            for (var srcY = 0; srcY < srcData.height; srcY++) {
                int destX = 0, destY = 0;
                var destP = translate(srcX, srcY, x0, y0, matrix);
                destX = (int) Math.round(destP[0] - minX);
                destY = (int) Math.round(destP[1] - minY);

                if (destX >= 0 && destX < newWidth && destY >= 0 && destY < newHeight) {
                    newImageData.setPixel(destX, destY, srcData.getPixel(srcX, srcY));
                    newImageData.setAlpha(destX, destY, srcData.getAlpha(srcX, srcY));
                }
            }
        }
        // Re-set the lost transparency
        newImageData.transparentPixel = srcData.transparentPixel;
        newImageData.delayTime = srcData.delayTime;
        newImageData.disposalMethod = srcData.disposalMethod;
        return newImageData;
    }

    // multiply matrices
    private static double[][] multiply(double[][] m1, double[][] m2) {
        int p1 = m1.length, p2 = m2.length, q2 = m2[0].length;
        var result = new double[p1][q2];
        for (var i = 0; i < p1; i++) {
            for (var j = 0; j < q2; j++) {
                for (var k = 0; k < p2; k++) {
                    result[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return result;
    }

    // calculate new coordinates
    private static double[] translate(int x, int y, double x0, double y0, double[][] matrix) {
        // translate coordinates
        var p = new double[2][1];
        p[0][0] = x - x0;
        p[1][0] = y - y0;
        // apply permutation
        var pp = multiply(matrix, p);
        // translate back
        var result = new double[2];
        result[0] = pp[0][0] + x0;
        result[1] = pp[1][0] + y0;
        return result;
    }

    private static double findMax(double a, double b, double c, double d) {
        var result = Math.max(a, b);
        result = Math.max(result, c);
        result = Math.max(result, d);
        return result;
    }

    private static double findMin(double a, double b, double c, double d) {
        var result = Math.min(a, b);
        result = Math.min(result, c);
        result = Math.min(result, d);
        return result;
    }

    /**
     * Apply color change on an image.
     *
     * @param color
     * @param imageData
     */
    public static ImageData changeImageColor(Color color, ImageData originalImageData) {
        if (color == null || originalImageData == null || color.getRGB().equals(new RGB(0, 0, 0))) {
            return originalImageData;
        }
        var imageData = ImageUtils.convertToGrayscale(originalImageData);

        var luminance = (int) Math
                .round((0.299 * color.getRed()) + (0.587 * color.getGreen()) + (0.114 * color.getBlue()));

        // find min/max/average values ignoring white & transparent
        int sum = 0, count = 0, min = 0, max = 0;
        var lineData = new int[imageData.width];
        var palette = imageData.palette;
        for (var y = 0; y < imageData.height; y++) {
            imageData.getPixels(0, y, imageData.width, lineData, 0);

            // Analyze each pixel value in the line
            for (var x = 0; x < lineData.length; x++) {
                var pixelValue = lineData[x];

                // Do not set transparent pixel
                if (lineData[x] != imageData.transparentPixel) {
                    // Get pixel color value if not using direct palette
                    if (!palette.isDirect) {
                        pixelValue = palette.getPixel(palette.colors[lineData[x]]);
                    }
                    var current = palette.getRGB(pixelValue);
                    if (current.blue == current.green && current.blue == current.red && current.blue < 255) {
                        min = Math.min(current.red, min);
                        max = Math.max(current.red, max);
                        sum += current.red;
                        count++;
                    }
                }
            }
        }
        if (count == 0) {
            return imageData;
        }
        // we need to adjust the gradient depending on the luminance unless
        // bright colors will appear in white
        int gradientWidth = 512, gradientHeight = 10;
        var average = (int) sum / count;
        var start = average - 32;
        if (start < 0) {
            start = 0;
        }
        var end = max + luminance;
        if (end > gradientWidth - 1) {
            end = gradientWidth - 1;
        }

        // create the color gradient
        var color1 = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
        var color2 = new java.awt.Color(250, 250, 255);

        var gradient = new BufferedImage(gradientWidth, gradientHeight, BufferedImage.TYPE_INT_ARGB);
        var g2 = gradient.createGraphics();
        g2.setPaint(color1);
        g2.fill(new java.awt.Rectangle(0, 0, start, gradientHeight));
        g2.setPaint(new GradientPaint(start, 0, color1, end, gradientHeight, color2, false));
        g2.fill(new java.awt.Rectangle(start, 0, end - start, gradientHeight));
        g2.setPaint(color2);
        g2.fill(new java.awt.Rectangle(end, 0, gradientWidth - end, gradientHeight));

        for (var y = 0; y < imageData.height; y++) {
            imageData.getPixels(0, y, imageData.width, lineData, 0);

            // Analyze each pixel value in the line
            for (var x = 0; x < lineData.length; x++) {
                var pixelValue = lineData[x];

                // Do not set transparent pixel
                if (lineData[x] != imageData.transparentPixel) {
                    // Get pixel color value if not using direct palette
                    if (!palette.isDirect) {
                        pixelValue = palette.getPixel(palette.colors[lineData[x]]);
                    }
                    var current = palette.getRGB(pixelValue);
                    if (current.blue == current.green && current.blue == current.red && current.blue < 255) {
                        var gradientRGB = gradient.getRGB(current.red, 0);
                        var gradientColor = new java.awt.Color(gradientRGB);
                        var degraded = new RGB(gradientColor.getRed(), gradientColor.getGreen(),
                                gradientColor.getBlue());
                        if (palette.isDirect) {
                            var appliedColor = palette.getPixel(degraded);
                            imageData.setPixel(x, y, appliedColor);
                        } else {
                            palette.colors[lineData[x]] = degraded;
                        }
                    }
                }
            }
        }
        return imageData;
    }

    public static ImageData changeImageColor2(Color color, ImageData originalImageData) {
        if (color == null || originalImageData == null || color.getRGB().equals(new RGB(0, 0, 0))) {
            return originalImageData;
        }
        var imageData = ImageUtils.convertToGrayscale(originalImageData);
        new Colorizer().doColorize(imageData);
        return imageData;
    }

    /**
     * Crop the given rectangle with the given insets.
     *
     * @param rect
     *            rectangle to crop.
     * @param insets
     */
    public static void crop(Rectangle rect, Insets insets) {
        if (insets == null) {
            return;
        }
        rect.setX(rect.x + insets.left);
        rect.setY(rect.y + insets.top);
        rect.setWidth(rect.width - insets.left - insets.right);
        rect.setHeight(rect.height - insets.top - insets.bottom);
    }

    /**
     * Convert a colored image to grayscale image using average method.
     */
    public static ImageData convertToGrayscale(ImageData originalImageData) {
        var imageData = (ImageData) originalImageData.clone();
        var palette = imageData.palette;
        if (palette.isDirect) {
            var lineData = new int[imageData.width];
            for (var y = 0; y < imageData.height; y++) {
                imageData.getPixels(0, y, imageData.width, lineData, 0);
                // Analyze each pixel value in the line
                for (var x = 0; x < lineData.length; x++) {
                    var rgb = palette.getRGB(lineData[x]);
                    // int gray = (int) Math.round((rgb.red + rgb.green + rgb.blue) / 3);
                    var gray = (int) Math.round((0.21 * rgb.red) + (0.72 * rgb.green) + (0.07 * rgb.blue));
                    var newColor = palette.getPixel(new RGB(gray, gray, gray));
                    imageData.setPixel(x, y, newColor);
                }
            }
            if (imageData.transparentPixel != -1) {
                var rgb = palette.getRGB(imageData.transparentPixel);
                // int gray = (int) Math.round((rgb.red + rgb.green + rgb.blue) / 3);
                var gray = (int) Math.round((0.21 * rgb.red) + (0.72 * rgb.green) + (0.07 * rgb.blue));
                var newColor = palette.getPixel(new RGB(gray, gray, gray));
                imageData.transparentPixel = newColor;
            }
        } else {
            for (var i = 0; i < palette.colors.length; i++) {
                var rgb = palette.colors[i];
                // int gray = (int) Math.round((rgb.red + rgb.green + rgb.blue) / 3);
                var gray = (int) Math.round((0.21 * rgb.red) + (0.72 * rgb.green) + (0.07 * rgb.blue));
                palette.colors[i] = new RGB(gray, gray, gray);
            }
        }
        return imageData;
    }

    // ************************************************************
    // Old method to change image color
    // ************************************************************

    public static void oldChangeImageColor(Color color, ImageData originalImageData) {
        if (color == null || originalImageData == null || color.getRGB().equals(new RGB(0, 0, 0))) {
            return;
        }
        var imageData = ImageUtils.convertToGrayscale(originalImageData);
        var newColor = 0;
        var lineData = new int[imageData.width];
        // Calculate pixel value (integer)
        if (imageData.palette.isDirect) {
            var rgb = color.getRGB();

            var redMask = imageData.palette.redMask;
            var blueMask = imageData.palette.blueMask;
            var greenMask = imageData.palette.greenMask;

            var redShift = imageData.palette.redShift;
            var greenShift = imageData.palette.greenShift;
            var blueShift = imageData.palette.blueShift;

            newColor |= (redShift < 0 ? rgb.red << -redShift : rgb.red >>> redShift) & redMask;
            newColor |= (greenShift < 0 ? rgb.green << -greenShift : rgb.green >>> greenShift) & greenMask;
            newColor |= (blueShift < 0 ? rgb.blue << -blueShift : rgb.blue >>> blueShift) & blueMask;
        } else {
            // Add new color in PaletteData colors
            var paletteLength = imageData.palette.colors.length;
            newColor = (imageData.transparentPixel + 1) % paletteLength;
            imageData.palette.colors[newColor] = color.getRGB();
        }
        for (var y = 0; y < imageData.height; y++) {
            imageData.getPixels(0, y, imageData.width, lineData, 0);

            // Analyze each pixel value in the line
            for (var x = 0; x < lineData.length; x++) {
                // Do not set transparent pixel && change only black pixel
                var pixelValue = lineData[x];
                if (!imageData.palette.isDirect) {
                    pixelValue = imageData.palette.getPixel(imageData.palette.colors[lineData[x]]);
                }
                if (lineData[x] != imageData.transparentPixel && isShadeOfGray(pixelValue, imageData.palette)) {
                    var appliedColor = applyShade(pixelValue, newColor, imageData.palette);
                    if (imageData.alphaData == null) {
                        // appliedColor = applyShade(pixelValue, newColor, imageData.palette);
                    }
                    imageData.setPixel(x, y, appliedColor);
                }
            }
        }
    }

    private static boolean isShadeOfGray(int pixel, PaletteData palette) {
        var r = (pixel & palette.redMask) >> palette.redShift;
        var g = (pixel & palette.greenMask) >> palette.greenShift;
        var b = (pixel & palette.blueMask) >> palette.blueShift;
        return (r == g) && (g == b);
    }

    private static int applyShade(int shadedPixel, int pixelToShade, PaletteData palette) {
        int newColor = 0, redMask = palette.redMask, blueMask = palette.blueMask, greenMask = palette.greenMask,
                redShift = palette.redShift, greenShift = palette.greenShift, blueShift = palette.blueShift;

        var ratioR = ((shadedPixel & redMask) >> redShift) / 255f;
        var ratioG = ((shadedPixel & greenMask) >> greenShift) / 255f;
        var ratioB = ((shadedPixel & blueMask) >> blueShift) / 255f;

        var r = (pixelToShade & redMask) >> redShift;
        var g = (pixelToShade & greenMask) >> greenShift;
        var b = (pixelToShade & blueMask) >> blueShift;
        r = (int) Math.round(r * ratioR);
        g = (int) Math.round(g * ratioG);
        b = (int) Math.round(b * ratioB);
        if (r < 0) {
            r = 0;
        } else if (r > 255) {
            r = 255;
        }
        if (g < 0) {
            g = 0;
        } else if (g > 255) {
            g = 255;
        }
        if (b < 0) {
            b = 0;
        } else if (b > 255) {
            b = 255;
        }

        newColor |= (redShift < 0 ? r << -redShift : r >>> redShift) & redMask;
        newColor |= (greenShift < 0 ? g << -greenShift : g >>> greenShift) & greenMask;
        newColor |= (blueShift < 0 ? b << -blueShift : b >>> blueShift) & blueMask;
        return newColor;
    }

    // ************************************************************
    // Another old method to change image color
    // ************************************************************

    public static ImageData oldChangeImageColor2(Color color, ImageData originalImageData) {
        if (color == null || originalImageData == null || color.getRGB().equals(new RGB(0, 0, 0))) {
            return originalImageData;
        }
        var imageData = ImageUtils.convertToGrayscale(originalImageData);

        var hsb = new float[3];
        java.awt.Color.RGBtoHSB(color.getRGB().red, color.getRGB().green, color.getRGB().blue, hsb);
        var lineData = new int[imageData.width];
        var palette = imageData.palette;

        for (var y = 0; y < imageData.height; y++) {
            imageData.getPixels(0, y, imageData.width, lineData, 0);

            // Analyze each pixel value in the line
            for (var x = 0; x < lineData.length; x++) {
                var pixelValue = lineData[x];

                // Do not set transparent pixel
                if (lineData[x] != imageData.transparentPixel) {
                    // Get pixel color value if not using direct palette
                    if (!palette.isDirect) {
                        pixelValue = palette.getPixel(palette.colors[lineData[x]]);
                    }
                    var current = palette.getRGB(pixelValue);
                    if (current.blue == current.green && current.blue == current.red && current.blue < 255) {
                        var pixelHSB = new float[3];
                        java.awt.Color.RGBtoHSB(current.red, current.green, current.blue, pixelHSB);
                        var awtRGB = java.awt.Color.HSBtoRGB(hsb[0], hsb[1], 1 - pixelHSB[2]);
                        var awtColor = new java.awt.Color(awtRGB);
                        var degraded = new RGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
                        if (palette.isDirect) {
                            var appliedColor = palette.getPixel(degraded);
                            imageData.setPixel(x, y, appliedColor);
                        } else {
                            palette.colors[lineData[x]] = degraded;
                        }
                    }
                }
            }
        }
        return imageData;
    }
}
