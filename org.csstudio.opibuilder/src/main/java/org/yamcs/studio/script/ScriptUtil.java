/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.yamcs.studio.script;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.runmode.RunModeService.DisplayMode;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.MacrosInput;
import org.csstudio.opibuilder.widgetActions.ExecuteCommandAction;
import org.csstudio.opibuilder.widgetActions.OpenDisplayAction;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.osgi.framework.Version;

/**
 * The utility class to facilitate BOY script programming.
 */
public class ScriptUtil {

    /**
     * Open an OPI.
     *
     * @param widget
     *            the widget to which the script is attached.
     * @param opiPath
     *            the path of the OPI. It can be either an absolute path or a relative path to the Display file of the
     *            widget.
     * @param target
     *            target place of the new OPI. 0: new tab; 1: replace current one; 2: new window; 3: view on left; 4:
     *            view on right; 5: view on top; 6: view on bottom; 7: detached view; 8: new shell
     * @param macrosInput
     *            the macrosInput. null if no macros needed.
     */
    public static void openOPI(AbstractBaseEditPart widget, String opiPath, int target, MacrosInput macrosInput) {
        var action = new OpenDisplayAction();

        // Map target IDs of this API to DisplayMode
        DisplayMode mode;
        switch (target) {
        case 0:
            mode = DisplayMode.NEW_TAB;
            break;
        case 2:
            mode = DisplayMode.NEW_WINDOW;
            break;
        case 3:
            mode = DisplayMode.NEW_TAB_LEFT;
            break;
        case 4:
            mode = DisplayMode.NEW_TAB_RIGHT;
            break;
        case 5:
            mode = DisplayMode.NEW_TAB_TOP;
            break;
        case 6:
            mode = DisplayMode.NEW_TAB_BOTTOM;
            break;
        case 7:
            mode = DisplayMode.NEW_TAB_DETACHED;
            break;
        case 8:
            mode = DisplayMode.NEW_SHELL;
            break;
        default:
            mode = DisplayMode.REPLACE;
        }
        action.setWidgetModel(widget.getWidgetModel());
        action.setPropertyValue(OpenDisplayAction.PROP_PATH, opiPath);
        action.setPropertyValue(OpenDisplayAction.PROP_MACROS, macrosInput);
        action.setPropertyValue(OpenDisplayAction.PROP_MODE, mode.ordinal());
        action.run();
    }

    /**
     * Close current active OPI.
     */
    public static void closeCurrentOPI() {
        try {
            var activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            var activePart = activePage.getActivePart();

            if (activePart instanceof IEditorPart) {
                activePage.closeEditor((IEditorPart) activePart, false);
            } else if (activePart instanceof IViewPart) {
                activePage.hideView((IViewPart) activePart);
            }
        } catch (NullPointerException e) {
        }
    }

    /**
     * Close OPI associated with the provided widget.
     */
    public static void closeAssociatedOPI(AbstractBaseEditPart widget) {
        var widgetShell = widget.getWidgetModel().getRootDisplayModel().getViewer().getControl().getShell();
        // Is the shell part of a workbench window, or its own OPIShell?
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() != widgetShell) {
            widgetShell.close();
        } else {
            closeCurrentOPI();
        }
    }

    /**
     * Execute an Eclipse command with optional parameters. Any parameters must be defined along with the command in
     * plugin.xml.
     *
     * @param commandId
     *            the Eclipse command id
     * @param parameters
     *            a list of further String arguments alternating key, value: * executeEclipseCommand("id", ["pkey",
     *            "pvalue"])
     */
    public final static void executeEclipseCommand(String commandId, String[] parameters) {
        var handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getService(IHandlerService.class);
        try {
            if (parameters.length % 2 != 0) {
                throw new IllegalArgumentException(
                        "Parameterized commands must have " + "an equal number of keys and values");
            }

            if (parameters.length == 0) {
                handlerService.executeCommand(commandId, null);
            } else {
                var commandService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getService(ICommandService.class);
                var params = new Parameterization[parameters.length / 2];
                var c = commandService.getCommand(commandId);
                for (var i = 0; i < parameters.length / 2; i++) {
                    var key = parameters[2 * i];
                    var value = parameters[2 * i + 1];
                    var p = c.getParameter(key);
                    var pp = new Parameterization(p, value);
                    params[i] = pp;
                }
                var pc = new ParameterizedCommand(c, params);
                handlerService.executeCommand(pc, null);
            }
        } catch (Exception e) {
            ErrorHandlerUtil.handleError("Failed to execute eclipse command: " + commandId, e);
        }
    }

    /**
     * Execute an Eclipse command.
     *
     * @param commandId
     *            the Eclipse command id
     */
    public final static void executeEclipseCommand(String commandId) {
        executeEclipseCommand(commandId, new String[0]);
    }

    /**
     * Executing a system or shell command. On Unix, that could be anything in the PATH.
     * <p>
     * Several things can happen:
     * <ul>
     * <li>Command finishes OK right away
     * <li>Command gives error right away
     * <li>Command runs for a long time, eventually giving error or OK.
     * </ul>
     * The command executor waits a little time to see if the command finishes, and calls back in case of an error. When
     * the command finishes right away OK or runs longer, we leave it be. Command output will be printed on BOY console.
     *
     * @param command
     *            Command to run. Format depends on OS.
     * @param wait
     *            Time to wait for completion in seconds
     */
    public final static void executeSystemCommand(String command, int wait) {
        var action = new ExecuteCommandAction();
        action.setPropertyValue(ExecuteCommandAction.PROP_COMMAND, command);
        action.setPropertyValue(ExecuteCommandAction.PROP_WAIT_TIME, wait);
        action.run();
    }

    /**
     * Execute a runnable in UI thread.
     *
     * @param runnable
     *            the runnable to be executed.
     * @param widget
     *            any widget. It is referred to get the UI thread.
     */
    public final static void execInUI(Runnable runnable, AbstractBaseEditPart widget) {
        widget.getViewer().getControl().getDisplay().asyncExec(runnable);
    }

    @Deprecated
    public final static Version getBOYVersion() {
        return OPIBuilderPlugin.getDefault().getBundle().getVersion();
    }
}
