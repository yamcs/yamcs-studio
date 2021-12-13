/********************************************************************************
 * Copyright (c) 2008, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.FilePathProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.MatrixProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgets.FigureTransparencyHelper;
import org.csstudio.swt.widgets.symbol.util.PermutationMatrix;
import org.eclipse.core.runtime.Path;

/**
 * An image widget model.
 */
public final class ImageModel extends AbstractWidgetModel {
    /**
     * Unique identifier.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.Image";

    /**
     * File path of the image.
     */
    public static final String PROP_IMAGE_FILE = "image_file";
    /**
     * Crop part (in pixels) on top side of the image.
     */
    public static final String PROP_TOPCROP = "crop_top";
    /**
     * Crop part (in pixels) on bottom side of the image.
     */
    public static final String PROP_BOTTOMCROP = "crop_bottom";
    /**
     * Crop part (in pixels) on left side of the image.
     */
    public static final String PROP_LEFTCROP = "crop_left";
    /**
     * Crop part (in pixels) on right side of the image.
     */
    public static final String PROP_RIGHTCROP = "crop_right";
    /**
     * True if the image should be stretched to the widget size.
     */
    public static final String PROP_STRETCH = "stretch_to_fit";
    /**
     * True if the widget size is automatically adjusted to the size of the image.
     */
    public static final String PROP_AUTOSIZE = "auto_size";

    /**
     * True if the widget doesn't show animation even it is a animated image file.
     */
    public static final String PROP_NO_ANIMATION = "no_animation";

    /**
     * True if the widget animation start should be aligned to the nearest second.
     */
    public static final String PROP_ALIGN_TO_NEAREST_SECOND = "align_to_nearest_second";

    /**
     * The default value for the file extensions.
     */
    private static final String[] FILE_EXTENSIONS = new String[] { "jpg", "jpeg", "gif", "bmp", "png", "svg" };

    /**
     * Degree value of the image.
     */
    public static final String PROP_DEGREE = "degree";
    /**
     * Horizontal flip applied on the image.
     */
    public static final String PROP_FLIP_HORIZONTAL = "flip_horizontal";
    /**
     * Vertical flip applied on the image.
     */
    public static final String PROP_FLIP_VERTICAL = "flip_vertical";

    /**
     * Image disposition (permutation matrix)
     */
    public static final String PERMUTATION_MATRIX = "permutation_matrix";

    private static final String[] allowedDegrees = new String[] { "0", "90", "180", "270" };

    @Override
    public String getTypeID() {
        return ID;
    }

    @Override
    protected void configureProperties() {
        addProperty(
                new FilePathProperty(PROP_IMAGE_FILE, "Image File", WidgetPropertyCategory.Basic, "", FILE_EXTENSIONS));
        addProperty(new IntegerProperty(PROP_TOPCROP, "Crop Top", WidgetPropertyCategory.Image, 0));
        addProperty(new IntegerProperty(PROP_BOTTOMCROP, "Crop Bottom", WidgetPropertyCategory.Image, 0));
        addProperty(new IntegerProperty(PROP_LEFTCROP, "Crop Left", WidgetPropertyCategory.Image, 0));
        addProperty(new IntegerProperty(PROP_RIGHTCROP, "Crop Right", WidgetPropertyCategory.Image, 0));
        addProperty(new BooleanProperty(PROP_STRETCH, "Stretch to Fit", WidgetPropertyCategory.Image, false));
        addProperty(new BooleanProperty(PROP_AUTOSIZE, "Auto Size", WidgetPropertyCategory.Image, true));
        addProperty(new BooleanProperty(PROP_NO_ANIMATION, "No Animation", WidgetPropertyCategory.Image, false));
        addProperty(new BooleanProperty(PROP_ALIGN_TO_NEAREST_SECOND, "Animation aligned to the nearest second",
                WidgetPropertyCategory.Image, false));
        addProperty(new ComboProperty(PROP_DEGREE, "Rotation Angle", WidgetPropertyCategory.Image, allowedDegrees, 0));
        addProperty(new BooleanProperty(PROP_FLIP_HORIZONTAL, "Flip Horizontal", WidgetPropertyCategory.Image, false));
        addProperty(new BooleanProperty(PROP_FLIP_VERTICAL, "Flip Vertical", WidgetPropertyCategory.Image, false));
        addProperty(new MatrixProperty(PERMUTATION_MATRIX, "Permutation Matrix", WidgetPropertyCategory.Image,
                PermutationMatrix.generateIdentityMatrix().getMatrix()));
        setPropertyVisibleAndSavable(PERMUTATION_MATRIX, false, true);

        FigureTransparencyHelper.addProperty(this);
    }

