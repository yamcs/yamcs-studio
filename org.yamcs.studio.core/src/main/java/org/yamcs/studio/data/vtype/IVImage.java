/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.data.vtype;

public class IVImage extends IVMetadata implements VImage {

    private final int height;
    private final int width;
    private final ListNumber data;
    private final VImageDataType imageDataType;
    private final VImageType imageType;

    public IVImage(int height, int width, ListNumber data, VImageDataType imageDataType, VImageType imageType,
            Alarm alarm, Time time) {
        super(alarm, time);
        this.height = height;
        this.width = width;
        this.data = data;
        this.imageDataType = imageDataType;
        this.imageType = imageType;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public ListNumber getData() {
        return data;
    }

    @Override
    public VImageDataType getDataType() {
        return imageDataType;
    }

    @Override
    public VImageType getVImageType() {
        return imageType;
    }
}
