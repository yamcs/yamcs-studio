/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.widgetActions.AbstractWidgetAction;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * An Action, which encapsulates a {@link AbstractWidgetAction}.
 */
public final class WidgetActionMenuAction extends Action {
    /**
     * The {@link AbstractWidgetActionModel}.
     */
    private AbstractWidgetAction _widgetAction;

    /**
     * Constructor.
     *
     * @param widgetAction
     *            The encapsulated {@link AbstractWidgetAction}
     */
    public WidgetActionMenuAction(AbstractWidgetAction widgetAction) {
        _widgetAction = widgetAction;
        this.setText(_widgetAction.getDescription());
        Object adapter = widgetAction.getAdapter(IWorkbenchAdapter.class);
        if (adapter != null && adapter instanceof IWorkbenchAdapter) {
            this.setImageDescriptor(((IWorkbenchAdapter) adapter).getImageDescriptor(widgetAction));
        }
        setEnabled(widgetAction.isEnabled());
    }

    @Override
    public void run() {
        _widgetAction.run();

    }
}
