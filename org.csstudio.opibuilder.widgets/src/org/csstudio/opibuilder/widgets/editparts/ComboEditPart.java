package org.csstudio.opibuilder.widgets.editparts;

import java.util.Arrays;
import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
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

        markAsControlPV(AbstractPVWidgetModel.PROP_PVNAME, AbstractPVWidgetModel.PROP_PVVALUE);

        return comboFigure;
    }

    private void updateCombo(List<String> items) {
        if (items != null && getExecutionMode() == ExecutionMode.RUN_MODE) {
            combo.removeAll();

            for (String item : items) {
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
                        setPVValue(AbstractPVWidgetModel.PROP_PVNAME, combo.getText());
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
                var pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
                if (pv != null) {
                    if (loadItemsFromPVListener == null) {
                        loadItemsFromPVListener = new IPVListener() {
                            @Override
                            public void valueChanged(IPV pv) {
                                var value = pv.getValue();
                                if (value != null && value instanceof VEnum) {
                                    var items = ((VEnum) value).getLabels();
                                    getWidgetModel().setPropertyValue(ComboModel.PROP_ITEMS, items);
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
            var pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
            if (pv != null && loadItemsFromPVListener != null) {
                pv.removeListener(loadItemsFromPVListener);
            }
        }
        // ((ComboFigure)getFigure()).dispose();
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        IWidgetPropertyChangeHandler pvNameHandler = (oldValue, newValue, figure) -> {
            registerLoadItemsListener();
            return false;
        };
        setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVNAME, pvNameHandler);

        autoSizeWidget((ComboFigure) getFigure());
        // PV_Value
        IWidgetPropertyChangeHandler pvhandler = (oldValue, newValue, refreshableFigure) -> {
            if (newValue != null) {
                var stringValue = VTypeHelper.getString((VType) newValue);
                if (Arrays.asList(combo.getItems()).contains(stringValue)) {
                    combo.setText(stringValue);
                } else {
                    combo.deselectAll();
                    //
                    // if(getWidgetModel().isBorderAlarmSensitve())
                    // autoSizeWidget((ComboFigure) refreshableFigure);
                }
            }

            return true;
        };
        setPropertyChangeHandler(ComboModel.PROP_PVVALUE, pvhandler);

        // Items
        IWidgetPropertyChangeHandler itemsHandler = new IWidgetPropertyChangeHandler() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean handleChange(Object oldValue, Object newValue, IFigure refreshableFigure) {
                if (newValue != null && newValue instanceof List) {
                    updateCombo((List<String>) newValue);
                    if (getWidgetModel().isItemsFromPV()) {
                        combo.setText(VTypeHelper.getString(getPVValue(AbstractPVWidgetModel.PROP_PVNAME)));
                    }
                }
                return true;
            }
        };
        setPropertyChangeHandler(ComboModel.PROP_ITEMS, itemsHandler);

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            updatePropSheet((Boolean) newValue);
            return false;
        };
        getWidgetModel().getProperty(ComboModel.PROP_ITEMS_FROM_PV).addPropertyChangeListener(
                evt -> handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));

        // size change handlers--always apply the default height
        IWidgetPropertyChangeHandler handle = (oldValue, newValue, figure) -> {
            autoSizeWidget((ComboFigure) figure);
            return true;
        };
        setPropertyChangeHandler(AbstractWidgetModel.PROP_WIDTH, handle);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_HEIGHT, handle);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_STYLE, handle);
        setPropertyChangeHandler(AbstractWidgetModel.PROP_BORDER_WIDTH, handle);
        setPropertyChangeHandler(ComboModel.PROP_FONT, handle);
    }

    private void updatePropSheet(boolean itemsFromPV) {
        getWidgetModel().setPropertyVisible(ComboModel.PROP_ITEMS, !itemsFromPV);
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
