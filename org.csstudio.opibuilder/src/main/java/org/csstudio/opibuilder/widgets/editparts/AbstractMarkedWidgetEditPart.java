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

import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_HIHI_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_HIHI_LEVEL;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_HI_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_HI_LEVEL;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_LOLO_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_LOLO_LEVEL;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_LO_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_LO_LEVEL;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_SHOW_HI;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_SHOW_HIHI;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_SHOW_LO;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_SHOW_LOLO;
import static org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel.PROP_SHOW_MARKERS;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_MAX;
import static org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel.PROP_MIN;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.AbstractMarkedWidgetModel;
import org.csstudio.opibuilder.widgets.model.AbstractScaledWidgetModel;
import org.csstudio.swt.widgets.figures.AbstractMarkedWidgetFigure;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.Display;

/**
 * Base editPart controller for a widget based on {@link AbstractMarkedWidgetModel}.
 */
public abstract class AbstractMarkedWidgetEditPart extends AbstractScaledWidgetEditPart {

    private Display meta = null;
    private IPVListener pvLoadLimitsListener;

    /**
     * Sets those properties on the figure that are defined in the {@link AbstractMarkedWidgetFigure} base class. This
     * method is provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link AbstractBaseEditPart#doCreateFigure()}.
     */
    protected void initializeCommonFigureProperties(AbstractMarkedWidgetFigure figure,
            AbstractMarkedWidgetModel model) {

        super.initializeCommonFigureProperties(figure, model);
        figure.setShowMarkers(model.isShowMarkers());

        figure.setLoloLevel(model.getLoloLevel());
        figure.setLoLevel(model.getLoLevel());
        figure.setHiLevel(model.getHiLevel());
        figure.setHihiLevel(model.getHihiLevel());

        figure.setShowLolo(model.isShowLolo());
        figure.setShowLo(model.isShowLo());
        figure.setShowHi(model.isShowHi());
        figure.setShowHihi(model.isShowHihi());

        figure.setLoloColor(model.getLoloColor());
        figure.setLoColor(model.getLoColor());
        figure.setHiColor(model.getHiColor());
        figure.setHihiColor(model.getHihiColor());
    }

    @Override
    protected void doActivate() {
        super.doActivate();
        registerLoadLimitsListener();
    }

    private void registerLoadLimitsListener() {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            var model = (AbstractMarkedWidgetModel) getModel();
            if (model.isLimitsFromPV()) {
                var pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
                if (pv != null) {
                    if (pvLoadLimitsListener == null) {
                        pvLoadLimitsListener = new IPVListener() {
                            @Override
                            public void valueChanged(IPV pv) {
                                var value = pv.getValue();
                                if (value != null && VTypeHelper.getDisplayInfo(value) != null) {
                                    var new_meta = VTypeHelper.getDisplayInfo(value);
                                    if (meta == null || !meta.equals(new_meta)) {
                                        meta = new_meta;

                                        Double upperLimit;
                                        Double lowerLimit;
                                        if (model.isControlWidget()) {
                                            upperLimit = meta.getUpperCtrlLimit();
                                            lowerLimit = meta.getLowerCtrlLimit();
                                        } else {
                                            upperLimit = meta.getUpperDisplayLimit();
                                            lowerLimit = meta.getLowerDisplayLimit();
                                        }
                                        if (!Double.isNaN(upperLimit)) {
                                            model.setPropertyValue(PROP_MAX, upperLimit);
                                        }
                                        if (!Double.isNaN(lowerLimit)) {
                                            model.setPropertyValue(PROP_MIN, lowerLimit);
                                        }

                                        if (Double.isNaN(meta.getUpperWarningLimit())) {
                                            model.setPropertyValue(PROP_SHOW_HI, false);
                                        } else {
                                            model.setPropertyValue(PROP_SHOW_HI, true);
                                            model.setPropertyValue(PROP_HI_LEVEL, meta.getUpperWarningLimit());
                                        }
                                        if (Double.isNaN(meta.getUpperAlarmLimit())) {
                                            model.setPropertyValue(PROP_SHOW_HIHI, false);
                                        } else {
                                            model.setPropertyValue(PROP_SHOW_HIHI, true);
                                            model.setPropertyValue(PROP_HIHI_LEVEL, meta.getUpperAlarmLimit());
                                        }
                                        if (Double.isNaN(meta.getLowerWarningLimit())) {
                                            model.setPropertyValue(PROP_SHOW_LO, false);
                                        } else {
                                            model.setPropertyValue(PROP_SHOW_LO, true);
                                            model.setPropertyValue(PROP_LO_LEVEL, meta.getLowerWarningLimit());
                                        }
                                        if (Double.isNaN(meta.getLowerAlarmLimit())) {
                                            model.setPropertyValue(PROP_SHOW_LOLO, false);
                                        } else {
                                            model.setPropertyValue(PROP_SHOW_LOLO, true);
                                            model.setPropertyValue(PROP_LOLO_LEVEL, meta.getLowerAlarmLimit());
                                        }
                                    }
                                }
                            }
                        };
                    }
                    pv.addListener(pvLoadLimitsListener);
                }
            }
        }
    }

    @Override
    public AbstractMarkedWidgetModel getWidgetModel() {
        return (AbstractMarkedWidgetModel) getModel();
    }

    @Override
    protected void doDeActivate() {
        super.doDeActivate();
        if (getWidgetModel().isLimitsFromPV()) {
            var pv = getPV(PROP_PVNAME);
            if (pv != null && pvLoadLimitsListener != null) {
                pv.removeListener(pvLoadLimitsListener);
            }
        }
    }

    /**
     * Registers property change handlers for the properties defined in {@link AbstractScaledWidgetModel}. This method
     * is provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link #registerPropertyChangeHandlers()}.
     */
    @Override
    protected void registerCommonPropertyChangeHandlers() {
        super.registerCommonPropertyChangeHandlers();

        setPropertyChangeHandler(PROP_PVNAME, (oldValue, newValue, figure) -> {
            registerLoadLimitsListener();
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_MARKERS, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowMarkers((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_LOLO_LEVEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setLoloLevel((Double) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_LO_LEVEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setLoLevel((Double) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_HI_LEVEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setHiLevel((Double) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_HIHI_LEVEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setHihiLevel((Double) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_LOLO, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowLolo((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_LO, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowLo((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_HI, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowHi((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_HIHI, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setShowHihi((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_LOLO_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setLoloColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_LO_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setLoColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_HI_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setHiColor(((OPIColor) newValue).getSWTColor());
            return false;
        });

        setPropertyChangeHandler(PROP_HIHI_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractMarkedWidgetFigure) refreshableFigure;
            figure.setHihiColor(((OPIColor) newValue).getSWTColor());
            return false;
        });
    }
}
