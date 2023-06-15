package org.csstudio.opibuilder.editparts;

import java.util.List;

import org.csstudio.opibuilder.widgetActions.AbstractWidgetAction;
import org.csstudio.opibuilder.widgetActions.OpenDisplayAction;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.swt.SWT;

public class HookedActionsMouseListener extends MouseListener.Stub {

    private List<AbstractWidgetAction> actions;

    public HookedActionsMouseListener(List<AbstractWidgetAction> actions) {
        this.actions = actions;
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (me.button != 1) {
            return;
        }

        runActions((me.getState() & SWT.CONTROL) != 0, (me.getState() & SWT.SHIFT) != 0);
    }

    public void runActions(boolean ctrlPressed, boolean shiftPressed) {
        for (var action : actions) {
            if (action instanceof OpenDisplayAction) {
                ((OpenDisplayAction) action).runWithModifiers(ctrlPressed, shiftPressed);
            } else {
                action.run();
            }
        }
    }
}
