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

/**
 * Represent an image. Use {@link ValueUtil#toImage(org.yamcs.studio.data.vtype.VImage) } and
 * {@link ValueUtil#toVImage(java.awt.image.BufferedImage)} to convert objects of this class to and from awt images.
 *
 */
public interface VImage extends VType, Alarm, Time {

    /**
     * Height of the image in pixels.
     *
     * @return image height
     */
    int getHeight();

    /**
     * Width of the image in pixels.
     *
     * @return image width
     */
    int getWidth();

    /**
     * Image data;
     *
     * @return image data
     */
    ListNumber getData();

    /**
     * Describes the type in which the data is stored {@link VImageDataType}
     *
     * @return image data type
     */
    VImageDataType getDataType();

    /**
     * Returns the image type, The image type describes the mechanism in which the data is encoded and how it can be
     * converted to something that can be rendered.
     *
     * @return the image type {@link VImageType}
     */
    VImageType getVImageType();
}
