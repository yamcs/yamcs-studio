/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.properties.ActionsProperty;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.FilePathProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgets.FigureTransparencyHelper;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.Path;

/**
 * The widget model for Boolean Switch.
 */
public class ImageBoolButtonModel extends AbstractBoolControlModel {

    /**
     * Image on the button when it is on.
     */
    public static final String PROP_ON_IMAGE = "on_image";
    /**
     * Image on the button when it is off.
     */
    public static final String PROP_OFF_IMAGE = "off_image";

    /**
     * True if the image should be stretched to the button size.
     */
    public static final String PROP_STRETCH = "stretch_to_fit";
    /**
     * True if the button size is automatically adjusted to the size of the image.
     */
    public static final String PROP_AUTOSIZE = "auto_size";

    private static final String[] FILE_EXTENSIONS = new String[] { "jpg", "jpeg", "gif", "bmp", "png", "svg" };

    /**
     * True if the widget doesn't show animation even it is a animated image file.
     */
    public static final String PROP_NO_ANIMATION = "no_animation";

    /**
     * True if the widget animation start should be aligned to the nearest second.
     */
    public static final String PROP_ALIGN_TO_NEAREST_SECOND = "align_to_nearest_second";

    public static final String PROP_TRANSPARENCY = "transparency";

    public ImageBoolButtonModel() {
        setForegroundColor(CustomMediaFactory.COLOR_BLACK);
    }

    @Override
    protected void configureProperties() {
        super.configureProperties();
        addProperty(new FilePathProperty(PROP_ON_IMAGE, "On Image", WidgetPropertyCategory.Image, "", FILE_EXTENSIONS));
        addProperty(
                new FilePathProperty(PROP_OFF_IMAGE, "Off Image", WidgetPropertyCategory.Image, "", FILE_EXTENSIONS));
        addProperty(new BooleanProperty(PROP_STRETCH, "Stretch to Fit", WidgetPropertyCategory.Image, false));
        addProperty(new BooleanProperty(PROP_AUTOSIZE, "Auto Size", WidgetPropertyCategory.Image, true));
        addProperty(new BooleanProperty(PROP_NO_ANIMATION, "No Animation", WidgetPropertyCategory.Image, false));
        addProperty(new BooleanProperty(PROP_ALIGN_TO_NEAREST_SECOND, "Animation aligned to the nearest second",
                WidgetPropertyCategory.Image, false));

        removeProperty(PROP_ACTIONS);
        addProperty(new ActionsProperty(PROP_ACTIONS, "Actions", WidgetPropertyCategory.Behavior, false));
        setPropertyVisible(PROP_ON_COLOR, false);
        setPropertyVisible(PROP_OFF_COLOR, false);

        FigureTransparencyHelper.addProperty(this);
    }

    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.ImageBoolButton";

    @Override
    public String getTypeID() {
        return ID;
    }

    /**
     * Returns if the image should be stretched.
     *
     * @return True is it should be stretched, false otherwise
     */
    public boolean isStretch() {
        return (Boolean) getProperty(PROP_STRETCH).getPropertyValue();
    }

    /**
     * @return True if the widget should be auto sized according the image size.
     */
    public boolean isAutoSize() {
        return (Boolean) getProperty(PROP_AUTOSIZE).getPropertyValue();
    }

    /**
     * @return the path of the on image.
     */
    public String getOnImagePath() {
        var absolutePath = (String) getProperty(PROP_ON_IMAGE).getPropertyValue();
        if (absolutePath == null || absolutePath.isEmpty()) {
            return absolutePath;
        }
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
     * @return the path of the off image.
     */
    public String getOffImagePath() {
        var absolutePath = (String) getProperty(PROP_OFF_IMAGE).getPropertyValue();
        if (absolutePath == null || absolutePath.isEmpty()) {
            return absolutePath;
        }
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
     * @return True if the animation is stopped.
     */
    public boolean isStopAnimation() {
        return (Boolean) getProperty(PROP_NO_ANIMATION).getPropertyValue();
    }

    public boolean isAlignedToNearestSecond() {
        return (Boolean) getProperty(PROP_ALIGN_TO_NEAREST_SECOND).getPropertyValue();
    }
}
