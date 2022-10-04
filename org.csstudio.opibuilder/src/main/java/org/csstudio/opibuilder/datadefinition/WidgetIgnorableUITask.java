/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.datadefinition;

import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.csstudio.opibuilder.util.GUIRefreshThread;
import org.eclipse.swt.widgets.Display;

/**
 * The element in the {@link GUIRefreshThread}'s task queue. An existing task in the queue should be ignored when a new
 * task arrives that has the same identifyObject. For example, multiple tasks calling Gauge.setValue() are ignorable
 * since the widget only needs to display the latest value.
 */
public class WidgetIgnorableUITask {

    /**
     * The widget property.
     */
    final private Object identifyObject;

    /**
     * The task to be executed.
     */
    final private Runnable runnableTask;

    final private Display display;

    /**
     * Constructor.
     *
     * @param identifyObject
     *            the object that identifies this task. If the task associated with the same identifyObject has not been
     *            executed, it will be ignored.
     * @param runnableTask
     *            the task to be executed.
     * @param display
     *            Associated Display.
     */
    public WidgetIgnorableUITask(Object identifyObject, Runnable runnableTask, Display display) {
        this.identifyObject = identifyObject;
        this.runnableTask = runnableTask;
        this.display = display;
    }

    public Display getDisplay() {
        return display;
    }

    /**
     * @return the identify object
     */
    public Object getIdentifyObject() {
        return identifyObject;
    }

    /**
     * @return the runnableTask
     */
    public Runnable getRunnableTask() {
        return runnableTask;
    }

    /**
     * @param obj
     *            Possible other {@link WidgetIgnorableUITask}
     * @return <code>true</code> if other {@link WidgetIgnorableUITask} refers to the same
     *         {@link AbstractWidgetProperty}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WidgetIgnorableUITask) {
            return identifyObject == ((WidgetIgnorableUITask) obj).getIdentifyObject();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return identifyObject.toString();
    }

    @Override
    public int hashCode() {
        return identifyObject.hashCode();
    }
}
