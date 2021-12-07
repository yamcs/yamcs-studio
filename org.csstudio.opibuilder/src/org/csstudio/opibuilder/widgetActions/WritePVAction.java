/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import org.csstudio.opibuilder.editparts.IPVWidgetEditpart;
import org.csstudio.opibuilder.model.IPVWidgetModel;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.PVNameProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.scriptUtil.GUIUtil;
import org.csstudio.opibuilder.util.BOYPVFactory;
import org.csstudio.opibuilder.util.DisplayUtils;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * An actions writing value to a PV.
 */
public class WritePVAction extends AbstractWidgetAction {

    public static final String PROP_PVNAME = "pv_name";
    public static final String PROP_VALUE = "value";
    public static final String PROP_TIMEOUT = "timeout";
    public static final String PROP_CONFIRM_MESSAGE = "confirm_message";
    private Display display;

    @Override
    protected void configureProperties() {
        addProperty(new PVNameProperty(PROP_PVNAME, "PV Name", WidgetPropertyCategory.Basic, "$(pv_name)"));
        addProperty(new StringProperty(PROP_VALUE, "Value", WidgetPropertyCategory.Basic, ""));
        addProperty(new IntegerProperty(PROP_TIMEOUT, "Timeout (second)", WidgetPropertyCategory.Basic, 10, 1, 3600));
        addProperty(new StringProperty(PROP_CONFIRM_MESSAGE, "Confirm Message", WidgetPropertyCategory.Basic, ""));
    }

    @Override
    public ActionType getActionType() {
        return ActionType.WRITE_PV;
    }

    public String getPVName() {
        return (String) getPropertyValue(PROP_PVNAME);
    }

    public String getValue() {
        return (String) getPropertyValue(PROP_VALUE);
    }

    public int getTimeout() {
        return (Integer) getPropertyValue(PROP_TIMEOUT);
    }

    public String getConfirmMessage() {
        return (String) getPropertyValue(PROP_CONFIRM_MESSAGE);
    }

    @Override
    public void run() {
        display = null;
        if (getWidgetModel() != null) {
            display = getWidgetModel().getRootDisplayModel().getViewer().getControl().getDisplay();
        } else {
            display = DisplayUtils.getDisplay();
        }

        if (!getConfirmMessage().isEmpty()) {
            if (!GUIUtil.openConfirmDialog(
                    "PV Name: " + getPVName() + "\nNew Value: " + getValue() + "\n\n" + getConfirmMessage())) {
                return;
            }
        }

        // If it has the same nave as widget PV name, use it.
        if (getWidgetModel() instanceof IPVWidgetModel) {
            var mainPVName = ((IPVWidgetModel) getWidgetModel()).getPVName();
            if (getPVName().equals(mainPVName)) {
                var o = getWidgetModel().getRootDisplayModel().getViewer().getEditPartRegistry().get(getWidgetModel());
                if (o instanceof IPVWidgetEditpart) {
                    ((IPVWidgetEditpart) o).setPVValue(IPVWidgetModel.PROP_PVNAME, getValue().trim());
                    return;
                }
            }
        }

        Job job = new Job(getDescription()) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                return writePVInSync();
            }

        };

        job.schedule();
    }

    private IStatus writePVInSync() {
        var text = getValue().trim();
        try {
            var pv = BOYPVFactory.createPV(getPVName());
            pv.start();
            try {
                if (!pv.setValue(text, getTimeout() * 1000)) {
                    throw new Exception("Write Failed!");
                }
            } finally {
                pv.stop();
            }
        } catch (Exception e) {
            popErrorDialog(e);
            return Status.CANCEL_STATUS;
        }
        return Status.OK_STATUS;

    }

    private void popErrorDialog(Exception e) {
        UIBundlingThread.getInstance().addRunnable(display, () -> {
            var message = "Failed to write PV:" + getPVName() + "\n"
                    + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            ErrorHandlerUtil.handleError(message, e, true, true);
            // ConsoleService.getInstance().writeError(message);
        });
    }

    @Override
    public String getDefaultDescription() {
        return "Write " + getValue() + " to " + getPVName();
    }
}
