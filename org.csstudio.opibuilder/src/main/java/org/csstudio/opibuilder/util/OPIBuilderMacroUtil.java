/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.Map;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.preferences.PreferencesHelper;

/**
 * Selfdefined MacroUtil for opibuilder.
 */
public class OPIBuilderMacroUtil {
    public static final String DNAME = "DNAME";
    public static final String DID = "DID";
    public static final String DLOC = "DLOC";

    /**
     * Replace the macros in the input with the real value. Simply calls the three argument version below
     *
     * @param input
     *            the raw string which include the macros string $(macro)
     * @return the string in which the macros have been replaced with the real value.
     */
    public static String replaceMacros(AbstractWidgetModel widgetModel, String input) {

        try {
            return MacroUtil.replaceMacros(input, new WidgetMacroTableProvider(widgetModel));
        } catch (InfiniteLoopException e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, e.getMessage(), e);
            return input;
        }
    }

    /**
     * @param widgetModel
     * @return the predefined macro map of the widget. This is the intrinsic map from the widget. Be careful to change
     *         the map contents.
     */
    public static Map<String, String> getWidgetMacroMap(AbstractWidgetModel widgetModel) {
        Map<String, String> macroMap;
        if (widgetModel instanceof AbstractContainerModel) {
            macroMap = ((AbstractContainerModel) widgetModel).getMacroMap();
        } else {
            if (widgetModel.getParent() != null) {
                macroMap = widgetModel.getParent().getMacroMap();
            } else {
                macroMap = PreferencesHelper.getMacros();
            }
        }
        return macroMap;
    }
}

/**
 * Customized macrotable provider.
 */
class WidgetMacroTableProvider implements IMacroTableProvider {
    private AbstractWidgetModel widgetModel;
    private Map<String, String> macroMap;

    public WidgetMacroTableProvider(AbstractWidgetModel widgetModel) {
        this.widgetModel = widgetModel;
        macroMap = OPIBuilderMacroUtil.getWidgetMacroMap(widgetModel);
    }

    @Override
    public String getMacroValue(String macroName) {
        if (macroMap != null && macroMap.containsKey(macroName)) {
            return macroMap.get(macroName);
        } else if (widgetModel.getAllPropertyIDs().contains(macroName)) {
            var propertyValue = widgetModel.getRawPropertyValue(macroName);
            if (propertyValue != null) {
                return propertyValue.toString();
            }
        }
        if (macroName.equals(OPIBuilderMacroUtil.DID)) {
            return OPIBuilderMacroUtil.DID + "_" + widgetModel.getRootDisplayModel().getDisplayID();
        } else if (macroName.equals(OPIBuilderMacroUtil.DNAME)) {
            return widgetModel.getRootDisplayModel().getName();
        } else if (macroName.equals(OPIBuilderMacroUtil.DLOC)) {
            var uri = ResourceUtil.workspacePathToSysPath(widgetModel.getRootDisplayModel().getOpiFilePath()).toFile()
                    .getParentFile().toURI().toString();
            // Fix the file protocol: we need 'file:///' for absolute paths
            if (uri.matches("file:/[^/].*")) {
                uri = "file:///" + uri.substring(6);
            }
            return uri;
        }

        return null;
    }
}
