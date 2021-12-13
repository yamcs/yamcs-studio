/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_STYLE;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_BORDER_WIDTH;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_HEIGHT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_WIDTH;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_COLOR_DEPTH;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_COLOR_MAP;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_CROP_BOTTOM;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_CROP_LEFT;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_CROP_RIGHT;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_CROP_TOP;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_DATA_HEIGHT;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_DATA_WIDTH;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_GRAPH_AREA_HEIGHT;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_GRAPH_AREA_WIDTH;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_HORIZON_PROFILE_X_PV_NAME;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_HORIZON_PROFILE_Y_PV_NAME;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_MAX;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_MIN;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_PIXEL_INFO_PV_NAME;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_RGB_MODE;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_ROI_COLOR;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_ROI_COUNT;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_SHOW_RAMP;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_SINGLE_LINE_PROFILING;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_VERTICAL_PROFILE_X_PV_NAME;
import static org.csstudio.opibuilder.widgets.model.IntensityGraphModel.PROP_VERTICAL_PROFILE_Y_PV_NAME;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.visualparts.BorderFactory;
import org.csstudio.opibuilder.visualparts.BorderStyle;
import org.csstudio.opibuilder.widgets.model.IntensityGraphModel;
import org.csstudio.opibuilder.widgets.model.IntensityGraphModel.AxisProperty;
import org.csstudio.opibuilder.widgets.model.IntensityGraphModel.ROIProperty;
import org.csstudio.opibuilder.widgets.util.ListNumberWrapper;
import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.figures.IntensityGraphFigure;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.draw2d.IFigure;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.ArrayDouble;
import org.yamcs.studio.data.vtype.ArrayInt;
import org.yamcs.studio.data.vtype.ListNumber;
import org.yamcs.studio.data.vtype.VNumberArray;
import org.yamcs.studio.data.vtype.VType;
import org.yamcs.studio.data.vtype.ValueFactory;

public class IntensityGraphEditPart extends AbstractPVWidgetEditPart {

    private boolean innerTrig;

    private IntensityGraphFigure graph;

    /** VTable columns used to publish values for the pixel info PV */
    private static final List<String> PXL_TABLE_COLUMNS = Arrays.asList("X", "Y", "Value", "Selected");

    /** VTable column types used to publish values for the pixel info PV */
    private static final List<Class<?>> PXL_TABLE_TYPES = Arrays.<Class<?>> asList(double.class, double.class,
            double.class, int.class);

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();
        graph = new IntensityGraphFigure(getExecutionMode() == ExecutionMode.RUN_MODE);
        graph.setMin(model.getMinimum());
        graph.setMax(model.getMaximum());
        graph.setDataWidth(model.getDataWidth());
        graph.setDataHeight(model.getDataHeight());
        graph.setColorMap(model.getColorMap());
        graph.setShowRamp(model.isShowRamp());
        graph.setCropLeft(model.getCropLeft());
        graph.setCropRight(model.getCropRight());
        graph.setCropTop(model.getCropTOP());
        graph.setCropBottom(model.getCropBottom());
        graph.setInRGBMode(model.isRGBMode());
        graph.setColorDepth(model.getColorDepth());
        graph.setSingleLineProfiling(model.isSingleLineProfiling());
        graph.setROIColor(model.getROIColor().getSWTColor());
        // init X-Axis
        for (var axisProperty : AxisProperty.values()) {
            var propID = IntensityGraphModel.makeAxisPropID(IntensityGraphModel.X_AXIS_ID, axisProperty.propIDPre);
            setAxisProperty(graph.getXAxis(), axisProperty, model.getPropertyValue(propID));
        }
        // init Y-Axis
        for (var axisProperty : AxisProperty.values()) {
            var propID = IntensityGraphModel.makeAxisPropID(IntensityGraphModel.Y_AXIS_ID, axisProperty.propIDPre);
            setAxisProperty(graph.getYAxis(), axisProperty, model.getPropertyValue(propID));
        }
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            // add profile data listener
            if (model.getHorizonProfileYPV().trim().length() > 0 || model.getVerticalProfileYPV().trim().length() > 0) {
                graph.addProfileDataListener((xProfileData, yProfileData, xAxisRange, yAxisRange) -> {
                    // horizontal
                    setPVValue(PROP_HORIZON_PROFILE_Y_PV_NAME, xProfileData);
                    var horizonXData = new double[xProfileData.length];
                    var d = (xAxisRange.getUpper() - xAxisRange.getLower()) / (xProfileData.length - 1);
                    for (var i1 = 0; i1 < xProfileData.length; i1++) {
                        horizonXData[i1] = xAxisRange.getLower() + d * i1;
                    }
                    setPVValue(PROP_HORIZON_PROFILE_X_PV_NAME, horizonXData);
                    // vertical
                    setPVValue(PROP_VERTICAL_PROFILE_Y_PV_NAME, yProfileData);
                    var verticalXData = new double[yProfileData.length];
                    d = (yAxisRange.getUpper() - yAxisRange.getLower()) / (yProfileData.length - 1);
                    for (var i2 = 0; i2 < yProfileData.length; i2++) {
                        verticalXData[i2] = yAxisRange.getUpper() - d * i2;
                    }
                    setPVValue(PROP_VERTICAL_PROFILE_X_PV_NAME, verticalXData);
                });
            }

