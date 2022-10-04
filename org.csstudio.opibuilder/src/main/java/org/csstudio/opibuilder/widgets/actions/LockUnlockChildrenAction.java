/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.widgets.actions;

import org.csstudio.opibuilder.actions.AbstractWidgetTargetAction;
import org.csstudio.opibuilder.commands.SetWidgetPropertyCommand;
import org.csstudio.opibuilder.datadefinition.WidgetIgnorableUITask;
import org.csstudio.opibuilder.util.GUIRefreshThread;
import org.csstudio.opibuilder.widgets.editparts.GroupingContainerEditPart;
import org.csstudio.opibuilder.widgets.model.GroupingContainerModel;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;

/**
 * The action to lock or unlock children in grouping container.
 */
public class LockUnlockChildrenAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {

        var containerModel = getSelectedContainer();

        var cmd = createLockUnlockCommand(containerModel);
        execute(cmd);
    }

    public static Command createLockUnlockCommand(GroupingContainerModel containerModel) {
        Command cmd = new SetWidgetPropertyCommand(containerModel, GroupingContainerModel.PROP_LOCK_CHILDREN,
                !containerModel.isLocked()) {
            @Override
            public void execute() {
                super.execute();
                selectWidgets();
            }

            @Override
            public void undo() {
                super.undo();
                selectWidgets();
            }

            private void selectWidgets() {
                // must be queued so it is executed after property has been changed.
                GUIRefreshThread.getInstance(false).addIgnorableTask(new WidgetIgnorableUITask(this,
                        () -> containerModel.getParent().selectWidget(containerModel, false), Display.getCurrent()));

            }
        };
        cmd.setLabel(containerModel.isLocked() ? "Unlock Children" : "Lock Children");
        return cmd;
    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final GroupingContainerModel getSelectedContainer() {
        return ((GroupingContainerEditPart) selection.getFirstElement()).getWidgetModel();
    }
}
