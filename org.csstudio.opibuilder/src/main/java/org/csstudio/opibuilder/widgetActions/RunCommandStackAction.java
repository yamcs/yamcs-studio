/*******************************************************************************
 * Copyright (c) 2023 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import org.csstudio.opibuilder.properties.FilePathProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.yamcs.studio.script.GUIUtil;
import org.yamcs.studio.script.Yamcs;

/**
 * Action to run a Yamcs Command Stack.
 */
public class RunCommandStackAction extends AbstractWidgetAction {

    public static final String PROP_PATH = "path";
    public static final String PROP_CONFIRM_MESSAGE = "confirm_message";

    @Override
    protected void configureProperties() {
        addProperty(new FilePathProperty(PROP_PATH, "File Path", WidgetPropertyCategory.Basic, "",
                new String[] { "ycs" }));
        addProperty(new StringProperty(PROP_CONFIRM_MESSAGE, "Confirm Message", WidgetPropertyCategory.Basic, ""));
    }

    @Override
    public ActionType getActionType() {
        return ActionType.RUN_COMMAND_STACK;
    }

    @Override
    public void run() {
        if (!getConfirmMessage().isEmpty()) {
            if (!GUIUtil.openConfirmDialog(
                    "Command Stack: " + getPath() + "\n\n" + getConfirmMessage())) {
                return;
            }
        }

        var absolutePath = getPath();
        if (!absolutePath.isAbsolute()) {
            absolutePath = ResourceUtil.buildAbsolutePath(getWidgetModel(), getPath());
        }
        if (absolutePath != null && ResourceUtil.isExsitingFile(absolutePath, true)) {
            Yamcs.runCommandStack(absolutePath.toString());
        }
    }

    protected IPath getPath() {
        var path = (String) getPropertyValue(PROP_PATH);
        return path != null ? Path.fromPortableString(path) : null;
    }

    public String getConfirmMessage() {
        return (String) getPropertyValue(PROP_CONFIRM_MESSAGE);
    }

    @Override
    public String getDefaultDescription() {
        return "Run " + getPath();
    }
}