            if (model.getPixelInfoPV().trim().length() > 0) { // Listen to pixel info, forward to PV
                graph.addPixelInfoListener((pixel_info, selected) -> {
                    // TODO "Selected" column should be boolean, but there is no 'ArrayBoolean', and List<Boolean>
                    // also fails
                    List<Object> values = Arrays.<Object> asList(new ArrayDouble(pixel_info.xcoord),
                            new ArrayDouble(pixel_info.ycoord), new ArrayDouble(pixel_info.value),
                            new ArrayInt(selected ? 1 : 0));
                    Object value = ValueFactory.newVTable(PXL_TABLE_TYPES, PXL_TABLE_COLUMNS, values);
                    setPVValue(PROP_PIXEL_INFO_PV_NAME, value);
                });
            }
        }

        updatePropSheet();

        return graph;
    }

    @Override
    public IntensityGraphModel getWidgetModel() {
        return (IntensityGraphModel) getModel();
    }

    private void updatePropSheet() {
        var rgbMode = getWidgetModel().isRGBMode();
        getWidgetModel().setPropertyVisible(PROP_COLOR_DEPTH, rgbMode);
        getWidgetModel().setPropertyVisible(PROP_COLOR_MAP, !rgbMode);
        getWidgetModel().setPropertyVisible(PROP_SHOW_RAMP, !rgbMode);
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        innerUpdateGraphAreaSizeProperty();
        registerAxisPropertyChangeHandler();
        registerROIPropertyChangeHandlers();
        registerROIAmountChangeHandler();

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, figure) -> {
            if (newValue == null) {
                return false;
            }
            var value = (VType) newValue;

            if (value instanceof VNumberArray) {
                setValue(((VNumberArray) value).getData());
                return false;
            }

            ((IntensityGraphFigure) figure).setDataArray(VTypeHelper.getDoubleArray(value));

            return false;
        });

        getWidgetModel().getProperty(PROP_MIN).addPropertyChangeListener(evt -> {
            ((IntensityGraphFigure) figure).setMin((Double) evt.getNewValue());
            figure.repaint();
            innerUpdateGraphAreaSizeProperty();
        });

        getWidgetModel().getProperty(PROP_MAX).addPropertyChangeListener(evt -> {
            ((IntensityGraphFigure) figure).setMax((Double) evt.getNewValue());
            figure.repaint();
            innerUpdateGraphAreaSizeProperty();
        });

        getWidgetModel().getProperty(PROP_BORDER_STYLE).removeAllPropertyChangeListeners();
        getWidgetModel().getProperty(PROP_BORDER_STYLE).addPropertyChangeListener(evt -> {
            figure.setBorder(BorderFactory.createBorder(BorderStyle.values()[(Integer) evt.getNewValue()],
                    getWidgetModel().getBorderWidth(), getWidgetModel().getBorderColor(), getWidgetModel().getName()));
            innerUpdateGraphAreaSizeProperty();
        });

        getWidgetModel().getProperty(PROP_BORDER_WIDTH).removeAllPropertyChangeListeners();
        getWidgetModel().getProperty(PROP_BORDER_WIDTH).addPropertyChangeListener(evt -> {
            figure.setBorder(BorderFactory.createBorder(getWidgetModel().getBorderStyle(), (Integer) evt.getNewValue(),
                    getWidgetModel().getBorderColor(), getWidgetModel().getName()));
            innerUpdateGraphAreaSizeProperty();
        });

        setPropertyChangeHandler(PROP_DATA_WIDTH, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) figure).setDataWidth((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_DATA_HEIGHT, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) figure).setDataHeight((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_COLOR_MAP, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) figure).setColorMap((ColorMap) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_CROP_LEFT, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) figure).setCropLeft((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_CROP_RIGHT, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) figure).setCropRight((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_CROP_TOP, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) figure).setCropTop((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_CROP_BOTTOM, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) figure).setCropBottom((Integer) newValue);
            return true;
        });

        getWidgetModel().getProperty(PROP_SHOW_RAMP).addPropertyChangeListener(evt -> {
            ((IntensityGraphFigure) getFigure()).setShowRamp((Boolean) evt.getNewValue());
            var d = ((IntensityGraphFigure) getFigure()).getGraphAreaInsets();
            innerTrig = true;
            getWidgetModel().setPropertyValue(PROP_GRAPH_AREA_WIDTH,
                    getWidgetModel().getWidth() - d.width);
            innerTrig = false;
        });

        getWidgetModel().getProperty(PROP_WIDTH).addPropertyChangeListener(evt -> {
            if (!innerTrig) { // if it is not triggered from inner
                innerTrig = true;
                var d = ((IntensityGraphFigure) getFigure()).getGraphAreaInsets();
                getWidgetModel().setPropertyValue(PROP_GRAPH_AREA_WIDTH, ((Integer) evt.getNewValue()) - d.width);
                innerTrig = false; // reset innerTrig to false after each inner triggering
            } else {
                innerTrig = false;
            }
        });

        getWidgetModel().getProperty(PROP_GRAPH_AREA_WIDTH).addPropertyChangeListener(evt -> {
            if (!innerTrig) {
                innerTrig = true;
                var d = ((IntensityGraphFigure) getFigure()).getGraphAreaInsets();
                getWidgetModel().setPropertyValue(PROP_WIDTH, ((Integer) evt.getNewValue()) + d.width);
                innerTrig = false; // reset innerTrig to false after each inner triggering
            } else {
                innerTrig = false;
            }
        });

        getWidgetModel().getProperty(PROP_HEIGHT).addPropertyChangeListener(evt -> {
            if (!innerTrig) {
                innerTrig = true;
                var d = ((IntensityGraphFigure) getFigure()).getGraphAreaInsets();
                getWidgetModel().setPropertyValue(PROP_GRAPH_AREA_HEIGHT,
                        ((Integer) evt.getNewValue()) - d.height);
                innerTrig = false; // reset innerTrig to false after each inner triggering
            } else {
                innerTrig = false;
            }
        });

        getWidgetModel().getProperty(PROP_GRAPH_AREA_HEIGHT).addPropertyChangeListener(evt -> {
            if (!innerTrig) {
                innerTrig = true;
                var d = ((IntensityGraphFigure) getFigure()).getGraphAreaInsets();
                getWidgetModel().setPropertyValue(PROP_HEIGHT,
                        ((Integer) evt.getNewValue()) + d.height);
                innerTrig = false; // reset innerTrig to false after each inner triggering
            } else {
                innerTrig = false;
            }
        });

        getWidgetModel().getProperty(PROP_RGB_MODE).addPropertyChangeListener(evt -> {
            updatePropSheet();
            ((IntensityGraphFigure) getFigure()).setInRGBMode((Boolean) (evt.getNewValue()));
        });

        setPropertyChangeHandler(PROP_COLOR_DEPTH, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) getFigure()).setColorDepth(getWidgetModel().getColorDepth());
            return false;
        });

        setPropertyChangeHandler(PROP_SINGLE_LINE_PROFILING, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) getFigure()).setSingleLineProfiling((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_ROI_COLOR, (oldValue, newValue, figure) -> {
            ((IntensityGraphFigure) getFigure()).setROIColor(((OPIColor) newValue).getSWTColor());
            return false;
        });
    }

    private synchronized void innerUpdateGraphAreaSizeProperty() {
        var d = ((IntensityGraphFigure) figure).getGraphAreaInsets();
        innerTrig = true;
        getWidgetModel().setPropertyValue(PROP_GRAPH_AREA_WIDTH, getWidgetModel().getSize().width - d.width);
        innerTrig = true; // recover innerTrig
        getWidgetModel().setPropertyValue(PROP_GRAPH_AREA_HEIGHT, getWidgetModel().getSize().height - d.height);
        innerTrig = false; // reset innerTrig to false after each inner triggering
    }

    private void registerROIAmountChangeHandler() {
        PropertyChangeListener listener = evt -> {
            int currentCount = (Integer) evt.getOldValue();
            int newCount = (Integer) evt.getNewValue();
            if (newCount > currentCount) {
                for (var i1 = currentCount; i1 < newCount; i1++) {
                    for (var roiProperty1 : ROIProperty.values()) {
                        if (roiProperty1 != ROIProperty.XPV_VALUE && roiProperty1 != ROIProperty.YPV_VALUE
                                && roiProperty1 != ROIProperty.WPV_VALUE && roiProperty1 != ROIProperty.HPV_VALUE) {
                            var propID1 = IntensityGraphModel.makeROIPropID(roiProperty1.propIDPre, i1);
                            getWidgetModel().setPropertyVisible(propID1, true);
                        }
                    }
                    var roiIndex = i1;
                    graph.addROI(getROIName(roiIndex), (xIndex, yIndex, width, height) -> {
                        var propID = IntensityGraphModel.makeROIPropID(ROIProperty.XPV.propIDPre, roiIndex);
                        setPVValue(propID, xIndex);
                        propID = IntensityGraphModel.makeROIPropID(ROIProperty.YPV.propIDPre, roiIndex);
                        setPVValue(propID, yIndex);
                        propID = IntensityGraphModel.makeROIPropID(ROIProperty.WPV.propIDPre, roiIndex);
                        setPVValue(propID, width);
                        propID = IntensityGraphModel.makeROIPropID(ROIProperty.HPV.propIDPre, roiIndex);
                        setPVValue(propID, height);
                    }, (xIndex, yIndex, width, height) -> {
                        var propID = IntensityGraphModel.makeROIPropID(ROIProperty.TITLE.propIDPre, roiIndex);
                        return (String) getPropertyValue(propID);
                    });
                }
            } else if (newCount < currentCount) {
                for (var i2 = currentCount - 1; i2 >= newCount; i2--) {
                    graph.removeROI(getROIName(i2));
                    for (var roiProperty2 : ROIProperty.values()) {
                        var propID2 = IntensityGraphModel.makeROIPropID(roiProperty2.propIDPre, i2);
                        getWidgetModel().setPropertyVisible(propID2, false);
                    }
                }
            }
        };
        var countProperty = getWidgetModel().getProperty(PROP_ROI_COUNT);
        countProperty.addPropertyChangeListener(listener);

        // init
        int currentCount = (Integer) getPropertyValue(PROP_ROI_COUNT);
        listener.propertyChange(
                new PropertyChangeEvent(countProperty, PROP_ROI_COUNT, 0, currentCount));
        for (var i = 0; i < currentCount; i++) {
            for (var roiProperty : ROIProperty.values()) {
                var propID = IntensityGraphModel.makeROIPropID(roiProperty.propIDPre, i);
                var propertyValue = getPropertyValue(propID);
                if (propertyValue != null) {
                    setROIProperty(getROIName(i), roiProperty, propertyValue);
                }
            }
        }
    }

    private static String getROIName(int index) {
        return "ROI_" + index;
    }

    private void registerROIPropertyChangeHandlers() {
        for (var i = 0; i < IntensityGraphModel.MAX_ROIS_AMOUNT; i++) {
            var roiName = getROIName(i);
            for (var roiProperty : ROIProperty.values()) {
                var propID = IntensityGraphModel.makeROIPropID(roiProperty.propIDPre, i);
                if (i >= (Integer) getPropertyValue(PROP_ROI_COUNT)) {
                    getWidgetModel().setPropertyVisible(propID, false);
                }
                setPropertyChangeHandler(propID, (oldValue, newValue, figure) -> {
                    setROIProperty(roiName, roiProperty, newValue);
                    return false;
                });
            }
        }
    }

    private void setROIProperty(String roiName, ROIProperty roiProperty, Object newValue) {
        switch (roiProperty) {
        case TITLE:
            break;
        case VISIBLE:
            graph.setROIVisible(roiName, (Boolean) newValue);
            break;
        case XPV_VALUE:
            graph.getROI(roiName).setROIDataBoundsX((int) VTypeHelper.getDouble((VType) newValue));
            break;
        case YPV_VALUE:
            graph.getROI(roiName).setROIDataBoundsY((int) VTypeHelper.getDouble((VType) newValue));
            break;
        case WPV_VALUE:
            graph.getROI(roiName).setROIDataBoundsW((int) VTypeHelper.getDouble((VType) newValue));
            break;
        case HPV_VALUE:
            graph.getROI(roiName).setROIDataBoundsH((int) VTypeHelper.getDouble((VType) newValue));
            break;
        default:
            break;
        }
    }

    private void registerAxisPropertyChangeHandler() {
        for (var axisID : new String[] { IntensityGraphModel.X_AXIS_ID, IntensityGraphModel.Y_AXIS_ID }) {
            for (var axisProperty : AxisProperty.values()) {
                IWidgetPropertyChangeHandler handler = new AxisPropertyChangeHandler(
                        axisID.equals(IntensityGraphModel.X_AXIS_ID) ? ((IntensityGraphFigure) getFigure()).getXAxis()
                                : ((IntensityGraphFigure) getFigure()).getYAxis(),
                        axisProperty);
                // must use listener instead of handler because the prop sheet need to be
                // refreshed immediately.
                getWidgetModel().getProperty(IntensityGraphModel.makeAxisPropID(axisID, axisProperty.propIDPre))
                        .addPropertyChangeListener(evt -> {
                            handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
                            UIBundlingThread.getInstance().addRunnable(getViewer().getControl().getDisplay(),
                                    () -> getFigure().repaint());
                        });

            }
        }
    }

    private void setAxisProperty(Axis axis, AxisProperty axisProperty, Object newValue) {
        switch (axisProperty) {
        case TITLE:
            axis.setTitle((String) newValue);
            break;
        case AXIS_COLOR:
            axis.setForegroundColor(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            break;
        case MAX:
            var axisID = (axis == graph.getXAxis()) ? IntensityGraphModel.X_AXIS_ID : IntensityGraphModel.Y_AXIS_ID;
            double lower = (Double) getPropertyValue(
                    IntensityGraphModel.makeAxisPropID(axisID, AxisProperty.MIN.propIDPre));
            axis.setRange(lower, (Double) newValue);
            break;
        case MIN:
            var axisID2 = (axis == graph.getXAxis()) ? IntensityGraphModel.X_AXIS_ID : IntensityGraphModel.Y_AXIS_ID;
            double upper = (Double) getPropertyValue(
                    IntensityGraphModel.makeAxisPropID(axisID2, AxisProperty.MAX.propIDPre));
            axis.setRange((Double) newValue, upper);
            break;
        case SCALE_FONT:
            axis.setFont(((OPIFont) newValue).getSWTFont());
            break;
        case TITLE_FONT:
            axis.setTitleFont(((OPIFont) newValue).getSWTFont());
            break;
        case MAJOR_TICK_STEP_HINT:
            axis.setMajorTickMarkStepHint((Integer) newValue);
            break;
        case SHOW_MINOR_TICKS:
            axis.setMinorTicksVisible((Boolean) newValue);
            break;
        case VISIBLE:
            axis.setVisible((Boolean) newValue);
            break;
        default:
            break;
        }
    }

    public void setColorMap(String mapName) {
        for (var map : ColorMap.PredefinedColorMap.values()) {
            if (map.toString().equals(mapName)) {
                setPropertyValue(PROP_COLOR_MAP, new ColorMap(map, true, true));
                break;
            }
        }
    }

    @Override
    public double[] getValue() {
        return ((IntensityGraphFigure) getFigure()).getDataArray();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof double[]) {
            ((IntensityGraphFigure) getFigure()).setDataArray((double[]) value);
        } else if (value instanceof ListNumber) {
            ((IntensityGraphFigure) getFigure()).setDataArray(new ListNumberWrapper((ListNumber) value));
        } else if (value instanceof short[]) {
            ((IntensityGraphFigure) getFigure()).setDataArray((short[]) value);
        } else if (value instanceof byte[]) {
            ((IntensityGraphFigure) getFigure()).setDataArray((byte[]) value);
        } else if (value instanceof float[]) {
            ((IntensityGraphFigure) getFigure()).setDataArray((float[]) value);
        } else if (value instanceof long[]) {
            ((IntensityGraphFigure) getFigure()).setDataArray((long[]) value);
        } else if (value instanceof int[]) {
            ((IntensityGraphFigure) getFigure()).setDataArray((int[]) value);
        } else {
            super.setValue(value);
        }
    }

    @Override
    public void deactivate() {
        ((IntensityGraphFigure) getFigure()).dispose();
        super.deactivate();
    }

    class AxisPropertyChangeHandler implements IWidgetPropertyChangeHandler {
        private AxisProperty axisProperty;
        private Axis axis;

        public AxisPropertyChangeHandler(Axis axis, AxisProperty axisProperty) {
            this.axis = axis;
            this.axisProperty = axisProperty;
        }

        @Override
        public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
            setAxisProperty(axis, axisProperty, newValue);
            innerTrig = true;
            innerUpdateGraphAreaSizeProperty();
            axis.revalidate();
            return true;
        }
    }
}
