/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_BIT;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_BOOL_LABEL_POS;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_DATA_TYPE;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_OFF_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_OFF_LABEL;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_OFF_STATE;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_ON_COLOR;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_ON_LABEL;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_ON_STATE;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel.PROP_SHOW_BOOL_LABEL;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel;
import org.csstudio.swt.widgets.figures.AbstractBoolFigure;
import org.csstudio.swt.widgets.figures.AbstractBoolFigure.BoolLabelPosition;
import org.csstudio.swt.widgets.figures.AbstractBoolFigure.TotalBits;
import org.csstudio.ui.util.CustomMediaFactory;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VType;

/**
 * Base editPart controller for a widget based on {@link AbstractBoolWidgetModel}.
 */
public abstract class AbstractBoolEditPart extends AbstractPVWidgetEditPart {

    /**
     * Sets those properties on the figure that are defined in the {@link AbstractBoolFigure} base class. This method is
     * provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link AbstractBaseEditPart#doCreateFigure()}.
     *
     * @param figure
     *            the figure.
     * @param model
     *            the model.
     */
    protected void initializeCommonFigureProperties(AbstractBoolFigure figure, AbstractBoolWidgetModel model) {
        if (model.getDataType() == 0) {
            figure.setBit(model.getBit());
        } else {
            figure.setBit(-1);
        }
        updatePropSheet(model.getDataType());
        figure.setShowBooleanLabel(model.isShowBoolLabel());
        figure.setOnLabel(model.getOnLabel());
        figure.setOffLabel(model.getOffLabel());
        figure.setOnColor(model.getOnColor());
        figure.setOffColor(model.getOffColor());
        figure.setFont(CustomMediaFactory.getInstance().getFont(model.getFont().getFontData()));
        figure.setBoolLabelPosition(model.getBoolLabelPosition());
    }

    @Override
    public AbstractBoolWidgetModel getWidgetModel() {
        return (AbstractBoolWidgetModel) getModel();
    }

    /**
     * Registers property change handlers for the properties defined in {@link AbstractBoolWidgetModel}. This method is
     * provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link #registerPropertyChangeHandlers()}.
     */
    protected void registerCommonPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            if (newValue == null || !(newValue instanceof VType)) {
                return false;
            }
            var figure = (AbstractBoolFigure) refreshableFigure;

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
            updateFromValue((VType) newValue, figure);
            return true;
        });

        setPropertyChangeHandler(PROP_BIT, (oldValue, newValue, refreshableFigure) -> {
            if (getWidgetModel().getDataType() != 0) {
                return false;
            }
            var figure = (AbstractBoolFigure) refreshableFigure;
            figure.setBit((Integer) newValue);
            updateFromValue(getPVValue(PROP_PVNAME), figure);
            return true;
        });

        IWidgetPropertyChangeHandler dataTypeHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolFigure) refreshableFigure;
            if ((Integer) newValue == 0) {
                figure.setBit(getWidgetModel().getBit());
            } else {
                figure.setBit(-1);
            }
            updateFromValue(getPVValue(PROP_PVNAME), figure);
            updatePropSheet((Integer) newValue);
            return true;
        };
        getWidgetModel().getProperty(PROP_DATA_TYPE).addPropertyChangeListener(
                evt -> dataTypeHandler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));

        setPropertyChangeHandler(PROP_ON_STATE, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolFigure) refreshableFigure;
            updateFromValue(getPVValue(PROP_PVNAME), figure);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_BOOL_LABEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolFigure) refreshableFigure;
            figure.setShowBooleanLabel((Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_BOOL_LABEL_POS, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolFigure) refreshableFigure;
            figure.setBoolLabelPosition(BoolLabelPosition.values()[(Integer) newValue]);
            return false;
        });

        setPropertyChangeHandler(PROP_ON_LABEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolFigure) refreshableFigure;
            figure.setOnLabel((String) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_OFF_LABEL, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolFigure) refreshableFigure;
            figure.setOffLabel((String) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_ON_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolFigure) refreshableFigure;
            figure.setOnColor(((OPIColor) newValue).getSWTColor());
            return true;
        });

        setPropertyChangeHandler(PROP_OFF_COLOR, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolFigure) refreshableFigure;
            figure.setOffColor(((OPIColor) newValue).getSWTColor());
            return true;
        });
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Number) {
            ((AbstractBoolFigure) getFigure()).setValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            ((AbstractBoolFigure) getFigure()).setBooleanValue((Boolean) value);
        } else {
            super.setValue(value);
        }
    }

    @Override
    public Boolean getValue() {
        return ((AbstractBoolFigure) getFigure()).getBooleanValue();
    }

    private void updateFromValue(VType newValue, AbstractBoolFigure figure) {
        if (newValue == null) {
            return;
        }
        if (getWidgetModel().getDataType() == 1 /* Enum */) {
            var stringValue = VTypeHelper.getString(newValue);
            if (stringValue.equals(getWidgetModel().getOnState())) {
                figure.setValue(1);
            } else {
                figure.setValue(0);
            }
        } else {
            figure.setValue(VTypeHelper.getDouble(newValue));
        }
    }

    private void updatePropSheet(int dataType) {
        getWidgetModel().setPropertyVisible(PROP_BIT, dataType == 0);
        getWidgetModel().setPropertyVisible(PROP_ON_STATE, dataType == 1);
        getWidgetModel().setPropertyVisible(PROP_OFF_STATE, dataType == 1);
    }
}
