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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ENABLED;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.ScrollBarModel.PROP_BAR_LENGTH;
import static org.csstudio.opibuilder.widgets.model.ScrollBarModel.PROP_HORIZONTAL;
import static org.csstudio.opibuilder.widgets.model.ScrollBarModel.PROP_MAX;
import static org.csstudio.opibuilder.widgets.model.ScrollBarModel.PROP_MIN;
import static org.csstudio.opibuilder.widgets.model.ScrollBarModel.PROP_PAGE_INCREMENT;
import static org.csstudio.opibuilder.widgets.model.ScrollBarModel.PROP_SHOW_VALUE_TIP;
import static org.csstudio.opibuilder.widgets.model.ScrollBarModel.PROP_STEP_INCREMENT;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.widgets.model.ScrollBarModel;
import org.csstudio.swt.widgets.figures.ScrollbarFigure;
import org.eclipse.draw2d.IFigure;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.Display;
import org.yamcs.studio.data.vtype.VType;

public class ScrollbarEditPart extends AbstractPVWidgetEditPart {

    private IPVListener pvLoadLimitsListener;
    private Display meta = null;

    @Override
    public ScrollBarModel getWidgetModel() {
        return (ScrollBarModel) super.getWidgetModel();
    }

    @Override
    protected void doActivate() {
        super.doActivate();
        registerLoadLimitsListener();
    }

    /**
     *
     */
    private void registerLoadLimitsListener() {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            var model = getWidgetModel();
            if (model.isLimitsFromPV()) {
                var pv = getPV(PROP_PVNAME);
                if (pv != null) {
                    if (pvLoadLimitsListener == null) {
                        pvLoadLimitsListener = new IPVListener() {
                            @Override
                            public void valueChanged(IPV pv) {
                                var value = pv.getValue();
                                var displayInfo = VTypeHelper.getDisplayInfo(value);
                                if (value != null && displayInfo != null) {
                                    var new_meta = displayInfo;
                                    if (meta == null || !meta.equals(new_meta)) {
                                        meta = new_meta;
                                        model.setPropertyValue(PROP_MAX, meta.getUpperDisplayLimit());
                                        model.setPropertyValue(PROP_MIN, meta.getLowerDisplayLimit());
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
    protected IFigure doCreateFigure() {
        var scrollBar = new ScrollbarFigure();
        var model = getWidgetModel();

        scrollBar.setMaximum(model.getMaximum());
        scrollBar.setMinimum(model.getMinimum());
        scrollBar.setStepIncrement(model.getStepIncrement());
        scrollBar.setPageIncrement(model.getPageIncrement());
        scrollBar.setExtent(model.getBarLength());
        scrollBar.setShowValueTip(model.isShowValueTip());
        scrollBar.setHorizontal(model.isHorizontal());

        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            scrollBar.addManualValueChangeListener(newValue -> setPVValue(PROP_PVNAME, newValue));
        }

        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);
        return scrollBar;
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_PVNAME, (oldValue, newValue, figure) -> {
            registerLoadLimitsListener();
            return false;
        });

        ((ScrollbarFigure) getFigure())
                .setEnabled(getWidgetModel().isEnabled() && (getExecutionMode() == ExecutionMode.RUN_MODE));

        removeAllPropertyChangeHandlers(PROP_ENABLED);

        setPropertyChangeHandler(PROP_ENABLED, (oldValue, newValue, figure) -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                figure.setEnabled((Boolean) newValue);
            }
            return false;
        });

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            if (newValue == null) {
                return false;
            }
            ((ScrollbarFigure) refreshableFigure).setValue(VTypeHelper.getDouble((VType) newValue));
            return false;
        });

        setPropertyChangeHandler(PROP_MIN, (oldValue, newValue, refreshableFigure) -> {
            ((ScrollbarFigure) refreshableFigure).setMinimum((Double) newValue);

            return false;
        });

        setPropertyChangeHandler(PROP_MAX, (oldValue, newValue, refreshableFigure) -> {
            ((ScrollbarFigure) refreshableFigure).setMaximum((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_PAGE_INCREMENT, (oldValue, newValue, refreshableFigure) -> {
            ((ScrollbarFigure) refreshableFigure).setPageIncrement((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_STEP_INCREMENT, (oldValue, newValue, refreshableFigure) -> {
            ((ScrollbarFigure) refreshableFigure).setStepIncrement((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_BAR_LENGTH, (oldValue, newValue, refreshableFigure) -> {
            ((ScrollbarFigure) refreshableFigure).setExtent((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_VALUE_TIP, (oldValue, newValue, refreshableFigure) -> {
            ((ScrollbarFigure) refreshableFigure).setShowValueTip((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL, (oldValue, newValue, refreshableFigure) -> {
            ((ScrollbarFigure) refreshableFigure).setHorizontal((Boolean) newValue);
            var model = getWidgetModel();
            if ((Boolean) newValue) {
                model.setLocation(model.getLocation().x - model.getSize().height / 2 + model.getSize().width / 2,
                        model.getLocation().y + model.getSize().height / 2 - model.getSize().width / 2);
            } else {
                model.setLocation(model.getLocation().x + model.getSize().width / 2 - model.getSize().height / 2,
                        model.getLocation().y - model.getSize().width / 2 + model.getSize().height / 2);
            }

            model.setSize(model.getSize().height, model.getSize().width);
            return false;
        });
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

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((ScrollbarFigure) getFigure()).setValue(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Double getValue() {
        return ((ScrollbarFigure) getFigure()).getValue();
    }
}
