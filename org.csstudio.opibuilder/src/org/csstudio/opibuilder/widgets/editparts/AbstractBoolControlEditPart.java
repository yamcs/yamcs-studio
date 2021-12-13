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

import static org.csstudio.opibuilder.model.AbstractWidgetModel.PROP_ENABLED;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVNAME;
import static org.csstudio.opibuilder.model.IPVWidgetModel.PROP_PVVALUE;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolControlModel.PROP_CONFIRM_DIALOG;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolControlModel.PROP_CONFIRM_TIP;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolControlModel.PROP_PASSWORD;
import static org.csstudio.opibuilder.widgets.model.AbstractBoolControlModel.PROP_TOGGLE_BUTTON;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.widgets.model.AbstractBoolControlModel;
import org.csstudio.opibuilder.widgets.model.AbstractBoolWidgetModel;
import org.csstudio.swt.widgets.figures.AbstractBoolControlFigure;
import org.csstudio.swt.widgets.figures.AbstractBoolFigure;

/**
 * Base editPart controller for a widget based on {@link AbstractBoolControlModel}.
 */
public abstract class AbstractBoolControlEditPart extends AbstractBoolEditPart {

    /**
     * Sets those properties on the figure that are defined in the {@link AbstractBoolFigure} base class. This method is
     * provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link AbstractBaseEditPart#doCreateFigure()}.
     */
    protected void initializeCommonFigureProperties(AbstractBoolControlFigure figure, AbstractBoolControlModel model) {
        super.initializeCommonFigureProperties(figure, model);
        figure.setToggle(model.isToggleButton());
        figure.setShowConfirmDialog(model.getShowConfirmDialog());
        figure.setConfirmTip(model.getConfirmTip());
        figure.setPassword(model.getPassword());
        figure.setRunMode(getExecutionMode().equals(ExecutionMode.RUN_MODE));
        figure.addManualValueChangeListener(newValue -> {
            if (getExecutionMode() == ExecutionMode.RUN_MODE) {
                if (getWidgetModel().getDataType() == 0) {
                    setPVValue(PROP_PVNAME, newValue);
                } else {
                    setPVValue(PROP_PVNAME,
                            newValue <= 0.01 ? getWidgetModel().getOffState() : getWidgetModel().getOnState());
                }
            }
        });
        delegate.setUpdateSuppressTime(-1);
        markAsControlPV(PROP_PVNAME, PROP_PVVALUE);
    }

    /**
     * Registers property change handlers for the properties defined in {@link AbstractBoolWidgetModel}. This method is
     * provided for the convenience of subclasses, which can call this method in their implementation of
     * {@link #registerPropertyChangeHandlers()}.
     */
    @Override
    protected void registerCommonPropertyChangeHandlers() {
        configureButtonListener((AbstractBoolControlFigure) getFigure());

        super.registerCommonPropertyChangeHandlers();

        IWidgetPropertyChangeHandler toggleHandler = (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolControlFigure) refreshableFigure;
            figure.setToggle((Boolean) newValue);
            return true;
        };
        getWidgetModel().getProperty(PROP_TOGGLE_BUTTON).addPropertyChangeListener(
                evt -> toggleHandler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure()));

        setPropertyChangeHandler(PROP_CONFIRM_DIALOG,
                (oldValue, newValue, refreshableFigure) -> {
                    var figure = (AbstractBoolControlFigure) refreshableFigure;
                    figure.setShowConfirmDialog(getWidgetModel().getShowConfirmDialog());
                    return true;
                });

        setPropertyChangeHandler(PROP_CONFIRM_TIP, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolControlFigure) refreshableFigure;
            figure.setConfirmTip((String) newValue);
            return true;
        });

        setPropertyChangeHandler(PROP_PASSWORD, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolControlFigure) refreshableFigure;
            figure.setPassword((String) newValue);
            return true;
        });

        // enabled. WidgetBaseEditPart will force the widget as disabled in edit model,
        // which is not the case for the bool control widget
        setPropertyChangeHandler(PROP_ENABLED, (oldValue, newValue, refreshableFigure) -> {
            var figure = (AbstractBoolControlFigure) refreshableFigure;
            figure.setEnabled((Boolean) newValue);
            return true;
        });
    }

    @Override
    public AbstractBoolControlModel getWidgetModel() {
        return (AbstractBoolControlModel) getModel();
    }

    /**
     * Configures a listener for performing a {@link AbstractWidgetActionModel}.
     */
    private void configureButtonListener(AbstractBoolControlFigure figure) {
        figure.addManualValueChangeListener(newValue -> {
            // If the display is not in run mode, don't do anything.
            if (getExecutionMode() != ExecutionMode.RUN_MODE) {
                return;
            }

            int actionIndex;
            if (figure.getBooleanValue()) {
                actionIndex = getWidgetModel().getPushActionIndex();
            } else {
                actionIndex = getWidgetModel().getReleasedActionIndex();
            }

            if (actionIndex >= 0 && getWidgetModel().getActionsInput().getActionsList().size() > actionIndex) {
                getWidgetModel().getActionsInput().getActionsList().get(actionIndex).run();
            }
        });
    }
}
