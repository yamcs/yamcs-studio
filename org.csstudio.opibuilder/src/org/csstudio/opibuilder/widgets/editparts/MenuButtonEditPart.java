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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ACTIONS;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.MenuButtonModel.PROP_ACTIONS_FROM_PV;
import static org.csstudio.opibuilder.widgets.model.MenuButtonModel.PROP_LABEL;
import static org.csstudio.opibuilder.widgets.model.MenuButtonModel.PROP_SHOW_DOWN_ARROW;
import static org.csstudio.opibuilder.widgets.model.MenuButtonModel.PROP_TRANSPARENT;

import java.util.List;

import org.csstudio.opibuilder.actions.WidgetActionMenuAction;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgetActions.ActionsInput;
import org.csstudio.opibuilder.widgetActions.WritePVAction;
import org.csstudio.opibuilder.widgets.figures.MenuButtonFigure;
import org.csstudio.opibuilder.widgets.model.MenuButtonModel;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.csstudio.swt.widgets.util.GraphicsUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.vtype.Scalar;
import org.yamcs.studio.data.vtype.VEnum;
import org.yamcs.studio.data.vtype.VType;

public final class MenuButtonEditPart extends AbstractPVWidgetEditPart {

    private IPVListener loadActionsFromPVListener;

    private List<String> meta = null;

    @Override
    protected IFigure doCreateFigure() {
        var model = (MenuButtonModel) getWidgetModel();
        updatePropSheet(model.isActionsFromPV());
        var figure = new MenuButtonFigure();
        figure.setOpaque(!model.isTransparent());
        figure.setText(model.getLabel());

        figure.setDownArrowVisible(model.showDownArrow());

        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            figure.addMouseListener(new MouseListener() {
                @Override
                public void mouseDoubleClicked(MouseEvent me) {
                }

                @Override
                public void mousePressed(MouseEvent me) {
                    if (me.button == 1 && figure.containsPoint(me.getLocation())) {
                        me.consume();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                    // Check location to ignore bogus mouse clicks,
                    // see https://github.com/ControlSystemStudio/cs-studio/issues/1818
                    if (me.button == 1 && getExecutionMode().equals(ExecutionMode.RUN_MODE)
                            && figure.containsPoint(me.getLocation())) {
                        var cursorLocation = Display.getCurrent().getCursorLocation();
                        showMenu(me.getLocation(), cursorLocation.x, cursorLocation.y);
                    }
                }

            });
        }
        figure.addMouseMotionListener(new MouseMotionListener.Stub() {
            @Override
            public void mouseEntered(MouseEvent me) {
                if (getExecutionMode().equals(ExecutionMode.RUN_MODE)) {
                    var backColor = figure.getBackgroundColor();
                    var darkColor = GraphicsUtil.mixColors(backColor.getRGB(), new RGB(0, 0, 0), 0.9);
                    figure.setBackgroundColor(CustomMediaFactory.getInstance().getColor(darkColor));
                }

            }

            @Override
            public void mouseExited(MouseEvent me) {
                if (getExecutionMode().equals(ExecutionMode.RUN_MODE)) {
                    figure.setBackgroundColor(
                            CustomMediaFactory.getInstance().getColor(getWidgetModel().getBackgroundColor()));
                }
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

    @Override
    public void performRequest(Request request) {
        if (getExecutionMode() == ExecutionMode.EDIT_MODE && (request.getType() == RequestConstants.REQ_DIRECT_EDIT
                || request.getType() == RequestConstants.REQ_OPEN)) {
            new TextEditManager(this, new LabelCellEditorLocator(getFigure()), false).show();
        }
    }

    @Override
    public MenuButtonModel getWidgetModel() {
        return (MenuButtonModel) getModel();
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
            for (var action : getWidgetModel().getActionsInput().getActionsList()) {
                menuManager.add(new WidgetActionMenuAction(action));
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
        registerLoadActionsListener();
    }

    private void registerLoadActionsListener() {
        if (getExecutionMode() == ExecutionMode.RUN_MODE) {
            if (getWidgetModel().isActionsFromPV()) {
                var pv = getPV(PROP_PVNAME);
                if (pv != null) {
                    if (loadActionsFromPVListener == null) {
                        loadActionsFromPVListener = new IPVListener() {
                            @Override
                            public void valueChanged(IPV pv) {
                                var value = pv.getValue();
                                if (value != null && value instanceof VEnum) {
                                    var new_meta = ((VEnum) value).getLabels();
                                    if (meta == null || !meta.equals(new_meta)) {
                                        meta = new_meta;
                                        var actionsInput = new ActionsInput();
                                        for (var writeValue : meta) {
                                            var action = new WritePVAction();
                                            action.setPropertyValue(WritePVAction.PROP_PVNAME,
                                                    getWidgetModel().getPVName());
                                            action.setPropertyValue(WritePVAction.PROP_VALUE, writeValue);
                                            action.setPropertyValue(WritePVAction.PROP_DESCRIPTION, writeValue);
                                            actionsInput.getActionsList().add(action);
                                        }
                                        getWidgetModel().setPropertyValue(AbstractWidgetModel.PROP_ACTIONS,
                                                actionsInput);

                                    }
                                }
                            }

                        };
                    }
                    pv.addListener(loadActionsFromPVListener);
                }
            }
        }
    }

    @Override
    protected void doDeActivate() {
        super.doDeActivate();
        if (getWidgetModel().isActionsFromPV()) {
            var pv = getPV(PROP_PVNAME);
            if (pv != null && loadActionsFromPVListener != null) {
                pv.removeListener(loadActionsFromPVListener);
            }
        }
    }

    @Override
    protected void registerPropertyChangeHandlers() {
        setPropertyChangeHandler(PROP_PVNAME, (oldValue, newValue, figure) -> {
            registerLoadActionsListener();
            return false;
        });

        setPropertyChangeHandler(PROP_PVVALUE, (oldValue, newValue, refreshableFigure) -> {
            if ((newValue != null) && (newValue instanceof Scalar)) {
                ((MenuButtonFigure) refreshableFigure).setText(VTypeHelper.getString((VType) newValue));
            }
            return true;
        });

        setPropertyChangeHandler(PROP_LABEL, (oldValue, newValue, refreshableFigure) -> {
            ((MenuButtonFigure) refreshableFigure).setText(newValue.toString());
            return true;
        });

        setPropertyChangeHandler(PROP_TRANSPARENT, (oldValue, newValue, refreshableFigure) -> {
            ((MenuButtonFigure) refreshableFigure).setOpaque(!(Boolean) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_SHOW_DOWN_ARROW, (oldValue, newValue, refreshableFigure) -> {
            ((MenuButtonFigure) refreshableFigure).setDownArrowVisible((boolean) newValue);
            return true;
        });

        IWidgetPropertyChangeHandler handler = (oldValue, newValue, refreshableFigure) -> {
            updatePropSheet((Boolean) newValue);
            return false;
        };
        getWidgetModel().getProperty(PROP_ACTIONS_FROM_PV).addPropertyChangeListener(
                evt -> handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));
    }

    private void updatePropSheet(boolean actionsFromPV) {
        getWidgetModel().setPropertyVisible(PROP_ACTIONS, !actionsFromPV);
    }

    public int size() {
        // always one sample
        return 1;
    }

    @Override
    public String getValue() {
        return ((MenuButtonFigure) getFigure()).getText();
    }

    @Override
    public void setValue(Object value) {
        ((MenuButtonFigure) getFigure()).setText(value.toString());
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
        if (key == ITextFigure.class) {
            return getFigure();
        }

        return super.getAdapter(key);
    }
}
