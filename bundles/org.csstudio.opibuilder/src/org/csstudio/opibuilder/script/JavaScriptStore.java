package org.csstudio.opibuilder.script;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.simplepv.IPV;

/**
 * This is the implementation of {@link AbstractScriptStore} for the default javascript script engine embedded in Java.
 * (Nashorn since Java 8).
 */
public class JavaScriptStore extends AbstractScriptStore {

    // Adds support for importPackage() to Nashorn scripts.
    // This is an old function that existed back in the Rhino days
    private static final String COMPAT_PREFIX = "load(\"nashorn:mozilla_compat.js\");\n";

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
    }

    @Override
    protected void compileString(String string) throws Exception {
        script = ((Compilable) engine).compile(COMPAT_PREFIX + string);
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
        script = ((Compilable) engine).compile(COMPAT_PREFIX + content);
    }

    @Override
    protected void execScript(IPV triggerPV) throws Exception {
        bindings.put(ScriptService.TRIGGER_PV, triggerPV);
        script.eval(bindings);
    }
}
