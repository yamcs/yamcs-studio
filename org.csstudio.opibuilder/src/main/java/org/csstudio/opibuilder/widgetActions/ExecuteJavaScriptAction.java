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

import java.io.IOException;
import java.util.logging.Level;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.properties.AbstractWidgetProperty;
import org.csstudio.opibuilder.properties.EmbeddedJavaScriptProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.csstudio.opibuilder.script.JavaScriptStore;
import org.csstudio.opibuilder.script.ScriptService;
import org.csstudio.opibuilder.script.ScriptStoreFactory;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The action executing javascript with the default javascript engine embedded in JDK.
 */
public class ExecuteJavaScriptAction extends AbstractExecuteScriptAction {

    private ScriptEngine scriptEngine;
    private Bindings scriptScope;
    private CompiledScript script;

    @Override
    public ActionType getActionType() {
        return ActionType.EXECUTE_JAVASCRIPT;
    }

    @Override
    protected AbstractWidgetProperty<?> createEmbeddedScriptProperty(String prop_id, String description,
            WidgetPropertyCategory category, String defaultValue) {
        return new EmbeddedJavaScriptProperty(prop_id, description, category, defaultValue);
    }

    @Override
    public void run() {
        if (scriptEngine == null) {
            try {
                scriptEngine = ScriptStoreFactory.getJavaScriptEngine();
            } catch (Exception exception) {
                ErrorHandlerUtil.handleError("Failed to get Script Context", exception);
                return;
            }
            scriptScope = scriptEngine.createBindings();
            try {
                JavaScriptStore.bootstrapScriptEngine(scriptEngine, scriptScope);
            } catch (ScriptException | IOException e) {
                ErrorHandlerUtil.handleError("Failed to bootstrap script engine", e);
                return;
            }

            var viewer = getWidgetModel().getRootDisplayModel().getViewer();
            if (viewer != null) {
                var obj = viewer.getEditPartRegistry().get(getWidgetModel());
                if (obj != null && obj instanceof AbstractBaseEditPart) {
                    scriptScope.put(ScriptService.DISPLAY, viewer.getContents());
                    scriptScope.put(ScriptService.WIDGET, obj);
                }
            }
        }
        Job job = new Job("Execute JavaScript") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                var taskName = isEmbedded() ? "Execute JavaScript" : "Connecting to " + getAbsolutePath();
                monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
                runTask();
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }

    private void runTask() {
        var display = getWidgetModel().getRootDisplayModel().getViewer().getControl().getDisplay();

        try {
            if (script == null) {
                UIBundlingThread.getInstance().addRunnable(display, () -> {
                    try {
                        if (isEmbedded()) {
                            script = ((Compilable) scriptEngine).compile(getScriptText());
                        } else {
                            var reader = getReader();
                            var buf = new StringBuilder();
                            try {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    buf.append(line).append("\n");
                                }
                            } finally {
                                closeReader();
                            }
                            script = ((Compilable) scriptEngine).compile(buf.toString());
                        }
                    } catch (Exception e) {
                        var message = "Failed to compile JavaScript: " + getAbsolutePath();
                        OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
                    }
                });
            }

            UIBundlingThread.getInstance().addRunnable(display, () -> {
                try {
                    script.eval(scriptScope);
                } catch (Exception e) {
                    var message = "Error in script " + getAbsolutePath();
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
                }
            });
        } catch (Exception e) {
            var message = "Failed to execute JavaScript: " + getAbsolutePath();
            OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
        }
    }

    @Override
    protected String getFileExtension() {
        return ScriptService.JS;
    }
}
