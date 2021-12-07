package org.yamcs.studio.editor.base;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;

public class OpenWindowMenu extends ContributionItem {

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public void fill(Menu menu, int index) {
        var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        ContributionItemFactory.OPEN_WINDOWS.create(window).fill(menu, index);
    }
}
