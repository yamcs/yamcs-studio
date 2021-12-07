/********************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.ComboProperty;
import org.csstudio.opibuilder.properties.DoubleProperty;
import org.csstudio.opibuilder.properties.FontProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.NameDefinedCategory;
import org.csstudio.opibuilder.properties.PVNameProperty;
import org.csstudio.opibuilder.properties.PVValueProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.MediaService;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.UpgradeUtil;
import org.csstudio.opibuilder.widgets.properties.ColorMapProperty;
import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.csstudio.swt.widgets.figures.IntensityGraphFigure.ColorDepth;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.Version;

/**
 * The model for intensity graph.
 */
public class IntensityGraphModel extends AbstractPVWidgetModel {

    public static final String Y_AXIS_ID = "y_axis";

    public static final String X_AXIS_ID = "x_axis";

    public static final int MAX_ROIS_AMOUNT = 5;

    public enum AxisProperty {
        TITLE("axis_title", "Axis Title"),
        TITLE_FONT("title_font", "Title Font"),
        SCALE_FONT("scale_font", "Scale Font"),
        AXIS_COLOR("axis_color", "Axis Color"),
        SHOW_MINOR_TICKS("show_minor_ticks", "Show Minor Ticks"),
        MAJOR_TICK_STEP_HINT("major_tick_step_hint", "Major Tick Step Hint"),
        MAX("maximum", "Maximum"),
        MIN("minimum", "Minimum"),
        VISIBLE("visible", "Visible");

        public String propIDPre;
        public String description;

