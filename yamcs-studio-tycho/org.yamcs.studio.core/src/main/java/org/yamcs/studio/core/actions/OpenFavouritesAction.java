package org.yamcs.studio.core.actions;

import org.csstudio.opibuilder.actions.OpenTopOPIsAction;
import org.eclipse.jface.action.IAction;

/**
 * Default behaviour is to open the first file in the list, which i consider a usability problem.
 * What it really should do is always show the menu. Needs more research on how to achieve that
 * though...
 */
public class OpenFavouritesAction extends OpenTopOPIsAction {

    @Override
    public void run(IAction action) {
        // NOP. (TODO show the getMenu())
    }
}
