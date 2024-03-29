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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.util.SizeLimitedStack;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;

/**
 * A manager help to manage the display open history and provide go back and forward functions.
 */
public class DisplayOpenManager {
    private SizeLimitedStack<IRunnerInput> backStack;
    private SizeLimitedStack<IRunnerInput> forwardStack;
    private List<IDisplayOpenManagerListener> listeners;
    private static int STACK_SIZE = 10;
    private IOPIRuntime opiRuntime;

    public DisplayOpenManager(IOPIRuntime opiRuntime) {
        backStack = new SizeLimitedStack<>(STACK_SIZE);
        forwardStack = new SizeLimitedStack<>(STACK_SIZE);
        listeners = new ArrayList<>();
        this.opiRuntime = opiRuntime;
    }

    public void openNewDisplay() {
        var input = getCurrentRunnerInputInEditor();
        if (input != null) {
            backStack.push(input);
        }
        forwardStack.clear();
        fireOperationsHistoryChanged();
    }

    public void goBack() {
        if (backStack.size() == 0) {
            return;
        }

        var input = getCurrentRunnerInputInEditor();
        if (input != null) {
            forwardStack.push(input);
        }

        openOPI(backStack.pop());
    }

    private IRunnerInput getCurrentRunnerInputInEditor() {

        var input = opiRuntime.getOPIInput();

        if (input instanceof IRunnerInput) {
            if (((IRunnerInput) input).getDisplayOpenManager() == null) {
                ((IRunnerInput) input).setDisplayOpenManager(opiRuntime.getAdapter(DisplayOpenManager.class));
            }
            return (IRunnerInput) input;
        } else {
            return new RunnerInput(getCurrentPathInEditor(), opiRuntime.getAdapter(DisplayOpenManager.class));
        }
    }

    private IPath getCurrentPathInEditor() {
        return ResourceUtil.getPathInEditor(opiRuntime.getOPIInput());
    }

    /** @param input */
    private void openOPI(IRunnerInput input) {
        try {
            opiRuntime.setOPIInput(input);
        } catch (PartInitException e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "Failed to go back", e);
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Open file error", "Failed to go back");
        }

        fireOperationsHistoryChanged();
    }

    public void goForward() {
        if (forwardStack.size() == 0) {
            return;
        }
        var input = getCurrentRunnerInputInEditor();
        if (input != null) {
            backStack.push(input);
        }

        openOPI(forwardStack.pop());
    }

    public void goBack(int index) {
        if (backStack.size() > index) {
            var input = getCurrentRunnerInputInEditor();
            if (input != null) {
                forwardStack.push(input);
            }

            for (var i = 0; i < index; i++) {
                forwardStack.push(backStack.pop());
            }

            openOPI(backStack.pop());
        }
    }

    public void goForward(int index) {
        if (forwardStack.size() > index) {
            var input = getCurrentRunnerInputInEditor();
            if (input != null) {
                backStack.push(input);
            }

            for (var i = 0; i < index; i++) {
                backStack.push(forwardStack.pop());
            }

            openOPI(forwardStack.pop());
        }
    }

    public void addListener(IDisplayOpenManagerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public boolean removeListener(IDisplayOpenManagerListener listener) {
        return listeners.remove(listener);
    }

    private void fireOperationsHistoryChanged() {
        for (var listener : listeners) {
            listener.displayOpenHistoryChanged(this);
        }
    }

    public boolean canBackward() {
        return backStack.size() > 0;
    }

    public boolean canForward() {
        return forwardStack.size() > 0;
    }

    /**
     * Return an array of all elements in the backward stack. The oldest element is the first element of the returned
     * array.
     *
     * @return the array contained all elements in the stack.
     */
    public Object[] getBackStackEntries() {
        return backStack.toArray();
    }

    /**
     * Return an array of all elements in the forward stack. The oldest element is the first element of the returned
     * array.
     *
     * @return the array contained all elements in the stack.
     */
    public Object[] getForwardStackEntries() {
        return forwardStack.toArray();
    }

    /**
     * Dispose of all resources allocated by this manager.
     */
    public void dispose() {
        backStack.clear();
        forwardStack.clear();
        listeners.clear();
        opiRuntime = null;
    }
}
