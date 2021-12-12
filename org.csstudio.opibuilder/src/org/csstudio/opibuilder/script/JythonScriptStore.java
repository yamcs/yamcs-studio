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

import java.io.InputStream;
import java.io.InputStreamReader;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.yamcs.studio.data.IPV;

/**
 * This is the implementation of {@link AbstractScriptStore} for Jython PythonInterpreter.
 */
public class JythonScriptStore extends AbstractScriptStore {

    private PythonInterpreter interp;
    private PySystemState state;

    private PyCode code;

    public JythonScriptStore(ScriptData scriptData, AbstractBaseEditPart editpart, IPV[] pvArray) throws Exception {
        super(scriptData, editpart, pvArray);
    }

    @Override
    protected void initScriptEngine() {
        var scriptPath = getAbsoluteScriptPath();
        // Add the path of script to python module search path
        state = Py.getSystemState();
        if (scriptPath != null && !scriptPath.isEmpty()) {

            // If it is a workspace file.
            if (ResourceUtil.isExistingWorkspaceFile(scriptPath)) {
                var folderPath = scriptPath.removeLastSegments(1);
                var sysLocation = ResourceUtil.workspacePathToSysPath(folderPath).toOSString();
                state.path.append(new PyString(sysLocation));
            } else if (ResourceUtil.isExistingLocalFile(scriptPath)) {
                var folderPath = scriptPath.removeLastSegments(1);
                state.path.append(new PyString(folderPath.toOSString()));
            }
        }
        interp = PythonInterpreter.threadLocalStateInterpreter(state.getDict());
        bootstrapInterpreter(interp);
    }

    public static void bootstrapInterpreter(PythonInterpreter interpreter) {
        // Auto-import standard utility libraries
        var buf = new StringBuilder();
        for (var lib : SCRIPT_LIBRARIES) {
            buf.append("from ")
                    .append(lib.getPackageName())
                    .append(" import ")
                    .append(lib.getSimpleName())
                    .append("\n");
        }
        interpreter.exec(buf.toString());
    }

    @Override
    protected void compileString(String string) throws Exception {
        code = interp.compile(string);
    }

    @Override
    protected void compileInputStream(InputStream s) throws Exception {
        code = interp.compile(new InputStreamReader(s));
    }

    @Override
    protected void execScript(IPV triggerPV) throws Exception {
        interp.set(ScriptService.WIDGET, getEditPart());
        interp.set(ScriptService.PVS, getPvArray());
        interp.set(ScriptService.DISPLAY, getDisplayEditPart());
        interp.set(ScriptService.WIDGET_CONTROLLER_DEPRECIATED, getEditPart());
        interp.set(ScriptService.PV_ARRAY_DEPRECIATED, getPvArray());
        interp.set(ScriptService.TRIGGER_PV, triggerPV);
        interp.exec(code);
    }

    @Override
    protected void dispose() {
        if (interp != null) {
            var o = interp.getLocals();
            if (o != null && o instanceof PyStringMap) {
                ((PyStringMap) o).clear();
            }
            // o = state.getBuiltins();
            // if (o != null && o instanceof PyStringMap) {
            // ((PyStringMap)o).clear();
            // }
            o = state.getDict();
            if (o != null && o instanceof PyStringMap) {
                ((PyStringMap) o).clear();
            }
            state.close();
            state.cleanup();
            interp.close();
            interp.cleanup();
            interp = null;
            state = null;
        }
        code = null;
        super.dispose();
    }
}
