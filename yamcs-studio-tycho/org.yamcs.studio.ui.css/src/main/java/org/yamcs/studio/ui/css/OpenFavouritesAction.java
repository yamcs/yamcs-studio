package org.yamcs.studio.ui.css;

import org.csstudio.opibuilder.actions.OpenTopOPIsAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionDelegate2;

/**
 * Does some hacks to open the menu also with the main button click. Ideally we should spend some
 * more time here to align the menu to the pulldown arrow, as now it depends on the mouse location.
 */
public class OpenFavouritesAction extends OpenTopOPIsAction implements IActionDelegate2 {

    @Override
    public void init(IAction action) {
    }

    @Override
    public void run(IAction action) {
        // NOP. (runWithEvent is used instead, since we implement IActionDelegate2)
    }

    @Override
    public void runWithEvent(IAction action, Event event) {
        ToolItem item = (ToolItem) event.widget;
        Menu menu = getMenu(item.getParent());
        menu.setVisible(true);
    }
}
