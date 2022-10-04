/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import java.util.LinkedList;

import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.ActionsProperty;

/**
 * The value type definition for {@link ActionsProperty}, which describes the input for an actions Property.
 */
public class ActionsInput {

    private LinkedList<AbstractWidgetAction> actionsList;

    private boolean hookUpFirstActionToWidget = false;

    private boolean hookUpAllActionsToWidget = false;

    private AbstractWidgetModel widgetModel;

    public ActionsInput(LinkedList<AbstractWidgetAction> actionsList) {
        this.actionsList = actionsList;
    }

    public ActionsInput() {
        this(new LinkedList<AbstractWidgetAction>());
    }

    /**
     * @return the scriptList
     */
    public LinkedList<AbstractWidgetAction> getActionsList() {
        return actionsList;
    }

    public void addAction(AbstractWidgetAction action) {
        actionsList.add(action);
        action.setWidgetModel(widgetModel);
    }

    /**
     * @return a total contents copy of this ScriptsInput.
     */
    public ActionsInput getCopy() {
        var copy = new ActionsInput();
        for (var data : actionsList) {
            copy.getActionsList().add(data.getCopy());
        }
        copy.setWidgetModel(widgetModel);
        copy.setHookUpFirstActionToWidget(hookUpFirstActionToWidget);
        copy.setHookUpAllActionsToWidget(hookUpAllActionsToWidget);
        return copy;
    }

    /**
     * @param hookWithWidget
     *            the hookWithWidget to set
     */
    public void setHookUpFirstActionToWidget(boolean hookWithWidget) {
        hookUpFirstActionToWidget = hookWithWidget;
    }

    /**
     * @return the hookWithWidget true if the first action is hooked with the widget's click, which means click on the
     *         widget will activate the first action in the list.
     */
    public boolean isFirstActionHookedUpToWidget() {
        return hookUpFirstActionToWidget;
    }

    @Override
    public String toString() {
        if (actionsList.size() == 0) {
            return "no action";
        }
        if (actionsList.size() == 1) {
            return actionsList.get(0).getDescription();
        }
        return actionsList.size() + " actions";
    }

    /**
     * @param widgetModel
     *            the widgetModel to set
     */
    public void setWidgetModel(AbstractWidgetModel widgetModel) {
        this.widgetModel = widgetModel;
        for (var action : actionsList) {
            action.setWidgetModel(widgetModel);
        }
    }

    /**
     * @return the widgetModel
     */
    public AbstractWidgetModel getWidgetModel() {
        return widgetModel;
    }

    public boolean isHookUpAllActionsToWidget() {
        return hookUpAllActionsToWidget;
    }

    public void setHookUpAllActionsToWidget(boolean hookUpAllActionsToWidget) {
        this.hookUpAllActionsToWidget = hookUpAllActionsToWidget;
    }
}
