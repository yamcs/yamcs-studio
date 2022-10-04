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

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.script.ScriptService.ScriptType;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The description data for a script.
 */
public class ScriptData implements IAdaptable {

    /**
     * The path of the script.
     */
    private IPath path;

    /**
     * The input PVs of the script. Which can be accessed in the script and trigger the script execution.
     */
    protected List<PVTuple> pvList = new ArrayList<>();

    /**
     * Check PVs connectivity before executing the script.
     */
    private boolean checkConnectivity = true;

    /**
     * Stop to execute the script if error is detected in script.
     */
    private boolean stopExecuteOnError = false;

    private ScriptType scriptType;

    private boolean isEmbedded = false;

    private String scriptText;

    private String scriptName;

    public ScriptData() {
        path = new Path("");
    }

    public ScriptData(IPath path) {
        this.path = path;
    }

    /**
     * Set the script path.
     *
     * @param path
     *            the file path of the script.
     * @return true if successful. false if the input is not a javascript file.
     */
    public boolean setPath(IPath path) {
        if (path.getFileExtension() != null) {
            this.path = path;
            return true;
        }
        return false;
    }

    /**
     * Get the path of the script.
     */
    public IPath getPath() {
        return path;
    }

    /**
     * Get the input PVs of the script
     */
    public List<PVTuple> getPVList() {
        return pvList;
    }

    public void addPV(PVTuple pvTuple) {
        if (!pvList.contains(pvTuple)) {
            pvList.add(pvTuple);
        }
    }

    public void removePV(PVTuple pvTuple) {
        pvList.remove(pvTuple);
    }

    public void setCheckConnectivity(boolean checkConnectivity) {
        this.checkConnectivity = checkConnectivity;
    }

    public boolean isCheckConnectivity() {
        return checkConnectivity;
    }

    public ScriptData getCopy() {
        var copy = new ScriptData();
        copy.setPath(path);
        copy.setCheckConnectivity(checkConnectivity);
        copy.setStopExecuteOnError(stopExecuteOnError);
        copy.setEmbedded(isEmbedded);
        copy.setScriptName(scriptName);
        copy.setScriptText(scriptText);
        copy.setScriptType(scriptType);
        for (var pv : pvList) {
            copy.addPV(new PVTuple(pv.pvName, pv.trigger));
        }
        return copy;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IWorkbenchAdapter.class) {
            return adapter.cast(new IWorkbenchAdapter() {
                @Override
                public Object getParent(Object o) {
                    return null;
                }

                @Override
                public String getLabel(Object o) {
                    if (isEmbedded) {
                        return getScriptName();
                    }
                    return path.toString();
                }

                @Override
                public ImageDescriptor getImageDescriptor(Object object) {
                    String icon;
                    if (isEmbedded) {
                        if (getScriptType() == ScriptType.PYTHON) {
                            icon = "icons/pyEmbedded.gif";
                        } else {
                            icon = "icons/jsEmbedded.gif";
                        }
                    } else if (path != null && !path.isEmpty() && path.getFileExtension().equals(ScriptService.PY)) {
                        icon = "icons/python_file.gif";
                    } else {
                        icon = "icons/js.gif";
                    }
                    return CustomMediaFactory.getInstance().getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID,
                            icon);
                }

                @Override
                public Object[] getChildren(Object o) {
                    return new Object[0];
                }
            });
        }

        return null;
    }

    /**
     * @param stopExecuteOnError
     *            If true, stop to execute the script if error is detected in script.
     */
    public void setStopExecuteOnError(boolean stopExecuteOnError) {
        this.stopExecuteOnError = stopExecuteOnError;
    }

    /**
     * @return true if stop to execute the script if error is detected in script..
     */
    public boolean isStopExecuteOnError() {
        return stopExecuteOnError;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public void setEmbedded(boolean isEmbedded) {
        this.isEmbedded = isEmbedded;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }
}
