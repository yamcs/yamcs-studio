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

import java.beans.PropertyChangeListener;
import java.util.List;

import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.util.WidgetsService;
import org.csstudio.opibuilder.widgetActions.AbstractWidgetAction;
import org.csstudio.opibuilder.widgets.model.ActionButtonModel;
import org.csstudio.swt.widgets.figures.ITextFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

/**
 * EditPart controller for the ActioButton widget. The controller mediates between {@link ActionButtonModel} and
 * ActionButtonFigure2.
 */
public class ActionButtonEditPart extends AbstractPVWidgetEditPart {

    private IButtonEditPartDelegate delegate;

    @Override
    protected IFigure doCreateFigure() {
        var model = getWidgetModel();

        switch (model.getStyle()) {
        case NATIVE:
            delegate = new NativeButtonEditPartDelegate(this);
            break;
        case CLASSIC:
        default:
            delegate = new Draw2DButtonEditPartDelegate(this);
            break;
        }
        updatePropSheet();
        markAsControlPV(AbstractPVWidgetModel.PROP_PVNAME, AbstractPVWidgetModel.PROP_PVVALUE);
        return delegate.doCreateFigure();
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
    protected void hookMouseClickAction() {
        delegate.hookMouseClickAction();
    }

    @Override
    public List<AbstractWidgetAction> getHookedActions() {
        var widgetModel = getWidgetModel();
        var isSelected = delegate.isSelected();
        return getHookedActionsForButton(widgetModel, isSelected);
    }

    /**
     * A shared static method for all button widgets.
     *
     * @param widgetModel
     * @param isSelected
     * @return
     */
    public static List<AbstractWidgetAction> getHookedActionsForButton(ActionButtonModel widgetModel,
            boolean isSelected) {
        int actionIndex;

        if (widgetModel.isToggleButton()) {
            if (isSelected) {
                actionIndex = widgetModel.getActionIndex();
            } else {
                actionIndex = widgetModel.getReleasedActionIndex();
            }
        } else {
            actionIndex = widgetModel.getActionIndex();
        }

        var actionsInput = widgetModel.getActionsInput();
        if (actionsInput.getActionsList().size() <= 0) {
            return null;
        }
        if (actionsInput.isHookUpAllActionsToWidget()) {
            return actionsInput.getActionsList();
        }

        if (actionIndex >= 0 && actionsInput.getActionsList().size() > actionIndex) {
            return widgetModel.getActionsInput().getActionsList().subList(actionIndex, actionIndex + 1);
        }

        if (actionIndex == -1) {
            return actionsInput.getActionsList();
        }

        return null;
    }

    @Override
    public ActionButtonModel getWidgetModel() {
        return (ActionButtonModel) getModel();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        delegate.deactivate();
    }

    @Override
    protected void registerPropertyChangeHandlers() {

        PropertyChangeListener styleListener = evt -> {
            AbstractWidgetModel model = getWidgetModel();
            var descriptor = WidgetsService.getInstance().getWidgetDescriptor(model.getTypeID());
            var type = descriptor == null ? model.getTypeID().substring(model.getTypeID().lastIndexOf(".") + 1)
                    : descriptor.getName();
            model.setPropertyValue(AbstractWidgetModel.PROP_WIDGET_TYPE, type);
            var parent = model.getParent();
            parent.removeChild(model);
            parent.addChild(model);
            parent.selectWidget(model, true);
        };
        getWidgetModel().getProperty(ActionButtonModel.PROP_STYLE).addPropertyChangeListener(styleListener);
        updatePropSheet();

        delegate.registerPropertyChangeHandlers();
    }

    protected void updatePropSheet() {
        var isToggle = getWidgetModel().isToggleButton();
        getWidgetModel().setPropertyVisible(ActionButtonModel.PROP_RELEASED_ACTION_INDEX, isToggle);
        getWidgetModel().setPropertyDescription(ActionButtonModel.PROP_ACTION_INDEX,
                isToggle ? "Push Action Index" : "Click Action Index");
        var isDraw2DButton = delegate instanceof Draw2DButtonEditPartDelegate;
        getWidgetModel().setPropertyVisible(AbstractWidgetModel.PROP_COLOR_BACKGROUND, isDraw2DButton);
        getWidgetModel().setPropertyVisible(ActionButtonModel.PROP_BACKCOLOR_ALARMSENSITIVE, isDraw2DButton);
        getWidgetModel().setPropertyVisible(ActionButtonModel.PROP_ALARM_PULSING, isDraw2DButton);
    }

    @Override
    public void setValue(Object value) {
        delegate.setValue(value);
    }

    @Override
    public Object getValue() {
        return delegate.getValue();
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
        if (key == ITextFigure.class) {
            return getFigure();
        }

        return super.getAdapter(key);
    }
}
