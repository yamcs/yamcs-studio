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
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.AbstractChoiceModel.PROP_HORIZONTAL;
import static org.csstudio.opibuilder.widgets.model.AbstractChoiceModel.PROP_ITEMS;
import static org.csstudio.opibuilder.widgets.model.AbstractChoiceModel.PROP_ITEMS_FROM_PV;
import static org.csstudio.opibuilder.widgets.model.AbstractChoiceModel.PROP_SELECTED_COLOR;

import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.AbstractChoiceModel;
import org.csstudio.swt.widgets.figures.AbstractChoiceFigure;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VType;

/**
 * The abstract editpart of choice widget.
 */
public abstract class AbstractChoiceEditPart extends AbstractPVWidgetEditPart {

    private IPVListener loadItemsFromPVListener;

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();
        updatePropSheet(model.isItemsFromPV());
        var choiceFigure = createChoiceFigure();
        choiceFigure.setSelectedColor(getWidgetModel().getSelectedColor().getSWTColor());

        choiceFigure.setFont(CustomMediaFactory.getInstance().getFont(model.getFont().getFontData()));

        choiceFigure.setHorizontal((Boolean) (model.getPropertyValue(PROP_HORIZONTAL)));
        if (!model.isItemsFromPV() || getExecutionMode() == ExecutionMode.EDIT_MODE) {
            var items = getWidgetModel().getItems();
            if (items != null) {
                choiceFigure.setStates(items);
            }
        }

        choiceFigure.addChoiceButtonListener((index, value) -> setPVValue(PROP_PVNAME, value));

        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);

        return choiceFigure;
    }

    protected abstract AbstractChoiceFigure createChoiceFigure();

    @Override
    public AbstractChoiceModel getWidgetModel() {
        return (AbstractChoiceModel) getModel();
    }

    @Override
    protected void doActivate() {
        super.doActivate();
        registerLoadItemsListener();
    }

    /**
     *
     */
    private void registerLoadItemsListener() {
        // load items from PV
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            if (getWidgetModel().isItemsFromPV()) {
                var pv = getPV(PROP_PVNAME);
                if (pv != null) {
                    if (loadItemsFromPVListener == null) {
                        loadItemsFromPVListener = new IPVListener() {
                            @Override
                            public void valueChanged(IPV pv) {
                                var value = pv.getValue();
                                if (value != null && value instanceof VEnum) {
                                    var new_meta = ((VEnum) value).getLabels();
                                    getWidgetModel().setPropertyValue(PROP_ITEMS, new_meta);
                                }
                            }
                        };
                    }
                    pv.addListener(loadItemsFromPVListener);
                }
            }
        }
    }

    @Override
    protected void doDeActivate() {
        super.doDeActivate();
        if (getWidgetModel().isItemsFromPV()) {
            var pv = getPV(PROP_PVNAME);
            if (pv != null && loadItemsFromPVListener != null) {
                pv.removeListener(loadItemsFromPVListener);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_PVNAME, (oldValue, newValue, figure) -> {
            registerLoadItemsListener();
            return false;
        });

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            if (newValue != null && newValue instanceof VType) {
                var stringValue = VTypeHelper.getString((VType) newValue);
                ((AbstractChoiceFigure) refreshableFigure).setState(stringValue);
            }
            return false;
        });

        setPropertyChangeHandler(PROP_ITEMS, (oldValue, newValue, refreshableFigure) -> {
            if (newValue != null && newValue instanceof List) {
                ((AbstractChoiceFigure) refreshableFigure).setStates(((List<String>) newValue));
                if (getWidgetModel().isItemsFromPV()) {
                    ((AbstractChoiceFigure) refreshableFigure)
                            .setState(VTypeHelper.getString(getPVValue(PROP_PVNAME)));
                }
            }
            return true;
        });

        IWidgetPropertyChangeHandler selectedColorHandler = (oldValue, newValue, figure) -> {
            ((AbstractChoiceFigure) figure).setSelectedColor(((OPIColor) newValue).getSWTColor());
            return false;
        };

        setPropertyChangeHandler(PROP_SELECTED_COLOR, selectedColorHandler);

        setPropertyChangeHandler(PROP_HORIZONTAL, (oldValue, newValue, figure) -> {
            ((AbstractChoiceFigure) figure).setHorizontal((Boolean) newValue);
            return true;
        });

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            if (!(Boolean) newValue) {
                ((AbstractChoiceFigure) refreshableFigure).setStates((getWidgetModel().getItems()));
            }
            updatePropSheet((Boolean) newValue);
            return false;
        };
        getWidgetModel().getProperty(PROP_ITEMS_FROM_PV).addPropertyChangeListener(
                evt -> handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));
    }

    private void updatePropSheet(boolean itemsFromPV) {
        getWidgetModel().setPropertyVisible(PROP_ITEMS, !itemsFromPV);
    }

    @Override
    public String getValue() {
        return ((AbstractChoiceFigure) getFigure()).getState();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof String) {
            ((AbstractChoiceFigure) getFigure()).setState((String) value);
        } else if (value instanceof Number) {
            ((AbstractChoiceFigure) getFigure()).setState(((Number) value).intValue());
        } else {
            super.setValue(value);
        }
    }
}
