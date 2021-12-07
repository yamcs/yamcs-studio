package org.csstudio.opibuilder.util;

import java.util.HashMap;
import java.util.Map;

import org.csstudio.opibuilder.actions.FullScreenAction;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * A service for storing related resources for each workbench window.
 */
@SuppressWarnings("restriction")
public final class WorkbenchWindowService {

    private static WorkbenchWindowService instance;

    private Map<IWorkbenchWindow, FullScreenAction> fullScreenRegistry;

    public WorkbenchWindowService() {
        fullScreenRegistry = new HashMap<>();
    }

    public synchronized static final WorkbenchWindowService getInstance() {
        if (instance == null) {
            instance = new WorkbenchWindowService();
        }
        return instance;
    }

    public void registerFullScreenAction(FullScreenAction action, IWorkbenchWindow window) {
        fullScreenRegistry.put(window, action);
    }

    public void unregisterFullScreenAction(IWorkbenchWindow window) {
        fullScreenRegistry.remove(window);
    }

    public FullScreenAction getFullScreenAction(IWorkbenchWindow window) {
        return fullScreenRegistry.get(window);
    }

    public static void setToolbarVisibility(WorkbenchWindow window, boolean visible) {
        window.setCoolBarVisible(visible);
        window.setPerspectiveBarVisible(visible);

        // All these don't work
        // window.setStatusLineVisible(false);
        // window.getActionBars().getStatusLineManager().getItems()[0].setVisible(visible);
        // window.getStatusLineManager().getItems()[0].setVisible(visible);
        // window.getStatusLineManager().getControl().setVisible(visible);

        // A hack to set status line invisible.
        for (Control child : window.getShell().getChildren()) {
            if (child.isDisposed()) {
                continue;
            } else if (child.getClass().equals(Canvas.class)) {
                continue;
            } else if (child.getClass().equals(Composite.class)) {
                for (Control c : ((Composite) child).getChildren()) {
                    if (c.getClass().getSimpleName().contains("StatusLine")) {
                        child.setVisible(visible);
                        break;
                    }
                }
                continue;
            }

            child.setVisible(visible);

        }
        window.getShell().layout();

    }

}
