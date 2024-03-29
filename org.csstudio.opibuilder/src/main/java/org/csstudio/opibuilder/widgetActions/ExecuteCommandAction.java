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

import java.util.logging.Level;
import java.util.regex.Pattern;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.StringProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;

/**
 * The action executing a system command.
 */
public class ExecuteCommandAction extends AbstractWidgetAction {

    private static final String OPI_DIR = "opi.dir";
    public final static String PROP_COMMAND = "command";
    public final static String PROP_DIRECTORY = "command_directory";
    public final static String PROP_WAIT_TIME = "wait_time";

    @Override
    protected void configureProperties() {
        addProperty(new StringProperty(PROP_COMMAND, "Command", WidgetPropertyCategory.Basic, ""));
        addProperty(new StringProperty(PROP_DIRECTORY, "Command Directory[path]", WidgetPropertyCategory.Basic,
                "$(user.home)"));
        addProperty(new IntegerProperty(PROP_WAIT_TIME, "Wait Time(s)", WidgetPropertyCategory.Basic, 10, 1,
                Integer.MAX_VALUE));
    }

    @Override
    public ActionType getActionType() {
        return ActionType.EXECUTE_CMD;
    }

    @Override
    public void run() {
        OPIBuilderPlugin.getLogger().info("Executing command: " + getCommand());
        new CommandExecutor(getCommand(), getDirectory(), getWaitTime());
    }

    public String getCommand() {
        return (String) getPropertyValue(PROP_COMMAND);
    }

    public String getDirectory() {
        var directory = (String) getPropertyValue(PROP_DIRECTORY);
        try {
            return replaceProperties(directory);
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return directory;
    }

    public int getWaitTime() {
        return (Integer) getPropertyValue(PROP_WAIT_TIME);
    }

    /**
     * @param value
     *            Value that might contain "$(prop)"
     * @return Value where "$(prop)" is replaced by Java system property "prop"
     * @throws Exception
     *             on error
     */
    private String replaceProperties(String value) throws Exception {
        var matcher = Pattern.compile("\\$\\((.*)\\)").matcher(value);
        if (matcher.matches()) {
            var prop_name = matcher.group(1);
            var prop = System.getProperty(prop_name);
            if (prop == null && prop_name.equals(OPI_DIR)) {
                var opiFilePath = getWidgetModel().getRootDisplayModel().getOpiFilePath();
                if (ResourceUtil.isExistingWorkspaceFile(opiFilePath)) {
                    opiFilePath = ResourceUtil.workspacePathToSysPath(opiFilePath);
                }
                prop = opiFilePath.removeLastSegments(1).toOSString();
            }

            if (prop == null) {
                throw new Exception("Property '" + prop_name + "' is not defined");
            }
            return prop;
        }
        // Return as is
        return value;
    }

    @Override
    public String getDefaultDescription() {
        return super.getDefaultDescription() + " " + getCommand();
    }
}
