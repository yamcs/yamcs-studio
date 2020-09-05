package org.csstudio.opibuilder.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.yamcs.studio.data.IPV;

/**
 * This is the implementation of {@link AbstractScriptStore} for the default javascript script engine embedded in Java.
 * (Nashorn since Java 8).
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
        String nashornBootstrap = "/org/csstudio/opibuilder/script/nashorn_bootstrap.js";
        try (Reader in = new InputStreamReader(JavaScriptStore.class.getResourceAsStream(nashornBootstrap))) {
            engine.eval(in, bindings);
        }
        engine.eval("importPackage(Packages.org.csstudio.opibuilder.scriptUtil);", bindings);
        engine.eval("importPackage(Packages.org.yamcs.studio.script);", bindings);
    }

    @Override
    protected void compileString(String string) throws Exception {
        script = ((Compilable) engine).compile(string);
    }

    @Override
    protected void compileInputStream(InputStream in) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                bout.write(buffer, 0, length);
            }
        } finally {
            in.close();
        }

        String content = bout.toString(StandardCharsets.UTF_8.name());
        script = ((Compilable) engine).compile(content);
    }

    @Override
    protected void execScript(IPV triggerPV) throws Exception {
        bindings.put(ScriptService.TRIGGER_PV, triggerPV);
        script.eval(bindings);
    }
}
