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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.DisplayEditpart;
import org.csstudio.opibuilder.model.AbstractLinkingContainerModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.ui.util.thread.UIBundlingThread;
import org.eclipse.core.runtime.IPath;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.osgi.util.NLS;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.IPVListener;
import org.yamcs.studio.script.ColorFontUtil;
import org.yamcs.studio.script.ConsoleUtil;
import org.yamcs.studio.script.CssEntityResolver;
import org.yamcs.studio.script.DataUtil;
import org.yamcs.studio.script.FileUtil;
import org.yamcs.studio.script.GUIUtil;
import org.yamcs.studio.script.PVUtil;
import org.yamcs.studio.script.ScriptUtil;
import org.yamcs.studio.script.WidgetUtil;
import org.yamcs.studio.script.Yamcs;

/**
 * The script store help to store the compiled script for afterward executions. This is the abstract script store
 * implementation for BOY script execution. All script stores in BOY should implement this abstract class with a
 * specific script engine. The store must be disposed manually when it is not needed.
 */
public abstract class AbstractScriptStore implements IScriptStore {

    protected static final Class<?>[] SCRIPT_LIBRARIES = new Class<?>[] {
            ColorFontUtil.class,
            ConsoleUtil.class,
            CssEntityResolver.class,
            DataUtil.class,
            FileUtil.class,
            GUIUtil.class,
            PVUtil.class,
            ScriptUtil.class,
            WidgetUtil.class,
            Yamcs.class,
    };

    private IPath absoluteScriptPath;

    private String errorSource;

    private Map<IPV, IPVListener> pvListenerMap;

    private boolean errorInScript;

    private volatile boolean unRegistered = false;

    private boolean triggerSuppressed = false;

    private ScriptData scriptData;
    private AbstractBaseEditPart editPart;
    private IPV[] pvArray;

    public AbstractScriptStore(ScriptData scriptData, AbstractBaseEditPart editpart, IPV[] pvArray) throws Exception {
        this.scriptData = scriptData;
        this.editPart = editpart;
        this.pvArray = pvArray;

        editPart.addEditPartListener(new EditPartListener.Stub() {
            @Override
            public void partDeactivated(EditPart editpart) {
                dispose();
                editPart.removeEditPartListener(this);
            }
        });
        if (editPart.isActive()) {
            init();
        } else {
            editPart.addEditPartListener(new EditPartListener.Stub() {
                @Override
                public void partDeactivated(EditPart editpart) {
                    try {
                        init();
                        editPart.removeEditPartListener(this);
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot initialize script store.", e);
                    }
                }
            });
        }
    }

    private void init() throws Exception {
        if (!(scriptData instanceof RuleScriptData) && !scriptData.isEmbedded()) {
            absoluteScriptPath = scriptData.getPath();
            if (!absoluteScriptPath.isAbsolute()) {
                // The following looked like this:
                // absoluteScriptPath = ResourceUtil.buildAbsolutePath(
                // editpart.getWidgetModel(), absoluteScriptPath);
                // .. but that doesn't work when the editpart is already a LinkingContainer.
                // It would fetch the parent's DisplayModel, i.e. look for scripts where the container is used,
                // instead of where it's defined.
                //
                // After updating buildAbsolutePath() to handle (editpart instanceof LinkingContainer) as below,
                // the resolution of related displays failed again as in #977 because buildAbsolutePath() is
                // called in many placed while reading the *.opi file, and it would now build a different widget tree.
                //
                // The following just fixes the relative script lookup from linking containers for #998,
                // without disturbing any other code.
                //
                // TODO Understand & redo the whole widget model and its quirks for linking containers,
                // so all the recently added (.. instanceof ..Linking..) can be removed.
                var model = editPart.getWidgetModel();
                DisplayModel root;
                if (model instanceof AbstractLinkingContainerModel) {
                    root = ((AbstractLinkingContainerModel) model).getDisplayModel();
                } else {
                    root = model.getRootDisplayModel();
                }
                absoluteScriptPath = root.getOpiFilePath().removeLastSegments(1).append(absoluteScriptPath);
                // ---
                if (!ResourceUtil.isExsitingFile(absoluteScriptPath, true)) {
                    throw new FileNotFoundException(scriptData.getPath().toString());
                }
            }
        }

        initScriptEngine();

        errorInScript = false;
        errorSource = (scriptData instanceof RuleScriptData ? ((RuleScriptData) scriptData).getRuleData().getName()
                : scriptData.getPath().toString()) + " on " + editPart.getWidgetModel().getName();

        if (scriptData instanceof RuleScriptData) {
            compileString(((RuleScriptData) scriptData).getScriptString());
        } else if (scriptData.isEmbedded()) {
            compileString(scriptData.getScriptText());
        } else {
            var inputStream = ResourceUtil.pathToInputStream(absoluteScriptPath);
            compileInputStream(inputStream);
            inputStream.close();
        }

        pvListenerMap = new HashMap<>();

        var suppressPVListener = new IPVListener() {
            @Override
            public synchronized void valueChanged(IPV pv) {
                if (triggerSuppressed && checkPVsConnected(scriptData, pvArray)) {
                    executeScriptInUIThread(pv);
                    triggerSuppressed = false;
                }
            }
        };

        var triggerPVListener = new IPVListener() {
            @Override
            public synchronized void valueChanged(IPV pv) {
                // execute script only if all input pvs are connected
                if (pvArray.length > 1) {
                    if (!checkPVsConnected(scriptData, pvArray)) {
                        triggerSuppressed = true;
                        return;

                    }
                }
                executeScriptInUIThread(pv);
            }
        };

        // register pv listener
        for (var i = 0; i < pvArray.length; i++) {
            var pv = pvArray[i];
            if (pv == null) {
                continue;
            }
            if (!scriptData.getPVList().get(i).trigger) {
                // execute the script if it was suppressed.
                pv.addListener(suppressPVListener);
                pvListenerMap.put(pv, suppressPVListener);
                continue;
            }
            pv.addListener(triggerPVListener);
            pvListenerMap.put(pv, triggerPVListener);
        }
    }

