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

import java.util.LinkedHashMap;
import java.util.Map;

import org.csstudio.opibuilder.properties.CommandProperty;
import org.csstudio.opibuilder.properties.StringMapProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.yamcs.studio.script.GUIUtil;
import org.yamcs.studio.script.Yamcs;

/**
 * Action to run a Yamcs command
 */
public class RunCommandAction extends AbstractWidgetAction {

    public static final String PROP_COMMAND = "command";
    public static final String PROP_ARGS = "args";
    public static final String PROP_CONFIRM_MESSAGE = "confirm_message";

    @Override
    protected void configureProperties() {
        addProperty(new CommandProperty(PROP_COMMAND, "Command", WidgetPropertyCategory.Basic, ""));
        addProperty(new StringMapProperty(PROP_ARGS, "Arguments", WidgetPropertyCategory.Basic, null));
        addProperty(new StringProperty(PROP_CONFIRM_MESSAGE, "Confirm Message", WidgetPropertyCategory.Basic, ""));
    }

    @Override
    public ActionType getActionType() {
        return ActionType.RUN_COMMAND;
    }

    @Override
    public void run() {
        if (!getConfirmMessage().isEmpty()) {
            if (!GUIUtil.openConfirmDialog(
                    "Command: " + getCommand() + "\n\n" + getConfirmMessage())) {
                return;
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        var args = (Map<String, Object>) (Map) getArgs();
        Yamcs.issueCommand(getCommand(), args);
    }

    public String getCommand() {
        return (String) getPropertyValue(PROP_COMMAND);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getArgs() {
        var result = new LinkedHashMap<String, String>();

        var propertyValue = (Map<String, String>) getPropertyValue(PROP_ARGS);
        if (propertyValue != null) {
            result.putAll(propertyValue);
        }

        return result;
    }

    public String getConfirmMessage() {
        return (String) getPropertyValue(PROP_CONFIRM_MESSAGE);
    }

    @Override
    public String getDefaultDescription() {
        return "Run " + getCommand();
    }
}
