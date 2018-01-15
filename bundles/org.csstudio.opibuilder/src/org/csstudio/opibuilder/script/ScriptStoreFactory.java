package org.csstudio.opibuilder.script;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.script.ScriptService.ScriptType;
import org.csstudio.simplepv.IPV;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.widgets.Display;
import org.mozilla.javascript.Context;

/**
 * The factory to return the corresponding script store according to the script type.
 * 
 * @author Xihui Chen
 *
 *         TODO Cleanup This has getJavaScriptEngine() and getRhinoContext(). Callers invoke one or the other depending
 *         on defaultJsEngine = ..RHINO or JDK.
 *
 *         A true factory should just have one "getJava..()" call which internally decides to return the Rhino from
 *         lib/, or the one bundled in Java 7, or Nashorn from Java 8. After fixing the Java 7 Rhino issues
 *         (https://github.com/ControlSystemStudio/cs-studio/issues/723), remove lib/rhino/js.jar
 */
public class ScriptStoreFactory {

    public static enum JavaScriptEngine {
        RHINO, JDK;
    }

    final private static JavaScriptEngine defaultJsEngine;

    private static Map<Display, Context> displayContextMap = new HashMap<>();
    private static Map<Display, ScriptEngine> displayScriptEngineMap = new HashMap<>();

    static {
        String option = JavaScriptEngine.RHINO.name(); // Default
        final IPreferencesService service = Platform.getPreferencesService();
        if (service != null)
            option = service.getString(OPIBuilderPlugin.PLUGIN_ID, "java_script_engine", option, null);
        try {
            defaultJsEngine = JavaScriptEngine.valueOf(option);
        } catch (Throwable ex) { // Create more obvious exception
            throw new RuntimeException(
                    "Invalid preference setting " + OPIBuilderPlugin.PLUGIN_ID + "/java_script_engine=" + option);
        }
    }

    public static JavaScriptEngine getDefaultJavaScriptEngine() {
        return defaultJsEngine;
    }

    /**
     * Must be called in UI Thread.
     * 
     * @throws Exception
     */
    private static void initRhinoJSEngine() throws Exception {
        Context scriptContext = Context.enter();
        final Display display = Display.getCurrent();
        displayContextMap.put(display, scriptContext);
    }

    /**
     * Must be called in UI Thread.
     * 
     * @throws Exception
     */
    private static void initJdkJSEngine() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        final Display display = Display.getCurrent();
        displayScriptEngineMap.put(display, engine);
    }

    /**
     * This method must be called in UI Thread!
     * 
     * @param scriptData
     * @param editpart
     * @param pvArray
     * @return
     * @throws Exception
     */
    public static AbstractScriptStore getScriptStore(
            ScriptData scriptData, AbstractBaseEditPart editpart, IPV[] pvArray) throws Exception {
        if (!scriptData.isEmbedded() &&
                (scriptData.getPath() == null || scriptData.getPath().getFileExtension() == null)) {
            if (scriptData instanceof RuleScriptData) {
                return getJavaScriptStore(scriptData, editpart, pvArray);
            } else
                throw new RuntimeException("No Script Engine for this type of script");
        }
        String fileExt = ""; //$NON-NLS-1$
        if (scriptData.isEmbedded()) {
            if (scriptData.getScriptType() == ScriptType.JAVASCRIPT)
                fileExt = ScriptService.JS;
        } else
            fileExt = scriptData.getPath().getFileExtension().trim().toLowerCase();
        if (fileExt.equals(ScriptService.JS)) {
            return getJavaScriptStore(scriptData, editpart, pvArray);
        } else
            throw new RuntimeException("No Script Engine for this type of script");
    }

    private static AbstractScriptStore getJavaScriptStore(
            ScriptData scriptData, AbstractBaseEditPart editpart, IPV[] pvArray) throws Exception {
        if (defaultJsEngine == JavaScriptEngine.RHINO) {
            boolean rhinoJsEngineInitialized = displayContextMap.containsKey(Display.getCurrent());
            if (!rhinoJsEngineInitialized)
                initRhinoJSEngine();
            return new RhinoScriptStore(scriptData, editpart, pvArray);
        } else {
            boolean jdkJsEngineInitialized = displayScriptEngineMap.containsKey(Display.getCurrent());
            if (!jdkJsEngineInitialized) {
                initJdkJSEngine();
            }
            return new JavaScriptStore(scriptData, editpart, pvArray);
        }
    }

    /**
     * This method must be executed in UI Thread!
     * 
     * @return the Rhino script context.
     * @throws Exception
     *             on error, including invocation when not using <code>JavaScriptEngine.RHINO</code>
     */
    public static Context getRhinoContext() throws Exception {
        if (defaultJsEngine != JavaScriptEngine.RHINO)
            throw new RuntimeException("Fetching Rhino context while not using Rhino?");
        Display display = Display.getCurrent();
        boolean jsEngineInitialized = displayContextMap.containsKey(display);
        if (!jsEngineInitialized)
            initRhinoJSEngine();
        return displayContextMap.get(display);
    }

    public static void exit() {
        boolean jsEngineInitialized = displayContextMap.containsKey(Display.getCurrent());
        if (jsEngineInitialized)
            UIBundlingThread.getInstance().addRunnable(Display.getCurrent(), new Runnable() {
                @Override
                public void run() {
                    Context.exit();
                }
            });
    }

    /**
     * This method must be executed in UI Thread!
     * 
     * @return the JDK's Javascript script engine.
     * @throws Exception
     *             on error, including invocation when not using <code>JavaScriptEngine.JDK</code>
     */
    public static ScriptEngine getJavaScriptEngine() throws Exception {
        if (defaultJsEngine != JavaScriptEngine.JDK)
            throw new RuntimeException("Fetching JDK script engine context while not using JDK");
        Display display = Display.getCurrent();
        boolean jsEngineInitialized = displayScriptEngineMap.containsKey(display);
        if (!jsEngineInitialized)
            initJdkJSEngine();
        return displayScriptEngineMap.get(display);
    }

}
