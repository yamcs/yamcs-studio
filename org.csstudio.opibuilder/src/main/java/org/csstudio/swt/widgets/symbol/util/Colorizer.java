/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.symbol.util;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

public class Colorizer {

    public static final int MAX_COLOR = 256;

    public static final float LUMINANCE_RED = 0.2126f;
    public static final float LUMINANCE_GREEN = 0.7152f;
    public static final float LUMINANCE_BLUE = 0.0722f;

    double hue = 300;
    double saturation = 50;
    double lightness = 78;

    int[] lum_red_lookup;
    int[] lum_green_lookup;
    int[] lum_blue_lookup;

    int[] final_red_lookup;
    int[] final_green_lookup;
    int[] final_blue_lookup;

    public Colorizer() {
        doInit();
    }

    public void doHSB(double t_hue, double t_sat, double t_bri, ImageData image) {
        hue = t_hue;
        saturation = t_sat;
        lightness = t_bri;
        doInit();
        doColorize(image);
    }

    private void doInit() {
        lum_red_lookup = new int[MAX_COLOR];
        lum_green_lookup = new int[MAX_COLOR];
        lum_blue_lookup = new int[MAX_COLOR];

        var temp_hue = hue / 360f;
        var temp_sat = saturation / 100f;

        final_red_lookup = new int[MAX_COLOR];
        final_green_lookup = new int[MAX_COLOR];
        final_blue_lookup = new int[MAX_COLOR];

        for (var i = 0; i < MAX_COLOR; ++i) {
            lum_red_lookup[i] = (int) (i * LUMINANCE_RED);
            lum_green_lookup[i] = (int) (i * LUMINANCE_GREEN);
            lum_blue_lookup[i] = (int) (i * LUMINANCE_BLUE);

            var temp_light = (double) i / 255f;

            var color = new Color(Color.HSBtoRGB((float) temp_hue, (float) temp_sat, (float) temp_light));

            final_red_lookup[i] = (color.getRed());
            final_green_lookup[i] = (color.getGreen());
            final_blue_lookup[i] = (color.getBlue());
        }
    }

    public void doColorize(ImageData image) {
        var lineData = new int[image.width];
        var palette = image.palette;

        for (var y = 0; y < image.height; y++) {
            image.getPixels(0, y, image.width, lineData, 0);

            // Analyze each pixel value in the line
            for (var x = 0; x < lineData.length; x++) {
                var pixelValue = lineData[x];

                // Do not set transparent pixel
                if (lineData[x] != image.transparentPixel) {
                    // Get pixel color value if not using direct palette
                    if (!palette.isDirect) {
                        pixelValue = palette.getPixel(palette.colors[lineData[x]]);
                    }
                    var current = palette.getRGB(pixelValue);
                    if (current.blue == current.green && current.blue == current.red && current.blue < 255) {
                        var color = new Color(current.red, current.green, current.blue);

                        var lum = lum_red_lookup[color.getRed()] + lum_green_lookup[color.getGreen()]
                                + lum_blue_lookup[color.getBlue()];

                        if (lightness > 0) {
                            lum = (int) (lum * (100f - lightness) / 100f);
                            lum += 255f - (100f - lightness) * 255f / 100f;
                        } else if (lightness < 0) {
                            lum = (int) ((lum * (lightness + 100f)) / 100f);
                        }
                        var final_color = new Color(final_red_lookup[lum], final_green_lookup[lum],
                                final_blue_lookup[lum]);
                        var degraded = new RGB(final_color.getRed(), final_color.getGreen(), final_color.getBlue());
                        if (palette.isDirect) {
                            var appliedColor = palette.getPixel(degraded);
                            image.setPixel(x, y, appliedColor);
                        } else {
                            palette.colors[lineData[x]] = degraded;
                        }
                    }
                }
            }
        }
    }

    public BufferedImage changeContrast(BufferedImage inImage, float increasingFactor) {
        var w = inImage.getWidth();
        var h = inImage.getHeight();

        var outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (var i = 0; i < w; i++) {
            for (var j = 0; j < h; j++) {
                var color = new Color(inImage.getRGB(i, j), true);
                int r, g, b, a;
                float fr, fg, fb;

                r = color.getRed();
                fr = (r - 128) * increasingFactor + 128;
                r = (int) fr;
                r = keep256(r);

                g = color.getGreen();
                fg = (g - 128) * increasingFactor + 128;
                g = (int) fg;
                g = keep256(g);

                b = color.getBlue();
                fb = (b - 128) * increasingFactor + 128;
                b = (int) fb;
                b = keep256(b);

                a = color.getAlpha();

                outImage.setRGB(i, j, new Color(r, g, b, a).getRGB());
            }
        }
        return outImage;
    }

    public BufferedImage changeGreen(BufferedImage inImage, int increasingFactor) {
        var w = inImage.getWidth();
        var h = inImage.getHeight();

        var outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (var i = 0; i < w; i++) {
            for (var j = 0; j < h; j++) {
                var color = new Color(inImage.getRGB(i, j), true);
                int r, g, b, a;
                r = color.getRed();
                g = keep256(color.getGreen() + increasingFactor);
                b = color.getBlue();
                a = color.getAlpha();
                outImage.setRGB(i, j, new Color(r, g, b, a).getRGB());
            }
        }
        return outImage;
    }

    public BufferedImage changeBlue(BufferedImage inImage, int increasingFactor) {
        var w = inImage.getWidth();
        var h = inImage.getHeight();

        var outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (var i = 0; i < w; i++) {
            for (var j = 0; j < h; j++) {
                var color = new Color(inImage.getRGB(i, j), true);
                int r, g, b, a;
                r = color.getRed();
                g = color.getGreen();
                b = keep256(color.getBlue() + increasingFactor);
                a = color.getAlpha();
                outImage.setRGB(i, j, new Color(r, g, b, a).getRGB());
            }
        }
        return outImage;
    }

    public BufferedImage changeRed(BufferedImage inImage, int increasingFactor) {
        var w = inImage.getWidth();
        var h = inImage.getHeight();

        var outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (var i = 0; i < w; i++) {
            for (var j = 0; j < h; j++) {
                var color = new Color(inImage.getRGB(i, j), true);
                int r, g, b, a;
                r = keep256(color.getRed() + increasingFactor);
                g = color.getGreen();
                b = color.getBlue();
                a = color.getAlpha();
                outImage.setRGB(i, j, new Color(r, g, b, a).getRGB());
            }
        }
        return outImage;
    }

    public BufferedImage changeBrightness(BufferedImage inImage, int increasingFactor) {
        var w = inImage.getWidth();
        var h = inImage.getHeight();

        var outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (var i = 0; i < w; i++) {
            for (var j = 0; j < h; j++) {
                var color = new Color(inImage.getRGB(i, j), true);
                int r, g, b, a;
                r = keep256(color.getRed() + increasingFactor);
                g = keep256(color.getGreen() + increasingFactor);
                b = keep256(color.getBlue() + increasingFactor);
                a = color.getAlpha();
                outImage.setRGB(i, j, new Color(r, g, b, a).getRGB());
            }
        }
        return outImage;
    }

    public int keep256(int i) {
        if (i <= 255 && i >= 0) {
            return i;
        }
        if (i > 255) {
            return 255;
        }
        return 0;
    }
}
