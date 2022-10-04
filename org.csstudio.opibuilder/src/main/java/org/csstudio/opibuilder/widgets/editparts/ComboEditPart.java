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
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_HEIGHT;
import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_WIDTH;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.ComboModel.PROP_ITEMS;
import static org.csstudio.opibuilder.widgets.model.ComboModel.PROP_ITEMS_FROM_PV;

import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.figures.ComboFigure;
import org.csstudio.opibuilder.widgets.model.ComboModel;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VDouble;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VType;

public final class ComboEditPart extends AbstractPVWidgetEditPart {

    private IPVListener loadItemsFromPVListener;

    private Combo combo;
    private SelectionListener comboSelectionListener;

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();
        updatePropSheet(model.isItemsFromPV());
        var comboFigure = new ComboFigure(this);
        combo = comboFigure.getSWTWidget();

        var items = getWidgetModel().getItems();

        updateCombo(items);

        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);

        return comboFigure;
    }

    private void updateCombo(List<String> items) {
        if (items != null && getExecutionMode() == ExecutionMode.RUN_MODE) {
            combo.removeAll();

            for (var item : items) {
                combo.add(item);
            }

            // write value to pv if pv name is not empty
            if (getWidgetModel().getPVName().trim().length() > 0) {
                if (comboSelectionListener != null) {
                    combo.removeSelectionListener(comboSelectionListener);
                }
                comboSelectionListener = new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        setPVValue(PROP_PVNAME, combo.getText());
                    }
                };
                combo.addSelectionListener(comboSelectionListener);
            }
        }
    }

    @Override
    public ComboModel getWidgetModel() {
        return (ComboModel) getModel();
    }

    @Override
    protected void doActivate() {
        super.doActivate();
        registerLoadItemsListener();
    }

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
                                    var items = ((VEnum) value).getLabels();
                                    getWidgetModel().setPropertyValue(PROP_ITEMS, items);
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
        // ((ComboFigure)getFigure()).dispose();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_PVNAME, (oldValue, newValue, figure) -> {
            registerLoadItemsListener();
            return false;
        });

        autoSizeWidget((ComboFigure) getFigure());

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            if (newValue != null) {
                var stringValue = VTypeHelper.getString((VType) newValue);

                String matchingItem = null;
                for (var item : combo.getItems()) {
                    if (item.equals(stringValue)) {
                        matchingItem = item;
                        break;
                    }

                    // Especially when connected to local PV without type information,
                    // numeric combo items are converted to VDouble-type, which
                    // may not necessarily match the value returned by
                    // VTypeHelper.getString (for example: 200 becomes 200.0)
                    // Therefore, we try to match back the item by comparing
                    // by double value. (else the Combo would appear to lose its value)
                    if (newValue instanceof VDouble) {
                        try {
                            var itemValue = Double.valueOf(item);
                            if (itemValue.equals(VTypeHelper.getDouble((VDouble) newValue))) {
                                matchingItem = item;
                                break;
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }

                if (matchingItem != null) {
                    combo.setText(matchingItem);
                } else {
                    combo.deselectAll();
                }
            }

            return true;
        });

        setPropertyChangeHandler(PROP_ITEMS, (oldValue, newValue, refreshableFigure) -> {
            if (newValue != null && newValue instanceof List) {
                updateCombo((List<String>) newValue);
                if (getWidgetModel().isItemsFromPV()) {
                    combo.setText(VTypeHelper.getString(getPVValue(PROP_PVNAME)));
                }
            }
            return true;
        });

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            updatePropSheet((Boolean) newValue);
            return false;
        };
        getWidgetModel().getProperty(PROP_ITEMS_FROM_PV).addPropertyChangeListener(
                evt -> handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));

        // size change handlers--always apply the default height
        IWidgetPropertyChangeHandler handle = (oldValue, newValue, figure) -> {
            autoSizeWidget((ComboFigure) figure);
            return true;
        };
        setPropertyChangeHandler(PROP_WIDTH, handle);
        setPropertyChangeHandler(PROP_HEIGHT, handle);
        setPropertyChangeHandler(PROP_BORDER_STYLE, handle);
        setPropertyChangeHandler(PROP_BORDER_WIDTH, handle);
        setPropertyChangeHandler(PROP_FONT, handle);
    }

    private void updatePropSheet(boolean itemsFromPV) {
        getWidgetModel().setPropertyVisible(PROP_ITEMS, !itemsFromPV);
    }

    private void autoSizeWidget(ComboFigure comboFigure) {
        var d = comboFigure.getAutoSizeDimension();
        getWidgetModel().setSize(getWidgetModel().getWidth(), d.height);
    }

    @Override
    public String getValue() {
        return combo.getText();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof String) {
            combo.setText((String) value);
        } else if (value instanceof Number) {
            combo.select(((Number) value).intValue());
        } else {
            super.setValue(value);
        }
    }
}
