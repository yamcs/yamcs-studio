package org.yamcs.studio.data.vtype;

/**
 * Represent an image. Use {@link ValueUtil#toImage(org.yamcs.studio.data.vtype.VImage) } and
 * {@link ValueUtil#toVImage(java.awt.image.BufferedImage)} to convert objects of this class to and from awt images.
 * 
 *
 * @author carcassi
 */
public interface VImage extends VType, Alarm, Time {

    /**
     * Height of the image in pixels.
     *
     * @return image height
     */
    public int getHeight();

    /**
     * Width of the image in pixels.
     *
     * @return image width
     */
    public int getWidth();

    /**
     * Image data;
     *
     * @return image data
     */
    public ListNumber getData();

    /**
     * Describes the type in which the data is stored {@link VImageDataType}
     * 
     * @return image data type
     */
    public VImageDataType getDataType();

    /**
     * Returns the image type, The image type describes the mechanism in which the data is encoded and how it can be
     * converted to something that can be rendered.
     * 
     * @return the image type {@link VImageType}
     */
    public VImageType getVImageType();

}
