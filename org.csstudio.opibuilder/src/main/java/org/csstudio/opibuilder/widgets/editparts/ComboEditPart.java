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
import static org.csstudio.opibuilder.widgets.model.ComboModel.PROP_ITEMS;
import static org.csstudio.opibuilder.widgets.model.ComboModel.PROP_ITEMS_FROM_PV;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.figures.ComboFigure;
import org.csstudio.opibuilder.widgets.model.ComboModel;
import org.csstudio.swt.widgets.util.GraphicsUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.VDouble;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VType;

public final class ComboEditPart extends AbstractPVWidgetEditPart {

    private IPVListener loadItemsFromPVListener;

    private List<String> items = new ArrayList<>();

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();
        updatePropSheet(model.isItemsFromPV());
        var comboFigure = new ComboFigure();

        var items = getWidgetModel().getItems();
        updateCombo(items);

        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            comboFigure.addMouseListener(new MouseListener() {
                @Override
                public void mouseDoubleClicked(MouseEvent me) {
                }

                @Override
                public void mousePressed(MouseEvent me) {
                    if (me.button == 1 && comboFigure.containsPoint(me.getLocation())) {
                        me.consume();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                    // Check location to ignore bogus mouse clicks,
                    // see https://github.com/ControlSystemStudio/cs-studio/issues/1818
                    if (me.button == 1 && getExecutionMode().equals(ExecutionMode.RUN_MODE)
                            && comboFigure.containsPoint(me.getLocation())) {
                        var cursorLocation = Display.getCurrent().getCursorLocation();
                        showMenu(me.getLocation(), cursorLocation.x, cursorLocation.y);
                    }
                }

            });
        }
        comboFigure.addMouseMotionListener(new MouseMotionListener.Stub() {
            @Override
            public void mouseEntered(MouseEvent me) {
                if (getExecutionMode().equals(ExecutionMode.RUN_MODE)) {
                    var backColor = comboFigure.getBackgroundColor();
                    var darkColor = GraphicsUtil.mixColors(backColor.getRGB(), new RGB(0, 0, 0), 0.9);
                    comboFigure.setBackgroundColor(CustomMediaFactory.getInstance().getColor(darkColor));
                }
            }

            @Override
            public void mouseExited(MouseEvent me) {
                if (getExecutionMode().equals(ExecutionMode.RUN_MODE)) {
                    comboFigure.setBackgroundColor(
                            CustomMediaFactory.getInstance().getColor(getWidgetModel().getBackgroundColor()));
                }
            }
        });

        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);

        return comboFigure;
    }

    private void updateCombo(List<String> items) {
        if (items != null && getExecutionMode() == ExecutionMode.RUN_MODE) {
            this.items.clear();
            this.items.addAll(items);
        }
    }

    @Override
    public ComboModel getWidgetModel() {
        return (ComboModel) getModel();
    }

    private ComboFigure getComboFigure() {
        return (ComboFigure) getFigure();
    }

    /**
     * Show Menu
     *
     * @param point
     *            the location of the mouse-event in the OPI display
     * @param absolutX
     *            The x coordinate of the mouse on the monitor
     * @param absolutY
     *            The y coordinate of the mouse on the monitor
     */
    private void showMenu(Point point, int absolutX, int absolutY) {
        if (getExecutionMode().equals(ExecutionMode.RUN_MODE)) {
            var shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            var menuManager = new MenuManager();
            for (var item : items) {
                menuManager.add(new SelectItemAction(item));
            }
            var menu = menuManager.createContextMenu(shell);

            /*
             * We need to position the menu in absolute monitor coordinates.
             * First we calculate the coordinates of the display, then add the
             * widget coordinates to these so that the menu opens on the
             * bottom left of the widget.
             */
            var x = absolutX - point.x;
            var y = absolutY - point.y;
            x += getWidgetModel().getLocation().x;
            y += getWidgetModel().getLocation().y + getWidgetModel().getSize().height;

            menu.setLocation(x, y);
            menu.setVisible(true);
        }
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
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_PVNAME, (oldValue, newValue, figure) -> {
            registerLoadItemsListener();
            return false;
        });

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            if (newValue != null) {
                var stringValue = VTypeHelper.getString((VType) newValue);

                String matchingItem = null;
                for (var item : items) {
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
                    getComboFigure().setText(matchingItem);
                } else {
                    getComboFigure().setText(null);
                }
            }

            return true;
        });

        setPropertyChangeHandler(PROP_ITEMS, (oldValue, newValue, refreshableFigure) -> {
            if (newValue != null && newValue instanceof List) {
                updateCombo((List<String>) newValue);
                if (getWidgetModel().isItemsFromPV()) {
                    getComboFigure().setText(VTypeHelper.getString(getPVValue(PROP_PVNAME)));
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
    }

    private void updatePropSheet(boolean itemsFromPV) {
        getWidgetModel().setPropertyVisible(PROP_ITEMS, !itemsFromPV);
    }

    @Override
    public String getValue() {
        return getComboFigure().getText();
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof String) {
            getComboFigure().setText((String) value);
        } else if (value instanceof Number) {
            var idx = ((Number) value).intValue();
            if (idx >= 0 && idx <= items.size() - 1) {
                var item = items.get(idx);
                getComboFigure().setText(item);
            }
        } else {
            super.setValue(value);
        }
    }

    private class SelectItemAction extends Action {

        private String item;

        SelectItemAction(String item) {
            this.item = item;
            setText(item);
        }

        @Override
        public void run() {
            getComboFigure().setText(item);

            // Write value to pv if pv name is not empty
            if (getWidgetModel().getPVName().trim().length() > 0) {
                setPVValue(PROP_PVNAME, item);
            }
        }
    }
}
