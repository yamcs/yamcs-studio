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

import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_AXIS_COUNT;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_PLOTAREA_BACKCOLOR;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_SHOW_LEGEND;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_SHOW_PLOTAREA_BORDER;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_SHOW_TOOLBAR;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_TITLE;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_TITLE_FONT;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_TRACE_COUNT;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.XYGraphModel.PROP_TRIGGER_PV_VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.dnd.DropPVtoPVWidgetEditPolicy;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.widgets.model.XYGraphModel;
import org.csstudio.opibuilder.widgets.model.XYGraphModel.AxisProperty;
import org.csstudio.opibuilder.widgets.model.XYGraphModel.TraceProperty;
import org.csstudio.ui.util.CustomMediaFactory;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.draw2d.IFigure;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider.PlotMode;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider.UpdateMode;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VType;

public class XYGraphEditPart extends AbstractPVWidgetEditPart {

    private List<Axis> axisList;
    private List<Trace> traceList;

    @Override
    public XYGraphModel getWidgetModel() {
        return (XYGraphModel) getModel();
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        removeEditPolicy(DropPVtoPVWidgetEditPolicy.DROP_PV_ROLE);
        installEditPolicy(DropPVtoPVWidgetEditPolicy.DROP_PV_ROLE, new DropPVtoXYGraphEditPolicy());
    }

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();
        var xyGraphFigure = new ToolbarArmedXYGraph();
        var xyGraph = xyGraphFigure.getXYGraph();
        xyGraph.setTitle(model.getTitle());
        xyGraph.setTitleFont(CustomMediaFactory.getInstance().getFont(model.getTitleFont().getFontData()));
        xyGraph.getPlotArea().setShowBorder(model.isShowPlotAreaBorder());
        xyGraph.getPlotArea()
                .setBackgroundColor(CustomMediaFactory.getInstance().getColor(model.getPlotAreaBackColor()));
        xyGraph.setShowLegend(model.isShowLegend());
        xyGraphFigure.setShowToolbar(model.isShowToolbar());
        xyGraphFigure.setTransparent(model.isTransprent());
        axisList = new ArrayList<>();
        axisList.add(xyGraph.getPrimaryXAxis());
        axisList.add(xyGraph.getPrimaryYAxis());
        traceList = new ArrayList<>();
        // init all axes
        for (var i = 0; i < XYGraphModel.MAX_AXES_AMOUNT; i++) {
            if (i >= 2) {
                axisList.add(new Axis("", true));
                if (i < model.getAxesAmount()) {
                    xyGraphFigure.getXYGraph().addAxis(axisList.get(i));
                }
            }
            for (var axisProperty : AxisProperty.values()) {
                // there is no primary and y-axis property for primary axes.
                if (i < 2 && (axisProperty == AxisProperty.PRIMARY || axisProperty == AxisProperty.Y_AXIS)) {
                    continue;
                }
                var propID = XYGraphModel.makeAxisPropID(axisProperty.propIDPre, i);
                setAxisProperty(axisList.get(i), axisProperty, model.getProperty(propID).getPropertyValue());
            }
        }

        // init all traces
        for (var i = 0; i < XYGraphModel.MAX_TRACES_AMOUNT; i++) {
            traceList.add(new Trace("", xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis(),
                    new CircularBufferDataProvider(false)));
            if (i < model.getTracesAmount()) {
                xyGraph.addTrace(traceList.get(i));
            }
            var xPVPropID = XYGraphModel.makeTracePropID(TraceProperty.XPV.propIDPre, i);
            var yPVPropID = XYGraphModel.makeTracePropID(TraceProperty.YPV.propIDPre, i);
            for (var traceProperty : TraceProperty.values()) {
                var propID = XYGraphModel.makeTracePropID(traceProperty.propIDPre, i);
                setTraceProperty(traceList.get(i), traceProperty, model.getProperty(propID).getPropertyValue(),
                        xPVPropID, yPVPropID);
            }
        }

        return xyGraphFigure;
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        registerAxisPropertyChangeHandlers();
        registerTracePropertyChangeHandlers();

