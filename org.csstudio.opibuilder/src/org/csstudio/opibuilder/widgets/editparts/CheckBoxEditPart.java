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
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_FONT;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.CheckBoxModel.PROP_AUTOSIZE;
import static org.csstudio.opibuilder.widgets.model.CheckBoxModel.PROP_BIT;
import static org.csstudio.opibuilder.widgets.model.CheckBoxModel.PROP_LABEL;
import static org.csstudio.opibuilder.widgets.model.CheckBoxModel.PROP_SELECTED_COLOR;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.CheckBoxModel;
import org.csstudio.swt.widgets.figures.AbstractBoolFigure.TotalBits;
import org.csstudio.swt.widgets.figures.CheckBoxFigure;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.swt.widgets.Display;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VType;

public class CheckBoxEditPart extends AbstractPVWidgetEditPart {

    @Override
    protected IFigure doCreateFigure() {
        var figure = new CheckBoxFigure(getExecutionMode().equals(ExecutionMode.RUN_MODE));
        figure.setBit(getWidgetModel().getBit());
        figure.setText(getWidgetModel().getLabel());
        figure.setSelectedColor(getWidgetModel().getSelectedColor().getSWTColor());
        figure.addManualValueChangeListener(newValue -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                setPVValue(PROP_PVNAME, newValue);
            }
        });
        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);

        return figure;
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        if (getExecutionMode() == ExecutionMode.EDIT_MODE) {
            installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new TextDirectEditPolicy());
        }
    }

    protected void performDirectEdit() {
        new TextEditManager(this, new LabelCellEditorLocator(getFigure()), false).show();
    }

    @Override
    public void performRequest(Request request) {
        if (getExecutionMode() == ExecutionMode.EDIT_MODE && (request.getType() == RequestConstants.REQ_DIRECT_EDIT
                || request.getType() == RequestConstants.REQ_OPEN)) {
            performDirectEdit();
        }
    }

    @Override
    public CheckBoxModel getWidgetModel() {
        return (CheckBoxModel) getModel();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            if (newValue == null) {
                return false;
            }
            var figure = (CheckBoxFigure) refreshableFigure;

            switch (VTypeHelper.getBasicDataType((VType) newValue)) {
            case SHORT:
                figure.setTotalBits(TotalBits.BITS_16);
                break;
            case INT:
            case ENUM:
                figure.setTotalBits(TotalBits.BITS_32);
                break;
            default:
                break;
            }

            figure.setValue(VTypeHelper.getDouble((VType) newValue));
            return true;
        });

        setPropertyChangeHandler(PROP_BIT, (oldValue, newValue, refreshableFigure) -> {
            var figure = (CheckBoxFigure) refreshableFigure;
            figure.setBit((Integer) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_LABEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (CheckBoxFigure) refreshableFigure;
            figure.setText((String) newValue);
            Display.getCurrent().timerExec(10, () -> {
                if (getWidgetModel().isAutoSize()) {
                    performAutoSize(refreshableFigure);
                }
            });
            return true;
        });

        setPropertyChangeHandler(PROP_AUTOSIZE, (oldValue, newValue, figure) -> {
            if ((Boolean) newValue) {
                performAutoSize(figure);
                figure.revalidate();
            }
            return true;
        });

        setPropertyChangeHandler(PROP_SELECTED_COLOR, (oldValue, newValue, figure) -> {
            ((CheckBoxFigure) figure).setSelectedColor(getWidgetModel().getSelectedColor().getSWTColor());
            return true;
        });

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, figure) -> {
            Display.getCurrent().timerExec(10, () -> {
                if (getWidgetModel().isAutoSize()) {
                    performAutoSize(figure);
                    figure.revalidate();
                }
            });

            return true;
        };
        setPropertyChangeHandler(PROP_FONT, handler);
        setPropertyChangeHandler(PROP_BORDER_STYLE, handler);
        setPropertyChangeHandler(PROP_BORDER_WIDTH, handler);
    }

    private void performAutoSize(IFigure figure) {
        getWidgetModel().setSize(((CheckBoxFigure) figure).getAutoSizeDimension());
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((CheckBoxFigure) getFigure()).setValue(((Number) value).longValue());
        } else if (value instanceof Boolean) {
            ((CheckBoxFigure) getFigure()).setBoolValue((Boolean) value);
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Boolean getValue() {
        return ((CheckBoxFigure) getFigure()).getBoolValue();
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
        if (key == ITextFigure.class) {
            return getFigure();
        }

        return super.getAdapter(key);
    }
}