        private AxisProperty(String propertyIDPrefix, String description) {
            this.propIDPre = propertyIDPrefix;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public enum ROIProperty {
        TITLE("title", "Title"),
        VISIBLE("visible", "Visible"),
        XPV("x_pv", "X PV"),
        YPV("y_pv", "Y PV"),
        XPV_VALUE("x_pv_value", "X PV Value"),
        YPV_VALUE("y_pv_value", "Y PV Value"),
        WPV("width_pv", "Width PV"),
        HPV("height_pv", "Height PV"),
        WPV_VALUE("w_pv_value", "W PV Value"),
        HPV_VALUE("h_pv_value", "H PV Value");

        public String propIDPre;
        public String description;

        private ROIProperty(String propertyIDPrefix, String description) {
            this.propIDPre = propertyIDPrefix;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * The lower limit of the value in the input data array.
     */
    public static final String PROP_MIN = "minimum";

    /**
     * The upper limit of the value in the input data array.
     */
    public static final String PROP_MAX = "maximum";

    /**
     * Number of columns of the input data.
     */
    public static final String PROP_DATA_WIDTH = "data_width";

    /**
     * Number of rows of the input data.
     */
    public static final String PROP_DATA_HEIGHT = "data_height";

    /**
     * Width of the intensity graph area.
     */
    public static final String PROP_GRAPH_AREA_WIDTH = "graph_area_width";

    /**
     * Height of the intensity graph area.
     */
    public static final String PROP_GRAPH_AREA_HEIGHT = "graph_area_height";

    /**
     * Color map of the graph.
     */
    public static final String PROP_COLOR_MAP = "color_map";

    /**
     * Show Ramp.
     */
    public static final String PROP_SHOW_RAMP = "show_ramp";

    /**
     * Left cropped part of the source data.
     */
    public static final String PROP_CROP_LEFT = "crop_left";
    /**
     * Right cropped part of the source data.
     */
    public static final String PROP_CROP_RIGHT = "crop_right";
    /**
     * Top cropped part of the source data.
     */
    public static final String PROP_CROP_TOP = "crop_top";
    /**
     * Bottom cropped part of the source data.
     */
    public static final String PROP_CROP_BOTTOM = "crop_bottom";

    /**
     * The output PV to which the horizontal profile data on X axis will be written.
     */
    public static final String PROP_HORIZON_PROFILE_X_PV_NAME = "horizon_profile_x_pv_name";
    public static final String PROP_HORIZON_PROFILE_X_PV_VALUE = "horizon_profile_x_pv_value";
    /**
     * The output PV to which the vertical profile data on X axis will be written.
     */
    public static final String PROP_VERTICAL_PROFILE_X_PV_NAME = "vertical_profile_x_pv_name";
    public static final String PROP_VERTICAL_PROFILE_X_PV_VALUE = "vertial_profile_x_pv_value";
    /**
     * The output PV to which the horizontal profile data on Y axis will be written.
     */
    public static final String PROP_HORIZON_PROFILE_Y_PV_NAME = "horizon_profile_y_pv_name";
    public static final String PROP_HORIZON_PROFILE_Y_PV_VALUE = "horizon_profile_y_pv_value";
    /**
     * The output PV to which the vertical profile data on Y axis will be written.
     */
    public static final String PROP_VERTICAL_PROFILE_Y_PV_NAME = "vertical_profile_y_pv_name";
    public static final String PROP_VERTICAL_PROFILE_Y_PV_VALUE = "vertial_profile_y_pv_value";

    /** PV to which information about the pixel at the cursor location is written */
    public static final String PROP_PIXEL_INFO_PV_NAME = "pixel_info_pv_name";
    public static final String PROP_PIXEL_INFO_PV_VALUE = "pixel_info_pv_value";

    public static final String PROP_RGB_MODE = "rgb_mode";

    public static final String PROP_COLOR_DEPTH = "color_depth";

    public static final String PROP_SINGLE_LINE_PROFILING = "single_line_profiling";

    public static final String PROP_ROI_COLOR = "roi_color";

    public static final String PROP_ROI_COUNT = "roi_count";

    /** The default value of the minimum property. */
    private static double DEFAULT_MIN = 0;

    /** The default value of the maximum property. */
    private static double DEFAULT_MAX = 255;

    /** The default color of the axis color property. */
    private static final RGB DEFAULT_AXIS_COLOR = new RGB(0, 0, 0);
    /**
     * The ID of this widget model.
     */
    public static final String ID = "org.csstudio.opibuilder.widgets.intensityGraph";

    public IntensityGraphModel() {
        setForegroundColor(new RGB(0, 0, 0));
        setSize(400, 240);
        setTooltip("$(pv_name)");
        setPropertyValue(PROP_BORDER_ALARMSENSITIVE, false);
        setScaleOptions(true, true, true);
    }

    @Override
    protected void configureProperties() {
        addPVProperty(new PVNameProperty(PROP_HORIZON_PROFILE_X_PV_NAME, "Horizon Profile X PV",
                WidgetPropertyCategory.Basic, ""), new PVValueProperty(PROP_HORIZON_PROFILE_X_PV_VALUE, null));

        addPVProperty(new PVNameProperty(PROP_VERTICAL_PROFILE_X_PV_NAME, "Vertical Profile X PV",
                WidgetPropertyCategory.Basic, ""), new PVValueProperty(PROP_VERTICAL_PROFILE_X_PV_VALUE, null));

        addPVProperty(new PVNameProperty(PROP_HORIZON_PROFILE_Y_PV_NAME, "Horizon Profile Y PV",
                WidgetPropertyCategory.Basic, ""), new PVValueProperty(PROP_HORIZON_PROFILE_Y_PV_VALUE, null));

        addPVProperty(new PVNameProperty(PROP_VERTICAL_PROFILE_Y_PV_NAME, "Vertical Profile Y PV",
                WidgetPropertyCategory.Basic, ""), new PVValueProperty(PROP_VERTICAL_PROFILE_Y_PV_VALUE, null));

        addPVProperty(new PVNameProperty(PROP_PIXEL_INFO_PV_NAME, "Pixel Info PV", WidgetPropertyCategory.Basic, ""),
                new PVValueProperty(PROP_PIXEL_INFO_PV_VALUE, null));

        addProperty(new DoubleProperty(PROP_MIN, "Minimum", WidgetPropertyCategory.Behavior, DEFAULT_MIN), true);

        addProperty(new DoubleProperty(PROP_MAX, "Maximum", WidgetPropertyCategory.Behavior, DEFAULT_MAX), true);

        addProperty(new IntegerProperty(PROP_DATA_WIDTH, "Data Width", WidgetPropertyCategory.Behavior, 0), true);

        addProperty(new IntegerProperty(PROP_DATA_HEIGHT, "Data Height", WidgetPropertyCategory.Behavior, 0), true);

        addProperty(new ColorMapProperty(PROP_COLOR_MAP, "Color Map", WidgetPropertyCategory.Display,
                new ColorMap(PredefinedColorMap.JET, true, true)), true);

        addProperty(new BooleanProperty(PROP_SHOW_RAMP, "Show Ramp", WidgetPropertyCategory.Display, true), true);

        addProperty(new IntegerProperty(PROP_GRAPH_AREA_WIDTH, "Graph Area Width", WidgetPropertyCategory.Position, 0),
                true);

        addProperty(
                new IntegerProperty(PROP_GRAPH_AREA_HEIGHT, "Graph Area Height", WidgetPropertyCategory.Position, 0),
                true);

        addProperty(new IntegerProperty(PROP_CROP_LEFT, "Crop Left", WidgetPropertyCategory.Behavior, 0));
        addProperty(new IntegerProperty(PROP_CROP_RIGHT, "Crop Right", WidgetPropertyCategory.Behavior, 0));
        addProperty(new IntegerProperty(PROP_CROP_TOP, "Crop Top", WidgetPropertyCategory.Behavior, 0));
        addProperty(new IntegerProperty(PROP_CROP_BOTTOM, "Crop Bottom", WidgetPropertyCategory.Behavior, 0));

        addProperty(new BooleanProperty(PROP_RGB_MODE, "RGB Mode", WidgetPropertyCategory.Behavior, false), false);

        addProperty(new ComboProperty(PROP_COLOR_DEPTH, "Color Depth", WidgetPropertyCategory.Behavior,
                ColorDepth.stringValues(), 0), true);

        addProperty(new BooleanProperty(PROP_SINGLE_LINE_PROFILING, "Profile on Single Line",
                WidgetPropertyCategory.Behavior, false), true);

        addProperty(new ColorProperty(PROP_ROI_COLOR, "ROI Color", WidgetPropertyCategory.Display,
                CustomMediaFactory.COLOR_CYAN), true);

        addProperty(new IntegerProperty(PROP_ROI_COUNT, "ROI Count", WidgetPropertyCategory.Behavior, 0, 0,
                MAX_ROIS_AMOUNT));

        addAxisProperties();
        addROIProperties();
    }

    @Override
    public void processVersionDifference(Version boyVersionOnFile) {
        super.processVersionDifference(boyVersionOnFile);
        if (UpgradeUtil.VERSION_WITH_PVMANAGER.compareTo(boyVersionOnFile) > 0) {
            setPropertyValue(PROP_HORIZON_PROFILE_X_PV_NAME,
                    UpgradeUtil.convertUtilityPVNameToPM((String) getPropertyValue(PROP_HORIZON_PROFILE_X_PV_NAME)));
            setPropertyValue(PROP_VERTICAL_PROFILE_X_PV_NAME,
                    UpgradeUtil.convertUtilityPVNameToPM((String) getPropertyValue(PROP_VERTICAL_PROFILE_X_PV_NAME)));
            setPropertyValue(PROP_HORIZON_PROFILE_Y_PV_NAME,
                    UpgradeUtil.convertUtilityPVNameToPM((String) getPropertyValue(PROP_HORIZON_PROFILE_Y_PV_NAME)));
            setPropertyValue(PROP_VERTICAL_PROFILE_Y_PV_NAME,
                    UpgradeUtil.convertUtilityPVNameToPM((String) getPropertyValue(PROP_VERTICAL_PROFILE_Y_PV_NAME)));
        }
    }

    public static String makeROIPropID(String propIDPre, int index) {
        return "roi_" + index + "_" + propIDPre;
    }

    private void addROIProperties() {
        for (var i = 0; i < MAX_ROIS_AMOUNT; i++) {
            WidgetPropertyCategory category = new NameDefinedCategory("ROI " + i);
            for (ROIProperty roiProperty : ROIProperty.values()) {
                addROIProperty(roiProperty, i, category);
            }
        }
    }

    private void addROIProperty(ROIProperty roiProperty, int index, WidgetPropertyCategory category) {
        var propID = makeROIPropID(roiProperty.propIDPre, index);
        switch (roiProperty) {
        case TITLE:
            addProperty(new StringProperty(propID, roiProperty.description, category, category.toString()));
            break;
        case VISIBLE:
            addProperty(new BooleanProperty(propID, roiProperty.description, category, true));
            break;
        case XPV:
            addPVProperty(new PVNameProperty(propID, roiProperty.description, category, ""),
                    new PVValueProperty(makeROIPropID(ROIProperty.XPV_VALUE.propIDPre, index), null));
            break;
        case YPV:
            addPVProperty(new PVNameProperty(propID, roiProperty.description, category, ""),
                    new PVValueProperty(makeROIPropID(ROIProperty.YPV_VALUE.propIDPre, index), null));
            break;
        case WPV:
            addPVProperty(new PVNameProperty(propID, roiProperty.description, category, ""),
                    new PVValueProperty(makeROIPropID(ROIProperty.WPV_VALUE.propIDPre, index), null));
            break;
        case HPV:
            addPVProperty(new PVNameProperty(propID, roiProperty.description, category, ""),
                    new PVValueProperty(makeROIPropID(ROIProperty.HPV_VALUE.propIDPre, index), null));
            break;
        default:
            break;
        }
    }

    public static String makeAxisPropID(String axisID, String propIDPre) {
        return axisID + "_" + propIDPre;
    }

    private void addAxisProperties() {
        WidgetPropertyCategory xCategory = new NameDefinedCategory("X Axis");
        WidgetPropertyCategory yCategory = new NameDefinedCategory("Y Axis");
        for (AxisProperty axisProperty : AxisProperty.values()) {
            addAxisProperty(X_AXIS_ID, axisProperty, xCategory);
        }

        for (AxisProperty axisProperty : AxisProperty.values()) {
            addAxisProperty(Y_AXIS_ID, axisProperty, yCategory);
        }
    }

    private void addAxisProperty(String axisID, AxisProperty axisProperty, WidgetPropertyCategory category) {
        var propID = makeAxisPropID(axisID, axisProperty.propIDPre);

        switch (axisProperty) {
        case TITLE:
            addProperty(new StringProperty(propID, axisProperty.toString(), category, category.toString()));
            break;
        case SCALE_FONT:
            addProperty(new FontProperty(propID, axisProperty.toString(), category, MediaService.DEFAULT_FONT));
            break;
        case TITLE_FONT:
            addProperty(new FontProperty(propID, axisProperty.toString(), category, MediaService.DEFAULT_BOLD_FONT));
            break;
        case AXIS_COLOR:
            addProperty(new ColorProperty(propID, axisProperty.toString(), category, DEFAULT_AXIS_COLOR));
            break;
        case VISIBLE:
        case SHOW_MINOR_TICKS:
            addProperty(new BooleanProperty(propID, axisProperty.toString(), category, true));
            break;
        case MAX:
            addProperty(new DoubleProperty(propID, axisProperty.toString(), category, 100));
            break;
        case MIN:
            addProperty(new DoubleProperty(propID, axisProperty.toString(), category, 0));
            break;
        case MAJOR_TICK_STEP_HINT:
            addProperty(new IntegerProperty(propID, axisProperty.toString(), category, 50, 1, 1000));
            break;
        default:
            break;
        }
    }

    @Override
    public String getTypeID() {
        return ID;
    }

    /**
     * @return the maximum value
     */
    public Double getMaximum() {
        return (Double) getCastedPropertyValue(PROP_MAX);
    }

    /**
     * @return the minimum value
     */
    public Double getMinimum() {
        return (Double) getCastedPropertyValue(PROP_MIN);
    }

    /**
     * @return the data width
     */
    public Integer getDataWidth() {
        return (Integer) getCastedPropertyValue(PROP_DATA_WIDTH);
    }

    /**
     * @return the data height
     */
    public Integer getDataHeight() {
        return (Integer) getCastedPropertyValue(PROP_DATA_HEIGHT);
    }

    /**
     * @return the graph area width
     */
    public int getGraphAreaWidth() {
        return (Integer) getCastedPropertyValue(PROP_GRAPH_AREA_WIDTH);
    }

    /**
     * @return the graph area height
     */
    public int getGraphAreaHeight() {
        return (Integer) getCastedPropertyValue(PROP_GRAPH_AREA_HEIGHT);
    }

    /**
     * @return the color map
     */
    public ColorMap getColorMap() {
        return (ColorMap) getCastedPropertyValue(PROP_COLOR_MAP);
    }

    /**
     * @return the color map
     */
    public Boolean isShowRamp() {
        return (Boolean) getCastedPropertyValue(PROP_SHOW_RAMP);
    }

    /**
     * @return the left crop part
     */
    public int getCropLeft() {
        return (Integer) getPropertyValue(PROP_CROP_LEFT);
    }

    /**
     * @return the right crop part
     */
    public int getCropRight() {
        return (Integer) getPropertyValue(PROP_CROP_RIGHT);
    }

    /**
     * @return the top crop part
     */
    public int getCropTOP() {
        return (Integer) getPropertyValue(PROP_CROP_TOP);
    }

    /**
     * @return the bottom crop part
     */
    public int getCropBottom() {
        return (Integer) getPropertyValue(PROP_CROP_BOTTOM);
    }

    public String getHorizonProfileXPV() {
        return (String) getPropertyValue(PROP_HORIZON_PROFILE_X_PV_NAME);
    }

    public String getVerticalProfileXPV() {
        return (String) getPropertyValue(PROP_VERTICAL_PROFILE_X_PV_NAME);
    }

    public String getHorizonProfileYPV() {
        return (String) getPropertyValue(PROP_HORIZON_PROFILE_Y_PV_NAME);
    }

    public String getVerticalProfileYPV() {
        return (String) getPropertyValue(PROP_VERTICAL_PROFILE_Y_PV_NAME);
    }

    public String getPixelInfoPV() {
        return (String) getPropertyValue(PROP_PIXEL_INFO_PV_NAME);
    }

    public boolean isRGBMode() {
        return (Boolean) getPropertyValue(PROP_RGB_MODE);
    }

    public ColorDepth getColorDepth() {
        return ColorDepth.values()[(Integer) getPropertyValue(PROP_COLOR_DEPTH)];
    }

    public OPIColor getROIColor() {
        return (OPIColor) getPropertyValue(PROP_ROI_COLOR);
    }

    public boolean isSingleLineProfiling() {
        return (Boolean) getPropertyValue(PROP_SINGLE_LINE_PROFILING);
    }

}
