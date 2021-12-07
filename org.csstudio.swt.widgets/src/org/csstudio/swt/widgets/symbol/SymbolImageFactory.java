/********************************************************************************
 * Copyright (c) 2010 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.symbol;

public class SymbolImageFactory {

    public static SymbolImage asynCreateSymbolImage(String imagePath, boolean runMode, SymbolImageProperties sip,
            SymbolImageListener listener) {
        if (imagePath == null || imagePath.isEmpty()) {
            return createEmptyImage(runMode);
        }
        var symbolImage = createImageFromPath(imagePath, sip, runMode);
        symbolImage.setListener(listener);
        symbolImage.asyncLoadImage();
        return symbolImage;
    }

    public static SymbolImage synCreateSymbolImage(String imagePath, boolean runMode, SymbolImageProperties sip) {
        if (imagePath == null || imagePath.isEmpty()) {
            return createEmptyImage(runMode);
        }
        var symbolImage = createImageFromPath(imagePath, sip, runMode);
        symbolImage.syncLoadImage();
        return symbolImage;
    }

    public static SymbolImage createEmptyImage(boolean runMode) {
        return new PNGSymbolImage(null, runMode);
    }

    private static SymbolImage createImageFromPath(String imagePath, SymbolImageProperties props, boolean runMode) {
        SymbolImage symbolImage = null;
        var idx = imagePath.lastIndexOf('.');
        if (idx != -1) {
            var ext = imagePath.substring(idx).toLowerCase();
            switch (ext) {
            case ".png":
            case ".jpg":
            case ".jpeg":
            case ".bmp":
                symbolImage = new PNGSymbolImage(props, runMode);
                symbolImage.setImagePath(imagePath);
                return symbolImage;
            case ".svg":
                symbolImage = new SVGSymbolImage(props, runMode);
                symbolImage.setImagePath(imagePath);
                return symbolImage;
            case ".gif":
                symbolImage = new GIFSymbolImage(props, runMode);
                symbolImage.setImagePath(imagePath);
                return symbolImage;
            }
        }

        return createEmptyImage(runMode);
    }
}