    /**
     * Returns the path to the specified file.
     */
    public String getFilename() {
        var absolutePath = (String) getProperty(PROP_IMAGE_FILE).getPropertyValue();
        if (!absolutePath.contains("://")) {
            var path = Path.fromPortableString(absolutePath);
            if (!path.isAbsolute()) {
                path = ResourceUtil.buildAbsolutePath(this, path);
                absolutePath = path.toPortableString();
            }
        }
        return absolutePath;
    }

    /**
     * Returns the amount of pixels, which should be cropped from the top edge of the image.
     */
    public int getTopCrop() {
        return (Integer) getProperty(PROP_TOPCROP).getPropertyValue();
    }

    /**
     * Returns the amount of pixels, which should be cropped from the bottom edge of the image.
     */
    public int getBottomCrop() {
        return (Integer) getProperty(PROP_BOTTOMCROP).getPropertyValue();
    }

    /**
     * Returns the amount of pixels, which should be cropped from the left edge of the image.
     */
    public int getLeftCrop() {
        return (Integer) getProperty(PROP_LEFTCROP).getPropertyValue();
    }

    /**
     * Returns the amount of pixels, which should be cropped from the right edge of the image.
     */
    public int getRightCrop() {
        return (Integer) getProperty(PROP_RIGHTCROP).getPropertyValue();
    }

    /**
     * Returns if the image should be stretched.
     */
    public boolean getStretch() {
        return (Boolean) getProperty(PROP_STRETCH).getPropertyValue();
    }

    /**
     * @return True if the widget should be auto sized according the image size.
     */
    public boolean isAutoSize() {
        return (Boolean) getProperty(PROP_AUTOSIZE).getPropertyValue();
    }

    /**
     * @return True if the animation is stopped.
     */
    public boolean isStopAnimation() {
        return (Boolean) getProperty(PROP_NO_ANIMATION).getPropertyValue();
    }

    public boolean isAlignedToNearestSecond() {
        return (Boolean) getProperty(PROP_ALIGN_TO_NEAREST_SECOND).getPropertyValue();
    }

    /**
     * @return The permutation matrix
     */
    public PermutationMatrix getPermutationMatrix() {
        return new PermutationMatrix((double[][]) getProperty(PERMUTATION_MATRIX).getPropertyValue());
    }

    public int getDegree(int index) {
        return Integer.valueOf(allowedDegrees[index]);
    }

    @Override
    public void rotate90(boolean clockwise) {
        int index = (Integer) getPropertyValue(PROP_DEGREE);
        if (clockwise) {
            if (index == allowedDegrees.length - 1) {
                index = 0;
            } else {
                index++;
            }
        } else {
            if (index == 0) {
                index = allowedDegrees.length - 1;
            } else {
                index--;
            }
        }
        setPropertyValue(PROP_DEGREE, index);
    }

    @Override
    public void flipHorizontally() {
        boolean oldValue = (Boolean) getPropertyValue(PROP_FLIP_HORIZONTAL);
        setPropertyValue(PROP_FLIP_HORIZONTAL, !oldValue);
    }

    @Override
    public void flipVertically() {
        boolean oldValue = (Boolean) getPropertyValue(ImageModel.PROP_FLIP_VERTICAL);
        setPropertyValue(ImageModel.PROP_FLIP_VERTICAL, !oldValue);
    }
}
