/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.util;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * A converter between AWT BufferedImage and SWT ImageData. By taking use of this converter, it is possible to use
 * Java2D in SWT or Draw2D.
 */
public class AWT2SWTImageConverter {

    static BufferedImage convertToAWT(ImageData data) {
        ColorModel colorModel = null;
        var palette = data.palette;
        if (palette.isDirect) {
            colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
            var bufferedImage = new BufferedImage(colorModel,
                    colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
            for (var y = 0; y < data.height; y++) {
                for (var x = 0; x < data.width; x++) {
                    var pixel = data.getPixel(x, y);
                    var rgb = palette.getRGB(pixel);
                    bufferedImage.setRGB(x, y, rgb.red << 16 | rgb.green << 8 | rgb.blue);
                }
            }
            return bufferedImage;
        } else {
            var rgbs = palette.getRGBs();
            var red = new byte[rgbs.length];
            var green = new byte[rgbs.length];
            var blue = new byte[rgbs.length];
            for (var i = 0; i < rgbs.length; i++) {
                var rgb = rgbs[i];
                red[i] = (byte) rgb.red;
                green[i] = (byte) rgb.green;
                blue[i] = (byte) rgb.blue;
            }
            if (data.transparentPixel != -1) {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
            } else {
                colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
            }
            var bufferedImage = new BufferedImage(colorModel,
                    colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
            var raster = bufferedImage.getRaster();
            var pixelArray = new int[1];
            for (var y = 0; y < data.height; y++) {
                for (var x = 0; x < data.width; x++) {
                    var pixel = data.getPixel(x, y);
                    pixelArray[0] = pixel;
                    raster.setPixel(x, y, pixelArray);
                }
            }
            return bufferedImage;
        }
    }

    static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            var colorModel = (DirectColorModel) bufferedImage.getColorModel();
            var palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
            var data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            for (var y = 0; y < data.height; y++) {
                for (var x = 0; x < data.width; x++) {
                    var rgb = bufferedImage.getRGB(x, y);
                    var pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            var colorModel = (IndexColorModel) bufferedImage.getColorModel();
            var size = colorModel.getMapSize();
            var reds = new byte[size];
            var greens = new byte[size];
            var blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            var rgbs = new RGB[size];
            for (var i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            var palette = new PaletteData(rgbs);
            var data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            var raster = bufferedImage.getRaster();
            var pixelArray = new int[1];
            for (var y = 0; y < data.height; y++) {
                for (var x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        return null;
    }
}
