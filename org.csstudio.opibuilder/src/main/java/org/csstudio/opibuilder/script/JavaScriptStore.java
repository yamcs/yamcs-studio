/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.yamcs.studio.data.IPV;

/**
 * This is the implementation of {@link AbstractScriptStore} using the Nashorn script engine.
 */
public class JavaScriptStore extends AbstractScriptStore {

    private ScriptEngine engine;
    private Bindings bindings;
    private CompiledScript script;

    public JavaScriptStore(ScriptData scriptData, AbstractBaseEditPart editpart, IPV[] pvArray) throws Exception {
        super(scriptData, editpart, pvArray);
    }

    @Override
    protected void initScriptEngine() throws Exception {
        engine = ScriptStoreFactory.getJavaScriptEngine();
        bindings = engine.createBindings();
        bindings.put(ScriptService.WIDGET, getEditPart());
        bindings.put(ScriptService.PVS, getPvArray());
        bindings.put(ScriptService.DISPLAY, getDisplayEditPart());
        bindings.put(ScriptService.WIDGET_CONTROLLER_DEPRECIATED, getEditPart());
        bindings.put(ScriptService.PV_ARRAY_DEPRECIATED, getPvArray());
        bootstrapScriptEngine(engine, bindings);
    }

    public static void bootstrapScriptEngine(ScriptEngine engine, Bindings bindings)
            throws IOException, ScriptException {
        var nashornBootstrap = "/org/csstudio/opibuilder/script/nashorn_bootstrap.js";
        try (var in = new InputStreamReader(JavaScriptStore.class.getResourceAsStream(nashornBootstrap))) {
            engine.eval(in, bindings);
        }

        // Auto-import standard utility libraries
        var buf = new StringBuilder();
        for (var lib : SCRIPT_LIBRARIES) {
            buf.append("var ")
                    .append(lib.getSimpleName())
                    .append(" = Java.type(\"")
                    .append(lib.getName())
                    .append("\");\n");
        }
        engine.eval(buf.toString(), bindings);
    }

    @Override
    protected void compileString(String string) throws Exception {
        script = ((Compilable) engine).compile(string);
    }

    @Override
    protected void compileInputStream(InputStream in) throws Exception {
        var bout = new ByteArrayOutputStream();
        try {
            var buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                bout.write(buffer, 0, length);
            }
        } finally {
            in.close();
        }

        var content = bout.toString(StandardCharsets.UTF_8.name());
        script = ((Compilable) engine).compile(content);
    }

    @Override
    protected void execScript(IPV triggerPV) throws Exception {
        bindings.put(ScriptService.TRIGGER_PV, triggerPV);
        script.eval(bindings);
    }
}
