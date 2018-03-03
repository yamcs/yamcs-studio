package org.yamcs.studio.explorer;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.navigator.CommonNavigator;

public class ExplorerView extends CommonNavigator {

    @Override
    protected Object getInitialInput() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        System.out.println("return initial input " + root);
        return root;
    }
}
