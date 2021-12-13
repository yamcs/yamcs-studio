/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.script;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.eclipse.osgi.util.NLS;
import org.yamcs.studio.data.IPV;

/**
 * The center service for script execution.
 */
public class ScriptService {
    public enum ScriptType {
        JAVASCRIPT("JavaScript"), PYTHON("Python/Jython Script");

        ScriptType(String description) {
            this.description = description;
        }

        private String description;

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues() {
            var sv = new String[values().length];
            var i = 0;
            for (var p : values()) {
                sv[i++] = p.toString();
            }
            return sv;
        }
    }

    public static final String PVS = "pvs";

    public static final String WIDGET = "widget";

    public static final String DISPLAY = "display";

    public static final String PV_ARRAY_DEPRECIATED = "pvArray";

    public static final String TRIGGER_PV = "triggerPV";

    public static final String WIDGET_CONTROLLER_DEPRECIATED = "widgetController";

    public static final String JS = "js";

    public static final String PY = "py";

    private static ScriptService instance;

    private Map<ScriptData, IScriptStore> scriptMap;

    private ScriptService() {
        scriptMap = new HashMap<>();
    }

    public synchronized static ScriptService getInstance() {
        if (instance == null) {
            instance = new ScriptService();
        }
        return instance;
    }

    /**
     * Register the script in the script service, so that it could be executed afterwards.
     */
    public void registerScript(ScriptData scriptData, AbstractBaseEditPart editpart, IPV[] pvArray) {
        // UIBundlingThread.getInstance().addRunnable(new Runnable(){
        // public void run() {
        try {
            scriptMap.put(scriptData, ScriptStoreFactory.getScriptStore(scriptData, editpart, pvArray));
        } catch (Exception e) {
            var name = scriptData instanceof RuleScriptData ? ((RuleScriptData) scriptData).getRuleData().getName()
                    : scriptData.getPath().toString();
            var errorInfo = NLS.bind("Failed to register {0}. \n{1}", name, e);
            OPIBuilderPlugin.getLogger().log(Level.WARNING, errorInfo, e);
        }
        // }
        // });
    }

    public void unRegisterScript(ScriptData scriptData) {
        if (scriptMap.get(scriptData) != null) {
            scriptMap.get(scriptData).unRegister();
        }
        scriptMap.remove(scriptData);
    }
}
