/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgetActions;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.csstudio.opibuilder.script.ScriptService;
import org.csstudio.opibuilder.script.ScriptStoreFactory;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgetActions.WidgetActionFactory.ActionType;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.swt.widgets.Display;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * The action executing python script.
 * 
 * @author Xihui Chen
 *
 */
public class ExecutePythonScriptAction extends AbstractExecuteScriptAction {

    private PyCode code;
    private PythonInterpreter interpreter;
    private PySystemState state;
    private DisplayEditpart displayEditpart;
    private AbstractBaseEditPart widgetEditPart;

    @Override
    public ActionType getActionType() {
        return ActionType.EXECUTE_PYTHONSCRIPT;
    }

    @Override
    public void run() {
        if (code == null) {
            try {
                ScriptStoreFactory.initPythonInterpreter();
            } catch (Exception e) {
                final String message = "Failed to initialize PythonInterpreter";
                OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
            }
            // read file
            IPath absolutePath = getAbsolutePath();
            state = new PySystemState();

            // Add the path of script to python module search path
            if (!isEmbedded() && absolutePath != null && !absolutePath.isEmpty()) {
                // If it is a workspace file.
                if (ResourceUtil.isExistingWorkspaceFile(absolutePath)) {
                    IPath folderPath = absolutePath.removeLastSegments(1);
                    String sysLocation = ResourceUtil.workspacePathToSysPath(folderPath).toOSString();
                    state.path.append(new PyString(sysLocation));
                } else if (ResourceUtil.isExistingLocalFile(absolutePath)) {
                    IPath folderPath = absolutePath.removeLastSegments(1);
                    state.path.append(new PyString(folderPath.toOSString()));
                }
            }

            interpreter = new PythonInterpreter(null, state);

            GraphicalViewer viewer = getWidgetModel().getRootDisplayModel().getViewer();
            if (viewer != null) {
                Object obj = viewer.getEditPartRegistry().get(getWidgetModel());
                if (obj != null && obj instanceof AbstractBaseEditPart) {
                    displayEditpart = (DisplayEditpart) (viewer.getContents());
                    widgetEditPart = (AbstractBaseEditPart) obj;
                }
            }
        }

        Job job = new Job("Execute Python Script") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                String taskName = isEmbedded() ? "Execute Python Script" : "Connecting to " + getAbsolutePath();
                monitor.beginTask(taskName,
                        IProgressMonitor.UNKNOWN);
                runTask();
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }

    public void runTask() {
        Display display = getWidgetModel().getRootDisplayModel().getViewer().getControl().getDisplay();

        try {
            if (code == null) {

                // compile
                if (isEmbedded()) {
                    code = interpreter.compile(getScriptText());
                } else {
                    InputStream inputStream = getInputStream();
                    code = interpreter.compile(new InputStreamReader(inputStream));
                    inputStream.close();
                }
            }

            UIBundlingThread.getInstance().addRunnable(display, () -> {

                try {
                    interpreter.set(ScriptService.WIDGET, widgetEditPart);
                    interpreter.set(ScriptService.DISPLAY, displayEditpart);
                    interpreter.exec(code);
                } catch (Exception e) {
                    final String message = "Error exists in script " + getPath();
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
                }
            });
        } catch (Exception e) {
            final String message = "Failed to execute Python Script: " + getPath();
            OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
        }
    }

    @Override
    protected String getFileExtension() {
        return ScriptService.PY;
    }

    @Override
    protected String getScriptHeader() {
        return ScriptService.DEFAULT_PYTHONSCRIPT_HEADER;
    }

    @Override
    public void dispose() {
        if (interpreter != null) {
            PyObject o = interpreter.getLocals();
            if (o != null && o instanceof PyStringMap) {
                ((PyStringMap) o).clear();
            }
            o = state.getDict();
            if (o != null && o instanceof PyStringMap) {
                ((PyStringMap) o).clear();
            }
            state.close();
            state.cleanup();
            interpreter.close();
            interpreter.cleanup();
            interpreter = null;
            state = null;
        }
        super.dispose();
    }

}