    protected abstract void initScriptEngine() throws Exception;

    protected abstract void compileString(String string) throws Exception;

    /**
     * Compile InputStream with script engine. The stream will be closed by this method.
     */
    protected abstract void compileInputStream(InputStream s) throws Exception;

    /**
     * Execute the script with script engine.
     * 
     * @param triggerPV
     *            the PV that triggers this execution.
     */
    protected abstract void execScript(IPV triggerPV) throws Exception;

    private void executeScriptInUIThread(IPV triggerPV) {
        var display = editPart.getRoot().getViewer().getControl().getDisplay();
        UIBundlingThread.getInstance().addRunnable(display, () -> {
            // Avoid running a execution that was pending just before a Yamcs disconnect was done.
            // It can still go wrong later on, but with much reduced likelihood.
            if (!triggerPV.isConnected()) {
                return;
            }

            if ((!scriptData.isStopExecuteOnError() || !errorInScript) && !unRegistered) {
                try {
                    execScript(triggerPV);
                } catch (Exception e) {
                    errorInScript = true;
                    var notExecuteWarning = "\nThe script or rule will not be executed afterwards. "
                            + "You can change this setting in script dialog.";
                    var message = NLS.bind("Error in {0}.{1}\n{2}", new String[] { errorSource,
                            !scriptData.isStopExecuteOnError() ? "" : notExecuteWarning, e.toString() });
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, message, e);
                }
            }
        });
    }

    private boolean checkPVsConnected(ScriptData scriptData, IPV[] pvArray) {
        if (!scriptData.isCheckConnectivity()) {
            return true;
        }
        for (var i = 0; i < pvArray.length; i++) {
            var pv = pvArray[i];
            var isTrigger = scriptData.getPVList().get(i).trigger;
            if (!pv.isConnected() || (isTrigger && pv.getValue() == null)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void unRegister() {
        unRegistered = true;
        for (var entry : pvListenerMap.entrySet()) {
            entry.getKey().removeListener(entry.getValue());
        }
    }

    public ScriptData getScriptData() {
        return scriptData;
    }

    public AbstractBaseEditPart getEditPart() {
        return editPart;
    }

    public DisplayEditpart getDisplayEditPart() {
        if (getEditPart().isActive()) {
            return (DisplayEditpart) (getEditPart().getViewer().getContents());
        }
        return null;
    }

    public IPV[] getPvArray() {
        return pvArray;
    }

    public IPath getAbsoluteScriptPath() {
        return absoluteScriptPath;
    }

    /**
     * Dispose of all resources allocated by this script store.
     */
    protected void dispose() {
    }
}
