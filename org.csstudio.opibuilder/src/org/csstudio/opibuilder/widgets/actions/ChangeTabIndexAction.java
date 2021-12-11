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
import org.csstudio.opibuilder.widgets.editparts.TabEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;

/**
 * Duplicate the current tab.
 */
public class ChangeTabIndexAction extends AbstractWidgetTargetAction {

    @Override
    public void run(IAction action) {
        var activeTabIndex = getSelectedTabWidget().getActiveTabIndex();
        var newIndexDialog = new InputDialog(null, "Change Tab Index", "New Index", "" + activeTabIndex,
                new IInputValidator() {

                    @Override
                    public String isValid(String newText) {
                        try {
                            var newIndex = Integer.parseInt(newText);
                            var itemCount = getSelectedTabWidget().getTabItemCount();
                            if (newIndex < 0 || newIndex >= itemCount) {
                                return NLS.bind("Invalid Tab Index! It must be between [0, {0}]", itemCount - 1);
                            }
                        } catch (Exception e) {
                            return "It must be an integer!";
                        }
                        return null;
                    }
                });
        if (newIndexDialog.open() == Window.OK) {
            var newIndex = Integer.parseInt(newIndexDialog.getValue());
            if (newIndex != activeTabIndex) {
                Command command = new ChangeTabIndexCommand(getSelectedTabWidget(), newIndex);
                execute(command);

            }
        }

    }

    /**
     * Gets the widget models of all currently selected EditParts.
     *
     * @return a list with all widget models that are currently selected
     */
    protected final TabEditPart getSelectedTabWidget() {
        return (TabEditPart) selection.getFirstElement();
    }

}
