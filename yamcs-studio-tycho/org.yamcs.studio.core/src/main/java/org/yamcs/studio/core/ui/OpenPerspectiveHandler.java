package org.yamcs.studio.core.ui;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.menus.UIElement;

@SuppressWarnings("restriction")
public class OpenPerspectiveHandler extends AbstractHandler implements IElementUpdater {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (HandlerUtil.matchesRadioState(event))
            return null;

        String radioParameter = event.getParameter(RadioState.PARAMETER_ID);
        HandlerUtil.updateRadioState(event.getCommand(), radioParameter);

        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        // If there's already a window open for that perspective, bring that to the front instead
        for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            if (radioParameter.equals(window.getActivePage().getPerspective().getId())) {
                try {
                    PlatformUI.getWorkbench().showPerspective(radioParameter, window);
                } catch (WorkbenchException e) {
                    ErrorDialog.openError(activeWindow.getShell(),
                            "Could not open window", e.getMessage(), e.getStatus());
                }
                return null;
            }
        }
        openNewWindowPerspective(radioParameter, activeWindow);
        return null;
    }

    /*
     * Workaround to allow checking radio items in a dynamic contribution
     *
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=398647
     */
    @Override
    public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
        ICommandService service = (ICommandService) element.getServiceLocator().getService(ICommandService.class);
        String state = (String) parameters.get(RadioState.PARAMETER_ID);
        Command command = service.getCommand(PerspectiveItems.OPEN_PERSPECTIVE_COMMAND);
        State commandState = command.getState(RadioState.STATE_ID);
        if (commandState.getValue().equals(state)) {
            element.setChecked(true);
        }
    }

    /**
     * Same code as in the private method of ShowPerspectiveHandler
     */
    private void openNewWindowPerspective(String perspectiveId, IWorkbenchWindow activeWorkbenchWindow) throws ExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        try {
            IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
            workbench.openWorkbenchWindow(perspectiveId, input);
        } catch (WorkbenchException e) {
            ErrorDialog.openError(activeWorkbenchWindow.getShell(),
                    "Could not open window", e.getMessage(), e.getStatus());
        }
    }
}
