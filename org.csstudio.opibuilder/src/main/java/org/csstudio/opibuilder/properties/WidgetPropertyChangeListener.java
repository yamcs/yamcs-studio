/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.datadefinition.WidgetIgnorableUITask;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.util.GUIRefreshThread;

/**
 * The listener on widget property change.
 */
public class WidgetPropertyChangeListener implements PropertyChangeListener {

    private AbstractBaseEditPart editpart;
    private AbstractWidgetProperty<?> widgetProperty;
    private List<IWidgetPropertyChangeHandler> handlers;

    /**
     * Constructor.
     *
     * @param editpart
     *            backlint to the editpart, which uses this listener.
     */
    public WidgetPropertyChangeListener(AbstractBaseEditPart editpart, AbstractWidgetProperty<?> property) {
        this.editpart = editpart;
        widgetProperty = property;
        handlers = new ArrayList<>();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Runnable runnable = new Runnable() {
            @Override
            public synchronized void run() {
                if (editpart == null || !editpart.isActive()) {
                    return;
                }
                for (var h : handlers) {
                    var figure = editpart.getFigure();
                    h.handleChange(evt.getOldValue(), evt.getNewValue(), figure);

                }
            }
        };
        var display = editpart.getViewer().getControl().getDisplay();
        var task = new WidgetIgnorableUITask(widgetProperty, runnable, display);

        GUIRefreshThread.getInstance(editpart.getExecutionMode() == ExecutionMode.RUN_MODE).addIgnorableTask(task);
    }

    /**
     * Add handler, which is informed when a property changed.
     */
    public void addHandler(IWidgetPropertyChangeHandler handler) {
        assert handler != null;
        handlers.add(handler);
    }

    public void removeAllHandlers() {
        handlers.clear();
    }
}