        setPropertyChangeHandler(PROP_TITLE, (oldValue, newValue, refreshableFigure) -> {
            var graph = (ToolbarArmedXYGraph) refreshableFigure;
            graph.getXYGraph().setTitle((String) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_TITLE_FONT, (oldValue, newValue, refreshableFigure) -> {
            var graph = (ToolbarArmedXYGraph) refreshableFigure;
            graph.getXYGraph()
                    .setTitleFont(CustomMediaFactory.getInstance().getFont(((OPIFont) newValue).getFontData()));
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_PLOTAREA_BORDER, (oldValue, newValue, refreshableFigure) -> {
            var graph = (ToolbarArmedXYGraph) refreshableFigure;
            graph.getXYGraph().getPlotArea().setShowBorder((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_PLOTAREA_BACKCOLOR, (oldValue, newValue, refreshableFigure) -> {
            var graph = (ToolbarArmedXYGraph) refreshableFigure;
            graph.getXYGraph().getPlotArea()
                    .setBackgroundColor(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            return true;
        });

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, refreshableFigure) -> {
            var graph = (ToolbarArmedXYGraph) refreshableFigure;
            graph.setTransparent((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_LEGEND, (oldValue, newValue, refreshableFigure) -> {
            var graph = (ToolbarArmedXYGraph) refreshableFigure;
            graph.getXYGraph().setShowLegend((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_TOOLBAR, (oldValue, newValue, refreshableFigure) -> {
            var graph = (ToolbarArmedXYGraph) refreshableFigure;
            graph.setShowToolbar((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_TRIGGER_PV_VALUE, (oldValue, newValue, figure) -> {
            for (var i = 0; i < getWidgetModel().getTracesAmount(); i++) {
                var dataProvider = (CircularBufferDataProvider) traceList.get(i).getDataProvider();
                if (dataProvider.getUpdateMode() == UpdateMode.TRIGGER) {
                    dataProvider.triggerUpdate();
                }
            }
            return false;
        });

        registerAxesAmountChangeHandler();
        registerTraceAmountChangeHandler();
    }

    private void registerAxesAmountChangeHandler() {
        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            var model = (XYGraphModel) getModel();
            var xyGraph = ((ToolbarArmedXYGraph) refreshableFigure).getXYGraph();
            var currentAxisAmount = xyGraph.getAxisList().size();
            // add axis
            if ((Integer) newValue > currentAxisAmount) {
                for (var i1 = 0; i1 < (Integer) newValue - currentAxisAmount; i1++) {
                    for (var axisProperty1 : AxisProperty.values()) {
                        var propID1 = XYGraphModel.makeAxisPropID(axisProperty1.propIDPre, i1 + currentAxisAmount);
                        model.setPropertyVisible(propID1, true);
                    }
                    xyGraph.addAxis(axisList.get(i1 + currentAxisAmount));
                }
            } else if ((Integer) newValue < currentAxisAmount) { // remove axis
                for (var i2 = 0; i2 < currentAxisAmount - (Integer) newValue; i2++) {
                    for (var axisProperty2 : AxisProperty.values()) {
                        var propID2 = XYGraphModel.makeAxisPropID(axisProperty2.propIDPre, i2 + (Integer) newValue);
                        model.setPropertyVisible(propID2, false);
                    }
                    xyGraph.removeAxis(axisList.get(i2 + (Integer) newValue));
                }
            }
            return true;
        };
        getWidgetModel().getProperty(PROP_AXIS_COUNT).addPropertyChangeListener(
                evt -> handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));
        // setPropertyChangeHandler(XYGraphModel.PROP_AXES_AMOUNT, handler);
    }

    private void registerAxisPropertyChangeHandlers() {
        var model = (XYGraphModel) getModel();
        // set prop handlers and init all the potential axes
        for (var i = 0; i < XYGraphModel.MAX_AXES_AMOUNT; i++) {
            for (var axisProperty : AxisProperty.values()) {
                // there is no primary and y-axis property for primary axes.
                if (i < 2 && (axisProperty == AxisProperty.PRIMARY || axisProperty == AxisProperty.Y_AXIS)) {
                    continue;
                }
                var propID = XYGraphModel.makeAxisPropID(axisProperty.propIDPre, i);
                IWidgetPropertyChangeHandler handler = new AxisPropertyChangeHandler(i, axisProperty);
                setPropertyChangeHandler(propID, handler);
            }
        }

        for (var i = XYGraphModel.MAX_AXES_AMOUNT - 1; i >= model.getAxesAmount(); i--) {
            for (var axisProperty : AxisProperty.values()) {
                var propID = XYGraphModel.makeAxisPropID(axisProperty.propIDPre, i);
                model.setPropertyVisible(propID, false);
            }
        }
    }

    private void setAxisProperty(Axis axis, AxisProperty axisProperty, Object newValue) {
        switch (axisProperty) {
        case AUTO_SCALE:
            axis.setAutoScale((Boolean) newValue);
            break;
        case VISIBLE:
            axis.setVisible((Boolean) newValue);
            break;
        case TITLE:
            axis.setTitle((String) newValue);
            break;
        case AUTO_SCALE_THRESHOLD:
            axis.setAutoScaleThreshold((Double) newValue);
            break;
        case AXIS_COLOR:
            axis.setForegroundColor(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            break;
        case DASH_GRID:
            axis.setDashGridLine((Boolean) newValue);
            break;
        case GRID_COLOR:
            axis.setMajorGridColor(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            break;
        case LOG:
            axis.setLogScale((Boolean) newValue);
            break;
        case MAX:
            double lower = (Double) getPropertyValue(
                    XYGraphModel.makeAxisPropID(AxisProperty.MIN.propIDPre, axisList.indexOf(axis)));
            axis.setRange(lower, (Double) newValue);
            break;
        case MIN:
            double upper = (Double) getPropertyValue(
                    XYGraphModel.makeAxisPropID(AxisProperty.MAX.propIDPre, axisList.indexOf(axis)));
            axis.setRange((Double) newValue, upper);
            break;
        case PRIMARY:
            axis.setPrimarySide((Boolean) newValue);
            break;
        case SHOW_GRID:
            axis.setShowMajorGrid((Boolean) newValue);
            break;
        case TIME_FORMAT:
            if ((Integer) newValue == 0) {
                axis.setDateEnabled(false);
                axis.setAutoFormat(true);
                break;
            } else if ((Integer) newValue == 8) {
                axis.setDateEnabled(true);
                axis.setAutoFormat(true);
            } else {
                var format = XYGraphModel.TIME_FORMAT_ARRAY[(Integer) newValue];
                axis.setDateEnabled(true);
                axis.setFormatPattern(format);
            }
            break;
        case SCALE_FONT:
            axis.setFont(((OPIFont) newValue).getSWTFont());
            break;
        case TITLE_FONT:
            axis.setTitleFont(((OPIFont) newValue).getSWTFont());
            break;
        case Y_AXIS:
            axis.setYAxis((Boolean) newValue);
            break;
        case SCALE_FORMAT:
            if (((String) newValue).trim().equals("")) {
                if (!axis.isDateEnabled()) {
                    axis.setAutoFormat(true);
                }
            } else {
                axis.setAutoFormat(false);
                try {
                    axis.setFormatPattern((String) newValue);
                } catch (Exception e) {
                    OPIBuilderPlugin.getLogger().log(Level.SEVERE, (String) newValue + " is not a valid numeric format."
                            + " The axis will be auto formatted.");
                    axis.setAutoFormat(true);
                }
            }
            break;
        default:
            break;
        }
    }

    private void registerTraceAmountChangeHandler() {
        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            var model = (XYGraphModel) getModel();
            var xyGraph = ((ToolbarArmedXYGraph) refreshableFigure).getXYGraph();
            var currentTracesAmount = xyGraph.getPlotArea().getTraceList().size();
            // add trace
            if ((Integer) newValue > currentTracesAmount) {
                for (var i1 = 0; i1 < (Integer) newValue - currentTracesAmount; i1++) {
                    for (var traceProperty1 : TraceProperty.values()) {
                        if (traceProperty1 == TraceProperty.XPV_VALUE || traceProperty1 == TraceProperty.YPV_VALUE) {
                            continue;
                        }
                        var propID1 = XYGraphModel.makeTracePropID(traceProperty1.propIDPre, i1 + currentTracesAmount);
                        model.setPropertyVisible(propID1, true);
                    }
                    xyGraph.addTrace(traceList.get(i1 + currentTracesAmount));
                }
            } else if ((Integer) newValue < currentTracesAmount) { // remove trace
                for (var i2 = 0; i2 < currentTracesAmount - (Integer) newValue; i2++) {
                    for (var traceProperty2 : TraceProperty.values()) {
                        var propID2 = XYGraphModel.makeTracePropID(traceProperty2.propIDPre, i2 + (Integer) newValue);
                        model.setPropertyVisible(propID2, false);
                    }
                    xyGraph.removeTrace(traceList.get(i2 + (Integer) newValue));
                }
            }
            return true;
        };
        getWidgetModel().getProperty(PROP_TRACE_COUNT).addPropertyChangeListener(
                evt -> handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));

        // setPropertyChangeHandler(XYGraphModel.PROP_TRACES_AMOUNT, handler);
    }

    protected void registerTracePropertyChangeHandlers() {
        var model = (XYGraphModel) getModel();
        // set prop handlers and init all the potential axes
        for (var i = 0; i < XYGraphModel.MAX_TRACES_AMOUNT; i++) {
            boolean concatenate = (Boolean) getWidgetModel()
                    .getProperty(XYGraphModel.makeTracePropID(TraceProperty.CONCATENATE_DATA.propIDPre, i))
                    .getPropertyValue();
            var xPVPropID = XYGraphModel.makeTracePropID(TraceProperty.XPV.propIDPre, i);
            var yPVPropID = XYGraphModel.makeTracePropID(TraceProperty.YPV.propIDPre, i);
            for (var traceProperty : TraceProperty.values()) {
                var propID = XYGraphModel.makeTracePropID(traceProperty.propIDPre, i);
                IWidgetPropertyChangeHandler handler = new TracePropertyChangeHandler(i, traceProperty, xPVPropID,
                        yPVPropID);

                if (concatenate) {
                    // cannot use setPropertyChangeHandler because the PV value has to be buffered
                    // which means that it cannot be ignored.
                    getWidgetModel().getProperty(propID).addPropertyChangeListener(evt -> UIBundlingThread.getInstance()
                            .addRunnable(getViewer().getControl().getDisplay(), () -> {
                                if (isActive()) {
                                    handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
                                }
                            }));
                } else {
                    setPropertyChangeHandler(propID, handler);
                }
            }
        }
        for (var i = XYGraphModel.MAX_TRACES_AMOUNT - 1; i >= model.getTracesAmount(); i--) {
            for (var traceProperty : TraceProperty.values()) {
                var propID = XYGraphModel.makeTracePropID(traceProperty.propIDPre, i);
                model.setPropertyVisible(propID, false);
            }
        }
    }

    protected void setTraceProperty(Trace trace, TraceProperty traceProperty, Object newValue, String xPVPropID,
            String yPVPropID) {
        var dataProvider = (CircularBufferDataProvider) trace.getDataProvider();
        switch (traceProperty) {
        case ANTI_ALIAS:
            trace.setAntiAliasing((Boolean) newValue);
            break;
        case BUFFER_SIZE:
            dataProvider.setBufferSize((Integer) newValue);
            break;
        // case CHRONOLOGICAL:
        // dataProvider.setChronological((Boolean)newValue);
        // break;
        // case CLEAR_TRACE:
        // if((Boolean)newValue)
        // dataProvider.clearTrace();
        // break;
        case LINE_WIDTH:
            trace.setLineWidth((Integer) newValue);
            break;
        case NAME:
            trace.setName((String) newValue);
            break;
        case PLOTMODE:
            dataProvider.setPlotMode(PlotMode.values()[(Integer) newValue]);
            break;
        case POINT_SIZE:
            trace.setPointSize((Integer) newValue);
            break;
        case POINT_STYLE:
            trace.setPointStyle(PointStyle.values()[(Integer) newValue]);
            break;
        case TRACE_COLOR:
            trace.setTraceColor(CustomMediaFactory.getInstance().getColor(((OPIColor) newValue).getRGBValue()));
            break;
        case TRACE_TYPE:
            trace.setTraceType(TraceType.values()[(Integer) newValue]);
            break;
        case CONCATENATE_DATA:
            dataProvider.setConcatenate_data((Boolean) newValue);
            break;
        // case TRIGGER_VALUE:
        // dataProvider.triggerUpdate();
        // break;
        case UPDATE_DELAY:
            dataProvider.setUpdateDelay((Integer) newValue);
            break;
        case UPDATE_MODE:
            dataProvider.setUpdateMode(UpdateMode.values()[(Integer) newValue]);
            break;
        case XAXIS_INDEX:
            if (!axisList.get((Integer) newValue).isYAxis()) {
                trace.setXAxis(axisList.get((Integer) newValue));
            }
            break;
        case YAXIS_INDEX:
            if (axisList.get((Integer) newValue).isYAxis()) {
                trace.setYAxis(axisList.get((Integer) newValue));
            }
            break;
        case XPV:
            if (newValue.toString() != null && newValue.toString().trim().length() > 0) {
                dataProvider.setChronological(false);
            } else {
                dataProvider.setChronological(true);
            }
            break;
        case XPV_VALUE:
            if (newValue == null || !(newValue instanceof VType)) {
                break;
            }
            if (dataProvider.isConcatenate_data()) {
                var pv = getPV(xPVPropID);
                if (pv != null) {
                    var value = pv.getValue();
                    if (value != null) {
                        setXValue(dataProvider, value);
                    }
                }
            } else {
                setXValue(dataProvider, (VType) newValue);
            }
            break;
        case YPV_VALUE:
            if (newValue == null || !(newValue instanceof VType)) {
                break;
            }
            if (dataProvider.isConcatenate_data()) {
                var pv = getPV(yPVPropID);
                if (pv != null) {
                    var value = pv.getValue();
                    if (value != null) {
                        setYValue(trace, dataProvider, value);
                    }
                }
            } else {
                setYValue(trace, dataProvider, (VType) newValue);
            }
            break;
        case VISIBLE:
            trace.setVisible((Boolean) newValue);
            break;
        default:
            break;
        }
    }

    private void setXValue(CircularBufferDataProvider dataProvider, VType value) {
        if (VTypeHelper.getSize(value) > 1) {
            dataProvider.setCurrentXDataArray(VTypeHelper.getDoubleArray(value));
        } else {
            dataProvider.setCurrentXData(VTypeHelper.getDouble(value));
        }
    }

    private void setYValue(Trace trace, CircularBufferDataProvider dataProvider, VType y_value) {
        if (VTypeHelper.getSize(y_value) == 1 && trace.getXAxis().isDateEnabled() && dataProvider.isChronological()) {
            var timestamp = VTypeHelper.getTimestamp(y_value);
            var time = timestamp.toEpochMilli();
            dataProvider.setCurrentYData(VTypeHelper.getDouble(y_value), time);
        } else {
            if (VTypeHelper.getSize(y_value) > 1) {
                dataProvider.setCurrentYDataArray(VTypeHelper.getDoubleArray(y_value));
            } else {
                dataProvider.setCurrentYData(VTypeHelper.getDouble(y_value));
            }
        }
    }

    class AxisPropertyChangeHandler implements IWidgetPropertyChangeHandler {
        private int axisIndex;
        private AxisProperty axisProperty;

        public AxisPropertyChangeHandler(int axisIndex, AxisProperty axisProperty) {
            this.axisIndex = axisIndex;
            this.axisProperty = axisProperty;
        }

        @Override
        public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
            var axis = axisList.get(axisIndex);
            setAxisProperty(axis, axisProperty, newValue);
            return true;
        }
    }

    class TracePropertyChangeHandler implements IWidgetPropertyChangeHandler {
        private int traceIndex;
        private TraceProperty traceProperty;
        private String xPVPropID;
        private String yPVPropID;

        public TracePropertyChangeHandler(int traceIndex, TraceProperty traceProperty, String xPVPropID,
                String yPVPropID) {
            this.traceIndex = traceIndex;
            this.traceProperty = traceProperty;
            this.xPVPropID = xPVPropID;
            this.yPVPropID = yPVPropID;
        }

        @Override
        public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
            var trace = traceList.get(traceIndex);
            setTraceProperty(trace, traceProperty, newValue, xPVPropID, yPVPropID);
            return false;
        }
    }

    @Override
    public void setValue(Object value) {
        throw new RuntimeException("XY Graph does not accept value");
    }

    @Override
    public Object getValue() {
        throw new RuntimeException("XY Graph does not have value");
    }

    /**
     * Clear the graph by deleting data in buffer.
     */
    public void clearGraph() {
        for (var i = 0; i < getWidgetModel().getTracesAmount(); i++) {
            ((CircularBufferDataProvider) traceList.get(i).getDataProvider()).clearTrace();
        }
    }

    public double[] getXBuffer(int i) {
        var dataProvider = (CircularBufferDataProvider) traceList.get(i).getDataProvider();
        var XBuffer = new double[dataProvider.getSize()];
        for (var j = 0; j < dataProvider.getSize(); j++) {
            XBuffer[j] = dataProvider.getSample(j).getXValue();
        }
        return XBuffer;
    }

    public double[] getYBuffer(int i) {
        var dataProvider = (CircularBufferDataProvider) traceList.get(i).getDataProvider();
        var YBuffer = new double[dataProvider.getSize()];
        for (var j = 0; j < dataProvider.getSize(); j++) {
            YBuffer[j] = dataProvider.getSample(j).getYValue();
        }
        return YBuffer;
    }

    protected List<Trace> getTraceList() {
        return Collections.unmodifiableList(traceList);
    }
}
