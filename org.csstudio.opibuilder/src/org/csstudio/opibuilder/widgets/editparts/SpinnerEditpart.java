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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_FONT;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_ALIGN_H;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_ALIGN_V;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TEXT;
import static org.csstudio.opibuilder.widgets.model.LabelModel.PROP_TRANSPARENT;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_BUTTONS_ON_LEFT;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_FORMAT;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_HORIZONTAL_BUTTONS_LAYOUT;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_MAX;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_MIN;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_PAGE_INCREMENT;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_PRECISION;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_SHOW_TEXT;
import static org.csstudio.opibuilder.widgets.model.SpinnerModel.PROP_STEP_INCREMENT;

import java.text.DecimalFormat;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.util.OPIFont;
import org.csstudio.opibuilder.widgets.model.SpinnerModel;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.swt.widgets.figures.SpinnerFigure;
import org.csstudio.swt.widgets.figures.SpinnerFigure.NumericFormatType;
import org.csstudio.swt.widgets.figures.TextFigure.H_ALIGN;
import org.csstudio.swt.widgets.figures.TextFigure.V_ALIGN;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.Display;
import org.yamcs.studio.data.vtype.VType;

public class SpinnerEditpart extends AbstractPVWidgetEditPart {

    private IPVListener pvLoadLimitsListener;
    private Display meta = null;
    private IPVListener pvLoadPrecisionListener;

    @Override
    protected IFigure doCreateFigure() {
        var spinner = new SpinnerFigure();
        var labelFigure = spinner.getLabelFigure();
        labelFigure.setFont(getWidgetModel().getFont().getSWTFont());
        labelFigure.setFontPixels(getWidgetModel().getFont().isSizeInPixels());
        labelFigure.setOpaque(!getWidgetModel().isTransparent());
        labelFigure.setHorizontalAlignment(getWidgetModel().getHorizontalAlignment());
        labelFigure.setVerticalAlignment(getWidgetModel().getVerticalAlignment());
        spinner.setMax(getWidgetModel().getMaximum());
        spinner.setMin(getWidgetModel().getMinimum());
        spinner.setStepIncrement(getWidgetModel().getStepIncrement());
        spinner.setPageIncrement(getWidgetModel().getPageIncrement());
        spinner.setFormatType(getWidgetModel().getFormat());
        spinner.setPrecision((Integer) getPropertyValue(PROP_PRECISION));
        spinner.setArrowButtonsOnLeft(getWidgetModel().isButtonsOnLeft());
        spinner.setArrowButtonsHorizontal(getWidgetModel().isHorizontalButtonsLayout());
        spinner.showText(getWidgetModel().showText());

        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            spinner.addManualValueChangeListener(newValue -> {
                setPVValue(PROP_PVNAME, newValue);
                getWidgetModel().setText(((SpinnerFigure) getFigure()).getLabelFigure().getText(), false);
            });
        }

