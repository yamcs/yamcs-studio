/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.runmode;

import java.io.InputStream;
import java.util.Objects;

import org.csstudio.opibuilder.util.MacrosInput;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * The editor input for OPI Runner.
 */
public class RunnerInput implements IRunnerInput {

    private DisplayOpenManager displayOpenManager;
    private MacrosInput macrosInput;
    private IPath path;

    public RunnerInput(IPath path, DisplayOpenManager displayOpenManager, MacrosInput macrosInput) {
        this.path = path;
        setDisplayOpenManager(displayOpenManager);
        this.macrosInput = macrosInput;
    }

    public RunnerInput(IPath path, DisplayOpenManager displayOpenManager) {
        this(path, displayOpenManager, null);
    }

    @Override
    public void setDisplayOpenManager(DisplayOpenManager displayOpenManager) {
        this.displayOpenManager = displayOpenManager;
    }

    @Override
    public DisplayOpenManager getDisplayOpenManager() {
        return displayOpenManager;
    }

    @Override
    public int hashCode() {
        return Objects.hash(macrosInput, path);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        var other = (RunnerInput) obj;
        if (!Objects.equals(macrosInput, other.macrosInput)) {
            return false;
        }
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        return true;
    }

    // @Override
    // public boolean equals(Object obj) {
    // if (this == obj) {
    // return true;
    // }
    // if (!(obj instanceof RunnerInput)) {
    // return false;
    // }
    // RunnerInput other = (RunnerInput) obj;
    // boolean macroSame = false;
    // if(macrosInput != null && other.getMacrosInput() !=null){
    // macroSame = macrosInput.equals(other.getMacrosInput());
    // }else if(macrosInput == null && other.getMacrosInput() == null)
    // macroSame = true;
    // return getPath().equals(other.getPath()) && macroSame;
    // // displayOpenManager == other.getDisplayOpenManager() &&
    //
    // }

    @Override
    public MacrosInput getMacrosInput() {
        return macrosInput;
    }

    @Override
    public void saveState(IMemento memento) {
        RunnerInputFactory.saveState(memento, this);
    }

    @Override
    public String getFactoryId() {
        return RunnerInputFactory.getFactoryId();
    }

    @Override
    public IPath getPath() {
        return path;
    }

    @Override
    public boolean exists() {
        InputStream in = null;
        try {
            in = getInputStream();
        } catch (Exception e) {
            return false;
        }
        return in != null;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return getPath().lastSegment();
    }

    @Override
    public IPersistableElement getPersistable() {
        return this;
    }

    @Override
    public String getToolTipText() {
        return path.toString();
    }

    @Override
    public InputStream getInputStream() throws Exception {
        return ResourceUtil.pathToInputStream(getPath());
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    @Override
    public String toString() {
        return getPath().toString();
    }
}
