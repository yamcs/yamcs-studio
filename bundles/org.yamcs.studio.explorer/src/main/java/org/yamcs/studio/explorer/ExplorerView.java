package org.yamcs.studio.explorer;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.navigator.CommonNavigator;

public class ExplorerView extends CommonNavigator {

    @Override
    protected Object getInitialInput() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
}