        return spinner;
    }

    @Override
    public SpinnerModel getWidgetModel() {
        return (SpinnerModel) getModel();
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new SpinnerDirectEditPolicy());
    }

    @Override
    public void activate() {
        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);
        super.activate();
    }

    @Override
    protected void doActivate() {
        super.doActivate();
        registerLoadLimitsListener();
    }

    private void registerLoadLimitsListener() {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            var model = getWidgetModel();
            if (model.isLimitsFromPV() || model.isPrecisionFromPV()) {
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
                                        if (model.isLimitsFromPV()) {
                                            model.setPropertyValue(PROP_MAX, meta.getUpperCtrlLimit());
                                            model.setPropertyValue(PROP_MIN, meta.getLowerCtrlLimit());
                                        }
                                        if (model.isPrecisionFromPV()) {
                                            model.setPropertyValue(PROP_PRECISION,
                                                    meta.getFormat().getMaximumFractionDigits());
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
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_TEXT, (oldValue, newValue, figure) -> {
            var text = (String) newValue;
            try {
                text = text.replace("e", "E");
                var value = new DecimalFormat().parse(text).doubleValue();
                // coerce value in range
                value = Math.max(((SpinnerFigure) figure).getMin(), Math.min(((SpinnerFigure) figure).getMax(), value));
                ((SpinnerFigure) figure).setValue(value);
                if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                    setPVValue(PROP_PVNAME, value);
                }
                getWidgetModel().setText(((SpinnerFigure) figure).getLabelFigure().getText(), false);
                return false;
            } catch (Exception e) {
                return false;
            }
        });

        setPropertyChangeHandler(PROP_PVNAME, (oldValue, newValue, figure) -> {
            registerLoadLimitsListener();
            return false;
        });

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, figure) -> {
            if (newValue == null) {
                return false;
            }
            var value = VTypeHelper.getDouble((VType) newValue);
            ((SpinnerFigure) figure).setDisplayValue(value);
            getWidgetModel().setText(((SpinnerFigure) figure).getLabelFigure().getText(), false);
            return false;
        });

        setPropertyChangeHandler(PROP_MIN, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setMin((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_MAX, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setMax((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_STEP_INCREMENT, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setStepIncrement((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_PAGE_INCREMENT, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setPageIncrement((Double) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_FONT, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).getLabelFigure()
                    .setFont(CustomMediaFactory.getInstance().getFont(((OPIFont) newValue).getFontData()));
            return true;
        });

        setPropertyChangeHandler(PROP_ALIGN_H, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).getLabelFigure().setHorizontalAlignment(H_ALIGN.values()[(Integer) newValue]);
            return true;
        });

        setPropertyChangeHandler(PROP_ALIGN_V, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).getLabelFigure().setVerticalAlignment(V_ALIGN.values()[(Integer) newValue]);
            return true;
        });

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).getLabelFigure().setOpaque(!(Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_FORMAT, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setFormatType(NumericFormatType.values()[(Integer) newValue]);
            return false;
        });

        setPropertyChangeHandler(PROP_PRECISION, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setPrecision((Integer) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_BUTTONS_ON_LEFT, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setArrowButtonsOnLeft((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_HORIZONTAL_BUTTONS_LAYOUT, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).setArrowButtonsHorizontal((Boolean) newValue);
            return false;
        });

        setPropertyChangeHandler(PROP_SHOW_TEXT, (oldValue, newValue, figure) -> {
            ((SpinnerFigure) figure).showText((Boolean) newValue);
            return false;
        });
    }

    @Override
    public DragTracker getDragTracker(Request request) {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            return new SelectEditPartTracker(this) {
                @Override
                protected boolean handleButtonUp(int button) {
                    if (button == 1) {
                        // make widget in edit mode by single click
                        performOpen();
                    }
                    return super.handleButtonUp(button);
                }
            };
        } else {
            return super.getDragTracker(request);
        }
    }

    @Override
    public void performRequest(Request request) {
        if (getFigure().isEnabled() && getWidgetModel().showText()
                && ((request.getType() == RequestConstants.REQ_DIRECT_EDIT
                        && getExecutionMode() != ExecutionMode.RUN_MODE)
                        || request.getType() == RequestConstants.REQ_OPEN)) {
            performDirectEdit();
        }
    }

    protected void performDirectEdit() {
        new SpinnerTextEditManager(this, new LabelCellEditorLocator(((SpinnerFigure) getFigure()).getLabelFigure()),
                false, ((SpinnerFigure) figure).getStepIncrement(), ((SpinnerFigure) figure).getPageIncrement()).show();
    }

    @Override
    protected void doDeActivate() {
        super.doDeActivate();
        if (getWidgetModel().isLimitsFromPV()) {
            var pv = getPV(PROP_PVNAME);
            if (pv != null && pvLoadLimitsListener != null) {
                pv.removeListener(pvLoadLimitsListener);
            }
            if (pv != null && pvLoadPrecisionListener != null) {
                pv.removeListener(pvLoadPrecisionListener);
            }
        }
    }

    @Override
    public Double getValue() {
        return ((SpinnerFigure) getFigure()).getValue();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((SpinnerFigure) getFigure()).setValue(((Number) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class key) {
        if (key == ITextFigure.class) {
            return ((SpinnerFigure) getFigure()).getLabelFigure();
        }

        return super.getAdapter(key);
    }
}
